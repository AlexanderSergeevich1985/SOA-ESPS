package com.soaesps.core.Utils.BaseOperation.statistics;

public class StatisticHelper {
    private StatisticHelper() {}

    static public Boolean itIsHappened(Double probability) {
        return getRandomNumber(1, 101) <=  100 * probability;
    }

    static public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    static public int getRandomInt(double probability, int numOfBits) {
        StringBuilder sb = new StringBuilder();
        int maxOfBits = numOfBits < 32 ? numOfBits : 31;
        for (int i = 0; i < maxOfBits; ++i) {
            sb.append(itIsHappened(probability) ? 1 : 0);
        }

        return Integer.parseInt(sb.toString(), 2);
    }

    static public int getRandomInt(int numOfBits) {
        return getRandomInt(0.5, numOfBits);
    }

    static public int getRandomInt(double[] probability) {
        StringBuilder sb = new StringBuilder();
        int maxOfBits = probability.length < 32 ? probability.length : 31;
        for (int i = 0; i < maxOfBits; ++i) {
            sb.append(itIsHappened(probability[i]) ? 1 : 0);
        }

        return Integer.parseInt(sb.toString(), 2);
    }
}