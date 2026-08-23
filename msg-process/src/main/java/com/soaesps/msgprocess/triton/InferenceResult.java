package com.soaesps.msgprocess.triton;

import java.util.List;

/** Decoded Triton gRPC response tensor. */
public record InferenceResult(
        String name,
        List<Long> shape,
        List<Float> data
) {}