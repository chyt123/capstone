package ycheng.util;

import ycheng.database.LocalFileStorage;
import ycheng.model.IdSet;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/4/17.
 */
public class Normalizer {
    public Normalizer createItemSetByEachUser(List<String> stream, String desFile) {

        LocalFileStorage.clear(desFile);

        IdSet set = new IdSet(Integer.MAX_VALUE);
        stream.forEach(line -> processUserItem(set, line, desFile));
        LocalFileStorage.write(set, desFile);
        return this;
    }

    public Normalizer createUserSetEachItem(List<String> stream, String desFile) {

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

    public Normalizer createUserSetEachItemSorted(List<String> stream, String desFile) {

        LocalFileStorage.clear(desFile);
        Map<Integer, Set<Integer>> itemMap = new HashMap<>();
        stream.forEach(line -> processItemUser(itemMap, line));
        Comparator<IdSet> comparator = (o1, o2) -> o2.getSet().size() - o1.getSet().size();
        PriorityQueue<IdSet<Integer>> queue = new PriorityQueue<>(comparator);

        for (Map.Entry<Integer, Set<Integer>> entry : itemMap.entrySet()) {
            IdSet idSet = new IdSet(entry.getKey());
            idSet.setSet(entry.getValue());
            queue.add(idSet);
        }

        while (queue.size() > 0){
            LocalFileStorage.write(queue.poll(), desFile);
        }

        return this;
    }

    public Normalizer createItemDocumentByEachUser(List<String> stream, String desFile) {

        LocalFileStorage.clear(desFile);

        IdSet<Integer> set = new IdSet(Integer.MAX_VALUE);
        int i = getUserItemDocumentCount(set, stream);
        LocalFileStorage.write(i, desFile);
        stream.forEach(line -> processUserItemDocument(set, line, desFile));

        StringBuilder ss = new StringBuilder();
        for (Integer ii : set.getSet()) {
            ss.append(ii);
            ss.append(" ");
        }
        ss.deleteCharAt(ss.length() - 1);
        LocalFileStorage.writeByte(ss.toString().getBytes(), desFile);

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

    private static void processUserItemDocument(IdSet<Integer> set, String line, String type) {
        String[] s = line.split(",");
        int userId = Integer.valueOf(s[0]);
        int itemId = Integer.valueOf(s[2]);

        if (set.getId() != userId) {
            if (set.getId() != Integer.MAX_VALUE) {
                StringBuilder ss = new StringBuilder();
                if (set.getSet().size() > 5) {
                    for (Integer i : set.getSet()) {
                        ss.append(i);
                        ss.append(" ");
                    }
                    ss.deleteCharAt(ss.length() - 1);
                    LocalFileStorage.writeByte((ss.toString() + "\n").getBytes(), type);
                }
            }
            set.setId(userId);
            set.getSet().clear();
        }
        set.getSet().add(itemId);
    }

    private static int getUserItemDocumentCount(IdSet<Integer> set, List<String> lines) {
        int i = 0;
        for (String line : lines) {
            String[] s = line.split(",");

            int userId = Integer.valueOf(s[0]);
            int itemId = Integer.valueOf(s[2]);

            if (set.getId() != userId) {
                if (set.getId() != Integer.MAX_VALUE) {
                    if (set.getSet().size() > 5)
                        i++;
                }
                set.setId(userId);
                set.getSet().clear();
            }
            set.getSet().add(itemId);
        }
        return i + 1;
    }
}
