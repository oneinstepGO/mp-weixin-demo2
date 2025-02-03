package com.demo.thread.comparison;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

public class BenchmarkRunner {
    public static void main(String[] args) throws RunnerException {
        System.setProperty("jmh.ignoreLock", "true");

        Options opt = new OptionsBuilder()
                .include(ThreadBenchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(2)
                .measurementIterations(3)
                .threads(1)
                .timeUnit(TimeUnit.MILLISECONDS)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(opt).run();
    }
} 