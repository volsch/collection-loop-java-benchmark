# Java Collection Loop Benchmark

Benchmark for different types of Java collection loops. The benchmark uses Java 21 (OpenJDK 21) and
is based on [JMH framework](https://github.com/openjdk/jmh).

Build:

```bash
mvn clean verify
```

Run:

```bash
java -jar target/collection-loop-benchmarks.jar -prof gc
```
