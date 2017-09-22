package ycheng.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ycheng.database.LocalFileStorage;
import ycheng.service.Application;
import ycheng.util.Normalizer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ycheng on 9/19/17.
 */
public class PopularityBased implements RecommendAlgorithm{
    private static ObjectMapper mapper = new ObjectMapper();
    @Override
    public void train(List<String> buyRecords, List<String> clickRecords) {
        Normalizer normalizer = new Normalizer();
        List<String> records = new LinkedList<>();
        for (String s : buyRecords) {
            records.add(s);
        }

        for (String s : clickRecords) {
            records.add(s);
        }
        normalizer.createUserSetEachItemSorted(records, LocalFileStorage.POPULARITY_ORDERED_ITEM);
    }

    @Override
    public List<Integer> recommend(List<Integer> sourceItems, int num_recommendations) {
        List<Integer> res = Application.getRecommender().recommendByPopularity(num_recommendations, sourceItems);
        if (res == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return res;
    }
}