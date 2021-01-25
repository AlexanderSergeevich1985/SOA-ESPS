package com.soaesps.core.BaseOperation.evaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BinaryEvaluator {
    private Integer tpValue;

    private Integer fpValue;

    private Integer fnValue;

    private Integer tnValue;

    public BigDecimal calcAccuracy() {
        BigDecimal result = new BigDecimal(tpValue + tnValue);

        return result.divide((new BigDecimal(tpValue + fpValue + fnValue + tnValue)), RoundingMode.FLOOR);
    }

    public BigDecimal calcPrecision() {
        BigDecimal result = new BigDecimal(tpValue);

        return result.divide((new BigDecimal(tpValue + fpValue)), RoundingMode.FLOOR);
    }

    public BigDecimal calcRecall() { //Sensitivty = TPR(True Positive Rate)= Recall
        BigDecimal result = new BigDecimal(tpValue);

        return result.divide((new BigDecimal(tpValue + fnValue)), RoundingMode.FLOOR);
    }

    public BigDecimal calcFPR() { //1- Specificity = FPR(False Positive Rate)
        BigDecimal result = new BigDecimal(fpValue);

        return result.divide((new BigDecimal(tnValue + fpValue)), RoundingMode.FLOOR);
    }

    public BigDecimal calcF1Score() {
        BigDecimal precision = calcPrecision();
        BigDecimal recall = calcRecall();

        return multiply(2, precision.multiply(recall).divide(precision.add(recall)));
    }

    public static BigDecimal multiply(int a, BigDecimal b) {
        return BigDecimal.valueOf(a).multiply(b);
    }
}