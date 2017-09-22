package ycheng.tester;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ycheng.database.LocalFileStorage;
import ycheng.model.IdSet;
import ycheng.service.Application;
import ycheng.util.Normalizer;
import ycheng.util.Recommender;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/19/17.
 */
public class PopularityBasedTester implements AlgorithmTester {
    private static ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    @Override
    public void preprocess(String srcFile) {
        if (LocalFileStorage.isExist(LocalFileStorage.ITEM_SET_BY_USER_TEST))
            return;
        Normalizer normalizer = new Normalizer();
        try (Stream<String> stream = LocalFileStorage.read(srcFile)){
            List<String> list = Arrays.asList(stream.toArray(String[]::new));
            normalizer.createItemSetByEachUser(list, LocalFileStorage.ITEM_SET_BY_USER_TEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TestResult test(Map parameters) {
        Recommender recommender = Application.getRecommender();
        ItemKNNRecommendTestData data = mapper.convertValue(parameters, ItemKNNRecommendTestData.class);

        try (Stream<String> stream = LocalFileStorage.read(LocalFileStorage.ITEM_SET_BY_USER_TEST)) {

            int[] s = stream
                    .map(line -> deserialize(line))
                    .filter(idSet -> dataFilter(idSet, data.getSourceDataMin(), data.getSourceDataMax()))
                    .map(idSet -> core(idSet, recommender, data.getRecommendNum(), data.getSourceDataRatio(), data.getSimThreshold()))
                    .filter(i -> i != null)
                    .reduce(new int[]{0, 0, 0, 0}, (a, b) -> {
                        int[] ret = new int[4];
                        ret[0] = a[0] + b[0];
                        ret[1] = a[1] + b[1];
                        ret[2] = a[2] + b[2];
                        ret[3] = a[3] + 1;
                        return ret;
                    });

            TestResult t = new TestResult();
            t.setRecall((double) s[0] / (double) s[3] / 10000.0);
            t.setPrecision((double) s[1] / (double) s[3] / 10000.0);
            t.setProcessTime((double)s[2] / (double)s[3]);
            return t;

        } catch (IOException e) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean dataFilter(IdSet idSet, int testDataLimitMin, int testDataLimitMax) {
        if (idSet == null || idSet.getId() < testDataLimitMin || idSet.getId() > testDataLimitMax) {
            return false;
        } else {
            return true;
        }
    }

    private static IdSet<Integer> deserialize(String line) {
        IdSet<Integer> idSet = null;
        try {
            idSet = mapper.readValue(line, IdSet.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return idSet;
    }

    private static int[] core(IdSet<Integer> idSet, Recommender recommender, int limit, double sourceDataRatio, int threshold) {
        int size = idSet.getSet().size();
        if (idSet.getSet().size() <= 1) return null;
        int[] stat = recommender.recommendTest(idSet, limit, (int) (size * sourceDataRatio), threshold, "popularity");
        return stat;
    }
}
