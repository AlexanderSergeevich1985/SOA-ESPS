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

    public TritonGrpcClient(
            @Value("${app.triton.grpc-host:triton-server}") String host,
            @Value("${app.triton.grpc-port:8001}") int port,
            @Value("${app.triton.timeout-ms:5000}") long timeoutMs) {
        this.timeoutMs = timeoutMs;
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()   // for TLS: .useTransportSecurity() + SslContext
                .build();
        this.asyncStub = GRPCInferenceServiceGrpc.newStub(channel);
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
}