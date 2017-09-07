import com.fasterxml.jackson.databind.ObjectMapper;
import database.LocalFileStorage;
import model.IdSet;
import processor.*;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/4/17.
 */
public class TestPre {
    private static ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {
//        Normalizer normalizer = new Normalizer(Normalizer.CLICK);
//        normalizer
//                .createItemSetByEachUser()
//                .createUserSetEachItem()
//                .createTestItemSetByEachUser()
//                .next()
//                .calculateItemSim();


        test(12, 50, 0.5, 100000, 200000);
        test(12, 80, 0.5, 100000, 200000);
        test(12, 100, 0.5, 100000, 200000);


    }

    private static double test(int limit, int threshold, double sourceDataRatio, int testDataLimitMin, int testDataLimitMax) {
        Recommender recommender = new Recommender();

        try (Stream<String> stream = LocalFileStorage.read(LocalFileStorage.ITEM_SET_BY_USER_TEST)) {

            double s = stream
                    .map(line -> deserialize(line))
                    .filter(idSet -> dataFilter(idSet, testDataLimitMin, testDataLimitMax))
                    .mapToInt(idSet -> core(idSet, recommender, limit, sourceDataRatio, threshold))
                    .filter(i -> i >= 0)
                    .average()
                    .getAsDouble();
            System.out.println("Recommend Count:" + limit +
                            ", Buy item threshold:" + threshold +
                            ", SourceDataRatio:" + sourceDataRatio +
                            ", TestDataRange:[" + testDataLimitMin + "," + testDataLimitMax + "]");

            System.out.println("Recall:" + s / 100 + "%");
            System.out.println();
            return s;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
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

    private static int core(IdSet<Integer> idSet, Recommender recommender, int limit, double sourceDataRatio, int threshold) {
        int size = idSet.getSet().size();
        if (idSet.getSet().size() <= 1) return -1;
        int recall = recommender.recommendTest(idSet, limit, (int)(size * sourceDataRatio), threshold);
        return recall;
    }
}
