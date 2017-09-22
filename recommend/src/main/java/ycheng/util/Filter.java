package ycheng.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import ycheng.database.LocalFileStorage;
import ycheng.model.IdSet;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/6/17.
 */
public class Filter {
    private Map<Integer, Map<Integer, Integer>> map;
    private Map<Integer, Map<Integer, Integer>> clickMap;
    private List<IdSet> orderedList = new ArrayList<>();
    private Map<Integer, LinkedHashMap<Integer, Double>> topicMap = new HashMap<>();
    public ObjectMapper mapper = new ObjectMapper();
    public Filter(boolean lda) {
        JavaType valueType = mapper.getTypeFactory().constructMapType(Map.class, Integer.class, Integer.class);
        JavaType type = mapper.getTypeFactory().constructMapType(Map.class, mapper.constructType(Integer.class), valueType);

        if (!lda) {
            try (Stream stream = LocalFileStorage.read(LocalFileStorage.ITEM_SIMILARITY_BUY)) {
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

            try (Stream<String> stream = LocalFileStorage.read(LocalFileStorage.POPULARITY_ORDERED_ITEM)) {
                stream.forEach(line -> {
                    try {
                        IdSet d = mapper.readValue(line, IdSet.class);
                        orderedList.add(d);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        try (Stream<String> stream = LocalFileStorage.read(LocalFileStorage.TOPIC_FILE)) {
            String[] ss = stream.toArray(String[]::new);
            int topicNum = -1;
            LinkedHashMap<Integer, Double> m = new LinkedHashMap<>();
            for (String s : ss) {
                if (s.startsWith("Topic")) {
                    if (topicNum >= 0)
                        topicMap.put(topicNum, m);
                    topicNum++;
                    m = new LinkedHashMap<>();
                } else {
                    String[] t = s.trim().split(" ");
                    m.put(Integer.parseInt(t[0]), Double.parseDouble(t[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Double> getTopItemByTopic(int topicId, int limit) {
        LinkedHashMap<Integer, Double> m = topicMap.get(topicId);
        Map<Integer, Double> res = new HashMap<>();
        int i = 0;
        for (Map.Entry<Integer, Double> mm : m.entrySet()) {
            if (i >=limit) {
                break;
            }
            res.put(mm.getKey(), mm.getValue());
            i++;
        }
        return res;
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

    public List<Integer> getTopPopularityItem(int limit) {
        List<Integer> res = new LinkedList<>();
        for (int i = 0; i < limit && i < orderedList.size(); i++) {
            res.add(orderedList.get(i).getId());
        }
        return res;
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
