package ycheng.tester;

/**
 * Created by ycheng on 9/16/17.
 */
public class RecommendTestFactory {
    public AlgorithmTester getTester(String name) {
        if (name == null) {
            return null;
        }

        if (name.equalsIgnoreCase("itemknn")) {
            return new ItemKNNTester();
        }

        return null;
    }
}
