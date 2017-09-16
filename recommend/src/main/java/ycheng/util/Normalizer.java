package ycheng.util;

import ycheng.database.LocalFileStorage;
import ycheng.model.IdSet;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/4/17.
 */
public class Normalizer {
    public Normalizer createItemSetByEachUser(Stream<String> stream, String desFile) {

        LocalFileStorage.clear(desFile);

        IdSet set = new IdSet(Integer.MAX_VALUE);
        stream.forEach(line -> processUserItem(set, line, desFile));
        LocalFileStorage.write(set, desFile);
        return this;
    }

    public Normalizer createUserSetEachItem(Stream<String> stream, String desFile) {

        LocalFileStorage.clear(desFile);
        Map<Integer, Set<Integer>> itemMap = new HashMap<>();
        stream.forEach(line -> processItemUser(itemMap, line));

        for (Map.Entry<Integer, Set<Integer>> entry : itemMap.entrySet()) {
            IdSet idSet = new IdSet(entry.getKey());
            idSet.setSet(entry.getValue());
            LocalFileStorage.write(idSet, desFile);
        }

        return this;
    }

    private static void processUserItem(IdSet set, String line, String type) {
        String[] s = line.split(",");
        int userId = Integer.valueOf(s[0]);
        int itemId = Integer.valueOf(s[2]);

        if (set.getId() != userId) {
            if (set.getId() != Integer.MAX_VALUE) {
                LocalFileStorage.write(set, type);
            }
            set.setId(userId);
            set.getSet().clear();
        }
        set.getSet().add(itemId);
    }

    private static void processItemUser(Map<Integer, Set<Integer>> itemMap, String line) {
        String[] s = line.split(",");
        int userId = Integer.valueOf(s[0]);
        int itemId = Integer.valueOf(s[2]);

        if (!itemMap.containsKey(itemId)) {
            itemMap.put(itemId, new HashSet<>());
        }
        itemMap.get(itemId).add(userId);
    }
}
