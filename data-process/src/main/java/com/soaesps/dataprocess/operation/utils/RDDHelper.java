package com.soaesps.dataprocess.operation.utils;

import org.apache.commons.math3.util.Pair;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.distributed.*;
import scala.Tuple2;

import java.util.*;

import static org.apache.commons.math3.util.FastMath.floor;
import static org.apache.commons.math3.util.FastMath.round;

public class RDDHelper {
    private RDDHelper() {}

    static public JavaRDD<IndexedRow> transformRDD(JavaRDD<Vector> vectorSet) {
        JavaRDD<IndexedRow> irMat = vectorSet.zipWithIndex().map(pair -> new IndexedRow(pair._2, pair._1));
        CoordinateMatrix corMat = new IndexedRowMatrix(irMat.rdd()).toCoordinateMatrix().transpose();
        int num = ((int) corMat.numCols());
        JavaRDD<IndexedRow> irTransMat = corMat.entries()
                .toJavaRDD()
                .mapToPair(entry -> new Tuple2<>(entry.i(), entry))
                .groupByKey().map(pair -> {
                    Iterator<MatrixEntry> iter = pair._2().iterator();
                    double[] array = new double[num];
                    int i = 0;
                    while (iter.hasNext()) {
                        array[i] = iter.next().value();
                        ++i;
                    }

                    return new IndexedRow(pair._1(), Vectors.dense(array));
                });

        return irTransMat;
    }

    static public double[][] rddVecToArray(JavaRDD<Vector> rddVec) {
        List<Vector> list = rddVec.collect();
        double[][] matrix = new double[list.size()][list.get(0).size()];
        for (int i = 0; i < list.size(); ++i) {
            matrix[i] = list.get(i).toArray();
        }

        return matrix;
    }

    static public double fivePercentCVaR(JavaRDD<Double> tests) {
        int count = round(tests.count() / 20 + 0.5f);
        List<Double> topWorst = tests.takeOrdered(count);
        Optional<Double> sum = topWorst.stream().reduce((aDouble, aDouble2) -> (aDouble+ aDouble2));

        return sum.get() / topWorst.size();
    }

    static public Pair<Double, Double> calcConfidenceInterval(
            JavaRDD<Double> tests, int numSamples, Double pValue) {
        int index = 0;
        List<Double> stats = new ArrayList<>();
        while(index < numSamples) {
            JavaRDD<Double> resample = tests.sample(true, 1.0);
            stats.add(fivePercentCVaR(resample));
        }
        stats.sort((Double o1, Double o2) -> (int) floor(o1 - o2));
        int lowerIndex = (int) round(numSamples * pValue / 2);
        int upperIndex = (int) round(numSamples * (1 - pValue / 2));

        return new Pair<>(stats.get(lowerIndex), stats.get(upperIndex));
    }
}