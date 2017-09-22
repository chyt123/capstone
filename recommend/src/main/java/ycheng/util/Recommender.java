package ycheng.util;

import jgibblda.Inferencer;
import jgibblda.LDACmdOption;
import jgibblda.Model;
import ycheng.database.LocalFileStorage;
import ycheng.model.IdSet;
import ycheng.service.Application;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by ycheng on 9/6/17.
 */
public class Recommender {
    private Filter filter;
    private static Inferencer inferencer;

    public Recommender(boolean lda) {
        if (lda) {
            LDACmdOption ldaOption = new LDACmdOption();
            ldaOption.inf = true;
            ldaOption.dir = "data/";
            ldaOption.modelName = LocalFileStorage.MODEL_NAME;
            ldaOption.niters = 200;
            inferencer = new Inferencer();
            inferencer.init(ldaOption);
        }

        filter = new Filter(lda);
    }

    public static Inferencer getInferencer() {
        return inferencer;
    }


    public int[] recommendTest(IdSet<Integer> idSet, int limit, int sampleCount, int threshold, String type) {

        List<Integer> sampleItems = new LinkedList<>();
        for (Integer sample : idSet.getSet()) {
            sampleItems.add(sample);
            if (sampleItems.size() == sampleCount)
                break;
        }

        Date start = new Date();
        List<Integer> rec = null;
        if (type.equalsIgnoreCase("itemknn"))
            rec = recommendBySimpleItemKNN(limit, sampleItems, threshold);
        else if (type.equalsIgnoreCase("popularity"))
            rec = recommendByPopularity(limit, sampleItems);
        else if (type.equalsIgnoreCase("lda"))
            rec = recommendByLDA(limit, sampleItems);
        Date end = new Date();
        if (rec == null) {
            return null;
        }

        for (Integer i : sampleItems) {
            idSet.getSet().remove(i);
        }

        if (idSet.getSet().size() <= 0) {
            System.out.println("Test case is not enough, please decrease the sampleCount");
            return null;
        }

        int truePositive = 0;
        for (Integer i : idSet.getSet()) {
            if (rec.contains(i)) {
                truePositive ++;
            }
        }

        int[] res = new int[3];
        res[0] = (int)((double)truePositive/(double)idSet.getSet().size() * 10000);
        res[1] = (int)((double)truePositive/(double)limit * 10000);
        res[2] = (int)(end.getTime() - start.getTime());

        return res;
    }

    public List<Integer> recommendByLDA(int limit, List<Integer> sampleItems) {
        String s = "";
        for(Integer i : sampleItems) {
            s += i;
            s += " ";
        }
        s = s.substring(0, s.length() - 1);
        String[] ss = new String[1];
        ss[0] = s;
        Model m = inferencer.inference(ss);
        m.saveModel("sample-data");

        String st;
        try(Stream<String> stream = LocalFileStorage.read("sample-data.tassign")) {
            st = stream.findFirst().get();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        String[] sa = st.split(" ");
        Map<Integer, Integer> topicCount = new HashMap<>();
        for (String saa : sa) {
            String[] saaa = saa.split(":");
            int topic = Integer.parseInt(saaa[1]);
            int tmp = topicCount.getOrDefault(topic, 0);
            topicCount.put(topic, tmp+1);
        }


        Map<Integer, Double> all = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : topicCount.entrySet()) {
            Map<Integer, Double> mmm = filter.getTopItemByTopic(entry.getKey(), limit * 2);
            for (Map.Entry<Integer, Double> entry2 : mmm.entrySet()) {
                if (sampleItems.contains(entry2.getKey())) continue;
                double d = all.getOrDefault(entry2.getKey(), 0.0);
                all.put(entry2.getKey(), d + entry2.getValue() * (double)entry.getValue());

            }
        }


        Comparator<Map.Entry<Integer, Double>> comparator = (o1, o2) -> {
            if ((o1.getValue() - o2.getValue()) >= 0) {
                return 1;
            } else {
                return -1;
            }
        };

        Stack<Integer> res = new Stack<>();

        PriorityQueue<Map.Entry<Integer, Double>> prq = new PriorityQueue<>(comparator);

        for (Map.Entry<Integer, Double> entry2 : all.entrySet()) {
            prq.add(entry2);
            if (prq.size() > limit) {
                prq.remove();
            }
        }

        while (!prq.isEmpty()) {
            Map.Entry<Integer, Double> e = prq.poll();
            res.push(e.getKey());
        }

        List<Integer> list = new LinkedList<>();
        list.addAll(res);
        return list;
    }

    public List<Integer> recommendByPopularity(int limit, List<Integer> sampleItems) {
        return filter.getTopPopularityItem(limit);
    }

    public List<Integer> recommendBySimpleItemKNN(int limit, List<Integer> sampleItems, int threshold) {
        List<Integer> targetItems = new LinkedList<>();
        Comparator<Map.Entry<Integer, Integer>> comparator = (o1, o2) -> o1.getValue() - o2.getValue();
        for (Integer i : sampleItems) {
            Map<Integer, Integer> simMap = filter.getTopSimilarCombineItem(i, limit * 2, threshold);
            if (simMap == null) {
                System.out.println("Cannot find related similar set of " + i);
                return null;
            }
            targetItems.addAll(simMap.keySet());
        }

        Map<Integer, Integer> targetItemMap = new HashMap<>();

        Stack<Integer> res = new Stack<>();

        for (Integer i : targetItems) {
            Map<Integer, Integer> simMap = filter.getTopSimilarCombineItem(i, limit * 2, threshold);
            if (simMap == null) {
                System.out.println("Cannot find related similar set of " + i);
                return null;
            }
            int bought = 0;
            for (Integer j : simMap.keySet()) {
                if (sampleItems.contains(j)) {
                    bought += simMap.get(j);
                }
            }

            int all = 0;
            for (Integer j : simMap.keySet()) {
                all += simMap.get(j);
            }
            if (all != 0) {
                targetItemMap.put(i, (int)((double)bought/(double)all * 10000));
            } else {
                targetItemMap.put(i, 0);
            }
        }

        PriorityQueue<Map.Entry<Integer, Integer>> prq = new PriorityQueue<>(comparator);
        for (Map.Entry<Integer, Integer> entry2 : targetItemMap.entrySet()) {
            if (entry2.getValue() == 0) continue;
            if (sampleItems.contains(entry2.getKey())) continue;
            prq.add(entry2);
            if (prq.size() > limit) {
                prq.remove();
            }
        }
        while (!prq.isEmpty()) {
            Map.Entry<Integer, Integer> e = prq.poll();
            res.push(e.getKey());
        }

        List<Integer> list = new LinkedList<>();
        list.addAll(res);
        return list;
    }
}
