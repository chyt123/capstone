package ycheng.processor;

import ycheng.model.BuyRecord;
import ycheng.model.ClickRecord;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/16/17.
 */
public interface RecommendAlgorithm {

    /**
     * train a item recommend model and save the model to file
     */
    void train(Stream buyRecords, Stream clickRecords);


    /**
     * recommend k items based on source items
     */
    List<Integer> recommend(List<Integer> sourceItems, int num_recommendations);
}
