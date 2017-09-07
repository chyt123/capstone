package processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.LocalFileStorage;
import model.IdSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/4/17.
 */
public class Calculator {
    public static ObjectMapper mapper = new ObjectMapper();
    private String type;
    public Calculator (String type) {
        this.type = type;
    }
    public Calculator calculateItemSim() {
        String srcFile1;
        String srcFile2;
        String desFile;
        if (Normalizer.BUY.equals(type)) {
            srcFile1 = LocalFileStorage.ITEM_SET_BY_USER;
            srcFile2 = LocalFileStorage.USER_SET_BY_ITEM;
            desFile = LocalFileStorage.ITEM_SIMILARITY;
        } else if (Normalizer.CLICK.equals(type)) {
            srcFile1 = LocalFileStorage.ITEM_SET_BY_USER_CLICK;
            srcFile2 = LocalFileStorage.USER_SET_BY_ITEM_CLICK;
            desFile = LocalFileStorage.ITEM_SIMILARITY_CLICK;
        } else {
            return this;
        }

        LocalFileStorage.clear(desFile);
        Map<Integer, Map<Integer, Integer>> map1 = getItemIntersection(srcFile1);
        Map<Integer, Integer> map2 = getItemCount(srcFile2);
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : map1.entrySet()) {
            for (Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                int inter = entry.getKey() == entry2.getKey() ? map1.get(entry.getKey()).get(entry2.getKey()) / 2 : map1.get(entry.getKey()).get(entry2.getKey());
                int sum = map2.get(entry.getKey()) + map2.get(entry2.getKey());
                int res = (int)((double)inter / (double)(sum - inter) * 10000);
                map1.get(entry.getKey()).put(entry2.getKey(), res);
            }
        }
        LocalFileStorage.write(map1, desFile);
        return this;
    }

    private static Map<Integer, Map<Integer, Integer>> getItemIntersection(String srcFile) {
        Map<Integer, Map<Integer, Integer>> countMap = new HashMap<>();
        try (Stream stream = LocalFileStorage.read(srcFile)) {
            stream.forEach(line -> {
                try {
                    IdSet<Integer> idSet = mapper.readValue((String)line, IdSet.class);
                    LinkedList<Integer> list = new LinkedList<>();
                    for (Integer i : idSet.getSet()) {
                        list.add(i);
                        if (countMap.get(i) == null) {
                            countMap.put(i, new HashMap<>());
                        }
                        for (Integer itemId : list) {
                            countMap.get(itemId).put(i, countMap.get(itemId).get(i) == null ? 1 : countMap.get(itemId).get(i) + 1);
                            countMap.get(i).put(itemId, countMap.get(i).get(itemId) == null ? 1 : countMap.get(i).get(itemId) + 1);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return countMap;
    }

    private Map<Integer, Integer> getItemCount(String srcFile) {
        Map<Integer, Integer> count = new HashMap<>();
        try (Stream stream = LocalFileStorage.read(srcFile)) {
            stream.forEach(line -> {
                try {
                    IdSet idSet = mapper.readValue((String)line, IdSet.class);
                    count.put(idSet.getId(), idSet.getSet().size());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
}
