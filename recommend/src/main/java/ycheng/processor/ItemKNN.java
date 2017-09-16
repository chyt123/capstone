package ycheng.processor;

import ycheng.database.LocalFileStorage;
import ycheng.model.BuyRecord;
import ycheng.model.ClickRecord;
import ycheng.service.Application;
import ycheng.util.Calculator;
import ycheng.util.Normalizer;
import ycheng.util.Recommender;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/16/17.
 */
public class ItemKNN implements RecommendAlgorithm{
    @Override
    public void train(Stream buyRecords, Stream clickRecords) {
        Normalizer normalizer = new Normalizer();
        normalizer.createItemSetByEachUser(buyRecords, LocalFileStorage.ITEM_SET_BY_USER_BUY);
        normalizer.createItemSetByEachUser(clickRecords, LocalFileStorage.ITEM_SET_BY_USER_CLICK);
        normalizer.createUserSetEachItem(buyRecords, LocalFileStorage.USER_SET_BY_ITEM_BUY);
        normalizer.createUserSetEachItem(clickRecords, LocalFileStorage.USER_SET_BY_ITEM_CLICK);

        Calculator calculator = new Calculator();
        calculator.calculateItemSim(LocalFileStorage.ITEM_SET_BY_USER_BUY,
                LocalFileStorage.USER_SET_BY_ITEM_BUY, LocalFileStorage.ITEM_SIMILARITY_BUY);
        calculator.calculateItemSim(LocalFileStorage.ITEM_SET_BY_USER_CLICK,
                LocalFileStorage.USER_SET_BY_ITEM_CLICK, LocalFileStorage.ITEM_SIMILARITY_CLICK);
    }

    @Override
    public List<Integer> recommend(List<Integer> sourceItems, int num_recommendations) {
        return Application.getRecommender().recommendBySimpleItemKNN(num_recommendations, sourceItems, 200);
    }
}
