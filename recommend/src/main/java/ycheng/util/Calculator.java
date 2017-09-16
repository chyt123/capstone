package ycheng.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import ycheng.database.LocalFileStorage;
import ycheng.model.IdSet;

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
    public Calculator calculateItemSim(String srcFile1, String srcFile2, String desFile) {
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
                            countMap.get(itemId).put(i, countMap.get(itemId).getOrDefault(i, 0)+1);
                            countMap.get(i).put(itemId, countMap.get(i).getOrDefault(itemId, 0)+1);
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
