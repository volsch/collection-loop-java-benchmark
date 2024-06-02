/*
 * Copyright (c) 2024, Volker Schmidt
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice,this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS”
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package eu.volsch.benchmark.collectionloop;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.profile.LinuxPerfProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CollectionLoopBenchmark {

  @Param({"0", "1", "10", "100", "1000"})
  private int loopIterations;

  private List<Integer> values;

  @Setup
  public void setup() {
    values = loopIterations == 0 ? emptyList() : new ArrayList<>(loopIterations);
    for (var i = 0; i < loopIterations; i++) {
      values.add(i);
    }
  }

  @Benchmark
  public List<Integer> loop() {
    final var localValues = this.values;
    final var size = localValues.size();

    final List<Integer> result = size == 0 ? emptyList() : new ArrayList<>(size);
    for (var i = 0; i < size; i++) {
      final var value = localValues.get(i);
      if (value % 2 == 0) {
        result.add(value);
      }
    }
    return result;
  }

  @Benchmark
  public List<Integer> enhancedLoop() {
    final List<Integer> result = values.isEmpty() ? emptyList() : new ArrayList<>(values.size());
    for (final var value : values) {
      if (value % 2 == 0) {
        result.add(value);
      }
    }
    return result;
  }

  @Benchmark
  public List<Integer> forEach() {
    final List<Integer> result;
    if (values.isEmpty()) {
      result = emptyList();
    } else {
      result = new ArrayList<>(values.size());
      values.forEach(value -> {
        if (value % 2 == 0) {
          result.add(value);
        }
      });
    }
    return result;
  }

  @Benchmark
  public List<Integer> streamToList() {
    return values.stream().filter(v -> v % 2 == 0).toList();
  }

  @Benchmark
  public List<Integer> streamToCollection() {
    return values.stream().filter(v -> v % 2 == 0)
        .collect(
            toCollection(() -> values.isEmpty() ? emptyList() : new ArrayList<>(values.size())));
  }

  public static void main(String[] args) throws RunnerException {
    final Options opt = new OptionsBuilder()
        .include(CollectionLoopBenchmark.class.getName())
        .addProfiler(LinuxPerfProfiler.class)
        .build();
    new Runner(opt).run();
  }
}
