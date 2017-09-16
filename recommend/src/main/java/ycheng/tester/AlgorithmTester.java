package ycheng.tester;

import java.util.Map;

/**
 * Created by ycheng on 9/16/17.
 */
public interface AlgorithmTester {
    void preprocess(String srcFile);
    TestResult test(Map parameters);
}
