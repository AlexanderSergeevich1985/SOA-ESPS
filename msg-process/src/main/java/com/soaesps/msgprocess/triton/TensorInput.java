package com.soaesps.msgprocess.triton;

import java.util.List;

/** Normalized FP32 tensor ready to be sent to Triton. */
public record TensorInput(
        String name,        // "INPUT__0"
        int batch,          // rows in frame
        int features,       // columns in frame
        List<Float> data    // flattened row-major
) {}