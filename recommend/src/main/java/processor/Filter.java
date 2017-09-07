package processor;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.LocalFileStorage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/6/17.
 */
public class Filter {
    private Map<Integer, Map<Integer, Integer>> map;
    private Map<Integer, Map<Integer, Integer>> clickMap;
    public ObjectMapper mapper = new ObjectMapper();
    public Filter() {
        JavaType valueType = mapper.getTypeFactory().constructMapType(Map.class, Integer.class, Integer.class);
        JavaType type = mapper.getTypeFactory().constructMapType(Map.class, mapper.constructType(Integer.class), valueType);

        try (Stream stream = LocalFileStorage.read(LocalFileStorage.ITEM_SIMILARITY)) {
            String s = (String) stream.findFirst().get();
            map = mapper.readValue(s, type);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Stream stream = LocalFileStorage.read(LocalFileStorage.ITEM_SIMILARITY_CLICK)) {
            String s = (String) stream.findFirst().get();
            clickMap = mapper.readValue(s, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Integer> getTopSimilarBuyItem(int itemId, int limit, int threshold) {
        Comparator<Map.Entry<Integer, Integer>> comparator = (o1, o2) -> o1.getValue() - o2.getValue();
        Map<Integer, Integer> res = new HashMap<>();
        Map<Integer, Integer> targetSet = map.get(itemId);
        if (targetSet == null) {
            return null;
        }
        PriorityQueue<Map.Entry<Integer, Integer>> prq = new PriorityQueue<>(comparator);
        for (Map.Entry<Integer, Integer> entry2 : targetSet.entrySet()) {
            if (entry2.getValue() <= 0) continue;
            if (entry2.getValue() < threshold) continue;
            if (entry2.getKey() == itemId) continue;
            prq.add(entry2);
            if (prq.size() > limit) {
                prq.remove();
            }
        }
        while (!prq.isEmpty()) {
            Map.Entry<Integer, Integer> e = prq.poll();
            res.put(e.getKey(), e.getValue());
        }
        return res;
    }

    public Map<Integer, Integer> getTopSimilarClickItem(int itemId, int limit, int threshold) {
        Comparator<Map.Entry<Integer, Integer>> comparator = (o1, o2) -> o1.getValue() - o2.getValue();
        Map<Integer, Integer> res = new HashMap<>();
        Map<Integer, Integer> targetSet = clickMap.get(itemId);
        if (targetSet == null) {
            return null;
        }
        PriorityQueue<Map.Entry<Integer, Integer>> prq = new PriorityQueue<>(comparator);
        for (Map.Entry<Integer, Integer> entry2 : targetSet.entrySet()) {
            if (entry2.getValue() == 0) continue;
            if (entry2.getValue() < threshold) continue;
            if (entry2.getKey() == itemId) continue;
            prq.add(entry2);
            if (prq.size() > limit) {
                prq.remove();
            }
        }
        while (!prq.isEmpty()) {
            Map.Entry<Integer, Integer> e = prq.poll();
            res.put(e.getKey(), e.getValue());
        }
        return res;
    }

    public Map<Integer, Integer> getTopSimilarCombineItem(int itemId, int limit, int threshold) {
        Map<Integer, Integer> map = getTopSimilarBuyItem(itemId, limit, threshold);
        if (map != null) {
            if (map.size() < limit) {
                //System.out.println("Item " + itemId + ": " + map.size() + " from buy among " + limit + " items");
                Map<Integer, Integer> map2 = getTopSimilarClickItem(itemId, limit - map.size(), -1);
                if (map2 != null) {
                    map.putAll(map2);
                }
            }
        } else {
            map = getTopSimilarClickItem(itemId, limit * 2, -1);
        }

        return map;
    }



    public int getBuyItemSimilarity(int itemIdA, int itemIdB) {
        int res = 0;
        Map<Integer, Integer> temp = map.get(itemIdA);
        if (temp == null) {
            return -1;
        }
        res = map.get(itemIdA).get(itemIdB);
        return res;
    }

    public int getClickItemSimilarity(int itemIdA, int itemIdB) {
        int res = 0;
        Map<Integer, Integer> temp = clickMap.get(itemIdA);
        if (temp == null) {
            return -1;
        }
        res = map.get(itemIdA).get(itemIdB);
        return res;
    }
}
