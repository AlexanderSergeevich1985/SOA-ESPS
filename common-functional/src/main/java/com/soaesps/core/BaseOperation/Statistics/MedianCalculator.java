package com.soaesps.core.BaseOperation.Statistics;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.*;

public class MedianCalculator {
    public static void main(String[] args) throws IOException {
        long sum_result = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line = reader.readLine();
            int n = Integer.parseInt(line);
            int rest = n % 2;
            int maxSizeRight = n/2;
            int maxSizeLeft = rest == 0 ? maxSizeRight-1 : maxSizeRight;
            PriorityQueue<Integer> left = new PriorityQueue<>(maxSizeLeft, Collections.reverseOrder());
            PriorityQueue<Integer> right = new PriorityQueue<>(maxSizeRight);
            line = reader.readLine();
            StringTokenizer tokenizer = new StringTokenizer(line, " ");
            int result = Integer.parseInt(tokenizer.nextToken());
            sum_result = result;
            boolean flag = false;
            int rv = Integer.MAX_VALUE, lv = 0;
            int temp = 0;
            for (int i = 1; i < n; ++i) {
                int value = Integer.parseInt(tokenizer.nextToken());
                if (flag) {
                    if (value < result) {
                        left.add(value);
                        lv = Math.max(lv, value);
                    } else {
                        left.add(result);
                        lv = result;
                        if (rv < value) {
                            right.poll();
                            right.add(value);
                            result = rv;
                            rv = right.peek();
                        } else {
                            result = value;
                        }
                    }
                } else {
                    if (value > result) {
                        right.add(value);
                        rv = Math.min(rv, value);
                    } else {
                        right.add(result);
                        rv = result;
                        if (lv > value) {
                            left.poll();
                            left.add(value);
                            result = lv;
                            lv = left.peek();
                        } else {
                            result = value;
                        }
                    }
                }
                flag = !flag;
                sum_result += result;
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out))) {
            writer.write(String.valueOf(sum_result));
        }
    }
}
