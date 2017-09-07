package model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by ycheng on 9/4/17.
 */
public class IdSet<T> {
    private int id;
    private Set<T> set = new HashSet<>();

    public IdSet() {}

    public IdSet(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<T> getSet() {
        return set;
    }

    public void setSet(Set<T> set) {
        this.set = set;
    }
}
