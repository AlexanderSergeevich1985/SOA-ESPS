package com.soaesps.msgprocess.triton;

import java.util.List;

public record InferResponse(
        String model_name,
        String id,
        List<OutputTensor> outputs
) {
    public record OutputTensor(
            String name,
            List<Long> shape,
            String datatype,
            List<Float> data
    ) {}
}