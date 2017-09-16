package ycheng.database;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/4/17.
 */
public class LocalFileStorage {
    private Stream stream;
    public static ObjectMapper mapper = new ObjectMapper();
    public static String ITEM_SET_BY_USER_BUY = "item-set-by-user.dat";
    public static String ITEM_SET_BY_USER_CLICK = "item-set-by-user-click.dat";
    public static String USER_SET_BY_ITEM_BUY = "user-set-by-item.dat";
    public static String USER_SET_BY_ITEM_CLICK = "user-set-by-item-click.dat";
    public static String ITEM_SIMILARITY_BUY = "item-similarity.dat";
    public static String ITEM_SIMILARITY_CLICK = "item-similarity-click.dat";
    public static String ITEM_SET_BY_USER_TEST = "item-set-by-user-test.dat";
    public static String SOURCE_BUY_DATA = "yoochoose-buys.dat";
    public static String SOURCE_CLICK_DATA = "yoochoose-clicks.dat";
    public static String SOURCE_TEST_DATA = "yoochoose-test.dat";
    public static void write(Object idSet, String name) {
        String s = "";
        try {
            s = mapper.writeValueAsString(idSet);
            List<String> lines = Arrays.asList(s);
            Path file = Paths.get("data/" + name);
            file.toFile().createNewFile();
            Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clear(String name) {
        try {
            Path file = Paths.get("data/" + name);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isExist(String name) {
        Path file = Paths.get("data/" + name);
        return Files.exists(file);
    }

    public static Stream read(String name) throws IOException {
        return Files.lines(Paths.get("data/" + name));
    }
}
