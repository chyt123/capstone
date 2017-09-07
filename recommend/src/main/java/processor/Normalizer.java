package processor;

import database.LocalFileStorage;
import model.IdSet;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/4/17.
 */
public class Normalizer {
    public static String BUY = "buy";
    public static String CLICK = "click";

    private String type;
    private Calculator calculator;

    public Normalizer(String type) {
        this.type = type;
        calculator = new Calculator(type);
    }

    public Calculator next() {
        return calculator;
    }

    public Normalizer createItemSetByEachUser() {
        String desFile;
        String srcFile;
        if (BUY.equals(type)) {
            desFile = LocalFileStorage.ITEM_SET_BY_USER;
            srcFile = LocalFileStorage.SOURCE_BUY_DATA;
        } else if (CLICK.equals(type)) {
            desFile = LocalFileStorage.ITEM_SET_BY_USER_CLICK;
            srcFile = LocalFileStorage.SOURCE_CLICK_DATA;
        } else {
            return this;
        }

        LocalFileStorage.clear(desFile);

        IdSet set = new IdSet(Integer.MAX_VALUE);
        try (Stream<String> stream = LocalFileStorage.read(srcFile)) {
            stream.forEach(line -> processUserItem(set, line, desFile));
            LocalFileStorage.write(set, desFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public Normalizer createTestItemSetByEachUser() {
        LocalFileStorage.clear(LocalFileStorage.ITEM_SET_BY_USER_TEST);
        IdSet set = new IdSet(Integer.MAX_VALUE);
        try (Stream<String> stream = LocalFileStorage.read(LocalFileStorage.SOURCE_TEST_DATA)) {
            stream.forEach(line -> processUserItem(set, line, LocalFileStorage.ITEM_SET_BY_USER_TEST));
            LocalFileStorage.write(set, LocalFileStorage.ITEM_SET_BY_USER_TEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Normalizer createUserSetEachItem() {
        String desFile;
        String srcFile;
        if (BUY.equals(type)) {
            desFile = LocalFileStorage.USER_SET_BY_ITEM;
            srcFile = LocalFileStorage.SOURCE_BUY_DATA;
        } else if (CLICK.equals(type)) {
            desFile = LocalFileStorage.USER_SET_BY_ITEM_CLICK;
            srcFile = LocalFileStorage.SOURCE_CLICK_DATA;
        } else {
            return this;
        }

        LocalFileStorage.clear(desFile);
        Map<Integer, Set<Integer>> itemMap = new HashMap<>();
        try (Stream<String> stream = LocalFileStorage.read(srcFile)) {
            stream.forEach(line -> processItemUser(itemMap, line));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
