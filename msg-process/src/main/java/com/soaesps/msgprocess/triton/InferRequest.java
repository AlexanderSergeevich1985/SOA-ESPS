package com.soaesps.msgprocess.triton;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Triton Inference Server v2 HTTP protocol DTOs.
 * See: https://github.com/triton-inference-server/inference-common/blob/main/docs/protocol/extension_classification.md
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InferRequest(
        String id,
        List<InferInput> inputs,
        List<InferOutputRequest> outputs
) {
    public record InferInput(
            String name,          // e.g. "INPUT__0"
            List<Integer> shape,  // [batch, features]
            String datatype,      // "FP32"
            List<Float> data      // flattened row-major
    ) {}

    public record InferOutputRequest(String name) {}
}