package ycheng.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ycheng on 9/4/17.
 */
@Getter
@Setter
@AllArgsConstructor
public class IdSet<T> {
    private int id;
    private Set<T> set = new HashSet<>();

    public IdSet(){}

    public IdSet(int id) {
        this.id = id;
    }
}
