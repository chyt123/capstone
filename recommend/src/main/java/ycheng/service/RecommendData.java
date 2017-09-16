package ycheng.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by ycheng on 9/16/17.
 */
@Getter
@Setter
@AllArgsConstructor
public class RecommendData {
    private String algo;
    private List<Integer> sourceData;
    private int recommendNum;
}
