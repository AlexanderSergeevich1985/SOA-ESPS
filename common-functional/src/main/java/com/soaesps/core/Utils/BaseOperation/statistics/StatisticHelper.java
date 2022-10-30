package com.soaesps.core.Utils.BaseOperation.statistics;

public class StatisticHelper {
    private StatisticHelper() {}

    static public Boolean itIsHappened(Double probability) {
        return getRandomNumber(1, 101) <=  100 * probability;
    }

    static public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}