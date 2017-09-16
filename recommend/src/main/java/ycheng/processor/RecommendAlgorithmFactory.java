package ycheng.processor;

/**
 * Created by ycheng on 9/16/17.
 */
public class RecommendAlgorithmFactory {
    public RecommendAlgorithm getAlgo(String name) {
        if (name == null) {
            return null;
        }

        if (name.equalsIgnoreCase("itemknn")) {
            return new ItemKNN();
        }

        return null;
    }
}
