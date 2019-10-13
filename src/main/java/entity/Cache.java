package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cache {
    private List<CacheRow> rows = new ArrayList<>();
    private Integer size;

    public Cache(Integer size) {
        this.size = size;
    }

    public Integer getSize() {
        return size;
    }

    public List<CacheRow> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
