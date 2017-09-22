package ycheng.processor;

import jgibblda.Inferencer;
import jgibblda.Model;
import ycheng.database.LocalFileStorage;
import ycheng.service.Application;
import ycheng.util.Calculator;
import ycheng.util.Normalizer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ycheng on 9/19/17.
 */
public class LDA implements RecommendAlgorithm{
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
        normalizer.createItemDocumentByEachUser(records, LocalFileStorage.ITEM_DOCUMENTS);

    }

    @Override
    public List<Integer> recommend(List<Integer> sourceItems, int num_recommendations) {
        List<Integer> res = Application.getRecommender().recommendByLDA(num_recommendations, sourceItems);
        if (res == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        return res;
    }
}
