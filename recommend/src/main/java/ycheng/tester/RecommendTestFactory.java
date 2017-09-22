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

        if (name.equalsIgnoreCase("popularity")) {
            return new PopularityBasedTester();
        }

        if (name.equalsIgnoreCase("lda")) {
            return new LDATester();
        }

        return null;
    }
}
