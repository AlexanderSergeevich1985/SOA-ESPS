package com.soaesps.msgprocess.client.triton;

import com.google.protobuf.ByteString;
import com.soaesps.msgprocess.exception.TritonInferenceException;
import com.soaesps.msgprocess.triton.InferenceResult;
import com.soaesps.msgprocess.triton.TensorInput;
import inference.GRPCInferenceServiceGrpc;
import inference.GrpcService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Non-blocking Triton client over gRPC (default port 8001).
 */
@Component
public class TritonGrpcClient implements DisposableBean {

    private final ManagedChannel channel;
    private final GRPCInferenceServiceGrpc.GRPCInferenceServiceStub asyncStub;
    private final long timeoutMs;
    private String inputName;

    public TritonGrpcClient(
            @Value("${app.triton.grpc-host:triton-server}") String host,
            @Value("${app.triton.grpc-port:8001}") int port,
            @Value("${app.triton.timeout-ms:5000}") long timeoutMs,
            @Value("${app.triton.input-name:RAW_FEATURES}") String inputName) {
        this.timeoutMs = timeoutMs;
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()   // for TLS: .useTransportSecurity() + SslContext
                .build();
        this.asyncStub = GRPCInferenceServiceGrpc.newStub(channel);
        this.inputName = inputName;
    }

    /** Runs inference; completes when Triton responds or the deadline hits. */
    public Mono<InferenceResult> infer(String modelName, TensorInput input) {
        GrpcService.ModelInferRequest request = buildRequest(modelName, input);
        return Mono.<GrpcService.ModelInferResponse>create(sink ->
                        asyncStub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                                .modelInfer(request, unaryObserver(sink)))
                .map(this::decodeResponse);
    }

    /** Readiness probe for startup checks. */
    public Mono<Boolean> isModelReady(String modelName) {
        GrpcService.ModelReadyRequest request = GrpcService.ModelReadyRequest.newBuilder().setName(modelName).build();
        return Mono.<GrpcService.ModelReadyResponse>create(sink ->
                        asyncStub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                                .modelReady(request, unaryObserver(sink)))
                .map(GrpcService.ModelReadyResponse::getReady)
                .onErrorReturn(false);
    }

    /** Bridges a unary gRPC call into a Reactor MonoSink. */
    private <T> StreamObserver<T> unaryObserver(MonoSink<T> sink) {
        return new StreamObserver<T>() {
            @Override
            public void onNext(T value) {
                sink.success(value);
            }

            @Override
            public void onError(Throwable t) {
                sink.error(t);
            }

            @Override
            public void onCompleted() {
                // unary call: value already delivered via onNext
            }
        };
    }

    // ---------- proto mapping ----------

    private GrpcService.ModelInferRequest buildRequest(String modelName, TensorInput input) {
        ByteBuffer buffer = ByteBuffer.allocate(input.data().size() * Float.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN);
        for (float value : input.data()) {
            buffer.putFloat(value);
        }

        GrpcService.ModelInferRequest.InferInputTensor tensor = GrpcService.ModelInferRequest.InferInputTensor.newBuilder()
                .setName(input.name())
                .setDatatype("FP32")
                .addShape(input.batch())
                .addShape(input.features())
                .build();

        return GrpcService.ModelInferRequest.newBuilder()
                .setModelName(modelName)
                .setId(UUID.randomUUID().toString())
                .addInputs(tensor)
                .addRawInputContents(ByteString.copyFrom(buffer.array()))
                .build();
    }

    private InferenceResult decodeResponse(GrpcService.ModelInferResponse response) {
        if (response.getOutputsCount() == 0) {
            throw new TritonInferenceException(0, "Response contains no output tensors");
        }
        GrpcService.ModelInferResponse.InferOutputTensor out = response.getOutputs(0);

        ByteBuffer buffer = response.getRawOutputContents(0)
                .asReadOnlyByteBuffer()
                .order(ByteOrder.LITTLE_ENDIAN);

        List<Float> data = new ArrayList<>(buffer.remaining() / Float.BYTES);
        while (buffer.remaining() >= Float.BYTES) {
            data.add(buffer.getFloat());
        }
        return new InferenceResult(out.getName(), out.getShapeList(), data);
    }

    @Override
    public void destroy() {
        channel.shutdown();
    }

    /** Runs inference on raw JSON features; Triton ensemble does the preprocessing. */
    public Mono<InferenceResult> infer(String modelName, String jsonFeatures) {
        GrpcService.ModelInferRequest request = buildRequest(modelName, jsonFeatures);
        return Mono.<GrpcService.ModelInferResponse>create(sink ->
                        asyncStub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS)
                                .modelInfer(request, unaryObserver(sink)))
                .map(this::decodeResponse);
    }

    /**
     * Builds a BYTES tensor with the JSON payload.
     *
     * <p>Triton wire format for BYTES tensors: each element is
     * 4-byte little-endian length prefix + raw bytes.
     */
    private GrpcService.ModelInferRequest buildRequest(String modelName, String jsonFeatures) {
        byte[] json = jsonFeatures.getBytes(StandardCharsets.UTF_8);

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + json.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(json.length);   // length prefix (LE)
        buffer.put(json);             // payload

        GrpcService.ModelInferRequest.InferInputTensor tensor = GrpcService.ModelInferRequest.InferInputTensor.newBuilder()
                .setName(inputName)   // MUST match the ensemble model input name in config.pbtxt
                .setDatatype("BYTES")
                .addShape(1)
                .build();

        return GrpcService.ModelInferRequest.newBuilder()
                .setModelName(modelName)
                .setId(UUID.randomUUID().toString())
                .addInputs(tensor)
                .addRawInputContents(ByteString.copyFrom(buffer.array()))
                .build();
    }
}