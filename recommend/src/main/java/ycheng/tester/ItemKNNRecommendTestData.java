package ycheng.tester;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ycheng on 9/16/17.
 */
@Getter
@Setter
@AllArgsConstructor
public class ItemKNNRecommendTestData {
    private int recommendNum;
    private int simThreshold;
    private double sourceDataRatio;
    private int sourceDataMin;
    private int sourceDataMax;
}
