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

        if (name.equalsIgnoreCase("popularity")) {
            return new PopularityBased();
        }

        if (name.equalsIgnoreCase("lda")) {
            return new LDA();
        }

        return null;
    }
}
