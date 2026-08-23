package com.soaesps.msgprocess.service.triton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.msgprocess.exception.MalformedFrameException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.soaesps.msgprocess.triton.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Converts a raw Kafka frame (JSON double[][]) into a normalized FP32 tensor
 * suitable for Triton inference.
 *
 * <p>If the frame format is Arrow/Parquet/CSV instead of JSON — replace only
 * the parsing block, the normalization/tensor logic stays the same.
 */
@Service
public class FramePreprocessor {

    private final ObjectMapper objectMapper;
    private final int expectedFeatures;
    private final double[] mean;   // null if normalization disabled
    private final double[] std;

    public FramePreprocessor(
            ObjectMapper objectMapper,
            @Value("${app.triton.features}") int expectedFeatures,
            @Value("${app.triton.mean:}") String meanCsv,
            @Value("${app.triton.std:}") String stdCsv) {
        this.objectMapper = objectMapper;
        this.expectedFeatures = expectedFeatures;
        this.mean = parseCsv(meanCsv);
        this.std = parseCsv(stdCsv);
        if ((mean == null) != (std == null)) {
            throw new IllegalArgumentException("app.triton.mean and app.triton.std must be set together");
        }
        if (mean != null && mean.length != expectedFeatures) {
            throw new IllegalArgumentException("mean/std length must equal app.triton.features");
        }
    }

    /**
     * @param rawFrame  JSON array of rows: [[f1, f2, ...], [...]]
     * @param inputName Triton input tensor name, e.g. "INPUT__0"
     */
    public TensorInput preprocess(byte[] rawFrame, String inputName) {
        final double[][] rows;
        try {
            rows = objectMapper.readValue(rawFrame, double[][].class);
        } catch (IOException e) {
            throw new MalformedFrameException("Frame is not valid JSON double[][]", e);
        }

        if (rows.length == 0) {
            throw new MalformedFrameException("Frame contains no rows");
        }

        List<Float> flat = new ArrayList<>(rows.length * expectedFeatures);
        for (int r = 0; r < rows.length; r++) {
            if (rows[r].length != expectedFeatures) {
                throw new MalformedFrameException(
                        "Row " + r + " has " + rows[r].length + " features, expected " + expectedFeatures);
            }
            for (int i = 0; i < rows[r].length; i++) {
                double v = rows[r][i];
                if (mean != null) {
                    v = (v - mean[i]) / std[i];   // z-score normalization
                }
                flat.add((float) v);
            }
        }

        InferRequest.InferInput input = new InferRequest.InferInput(
                inputName,
                List.of(rows.length, expectedFeatures),  // shape = [batch, features]
                "FP32",
                flat);

        /*return new InferRequest(
                UUID.randomUUID().toString(),
                List.of(input),
                List.of(new InferRequest.InferOutputRequest("OUTPUT__0")));*/

        return new TensorInput(inputName, rows.length, expectedFeatures, flat);
    }

    private double[] parseCsv(String csv) {
        if (csv == null || csv.isBlank()) return null;
        String[] parts = csv.split(",");
        double[] out = new double[parts.length];
        for (int i = 0; i < parts.length; i++) out[i] = Double.parseDouble(parts[i].trim());
        return out;
    }
}