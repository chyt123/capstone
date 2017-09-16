package ycheng.util;

import ycheng.model.IdSet;
import java.util.*;

/**
 * Created by ycheng on 9/6/17.
 */
public class Recommender {
    private Filter filter = new Filter();

    public int[] recommendTest(IdSet<Integer> idSet, int limit, int sampleCount, int threshold) {

        List<Integer> sampleItems = new LinkedList<>();
        for (Integer sample : idSet.getSet()) {
            sampleItems.add(sample);
            if (sampleItems.size() == sampleCount)
                break;
        }

        Date start = new Date();
        List<Integer> rec = recommendBySimpleItemKNN(limit, sampleItems, threshold);
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
