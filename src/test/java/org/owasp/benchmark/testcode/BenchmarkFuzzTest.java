package org.owasp.benchmark.testcode;

import static org.owasp.benchmark.testcode.FuzzServlet.fuzzServlet;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

class BenchmarkFuzzTest {

  @FuzzTest
  void fuzzBenchmarkTest02246(FuzzedDataProvider data) throws Exception {
      fuzzServlet(data, new BenchmarkTest02246());
  }

  @FuzzTest
  void fuzzBenchmarkTest00001(FuzzedDataProvider data) throws Exception {
      fuzzServlet(data, new BenchmarkTest00001());
  }

  @FuzzTest
  void fuzzBenchmarkTest00002(FuzzedDataProvider data) throws Exception {
      fuzzServlet(data, new BenchmarkTest00002());
  }

  @FuzzTest
  void fuzzBenchmarkTest00003(FuzzedDataProvider data) throws Exception {
      fuzzServlet(data, new BenchmarkTest00003());
  }

  @FuzzTest
  void fuzzBenchmarkTest00004(FuzzedDataProvider data) throws Exception {
      fuzzServlet(data, new BenchmarkTest00004());
  }

}
