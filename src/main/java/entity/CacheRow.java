package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CacheRow {
    private List<Cell> cells = new ArrayList<>();
    private Integer id;
    private Integer useFrequency;

    public CacheRow() {}

    public List<Cell> getCells() {
        return Collections.unmodifiableList(cells);
    }

    public Integer getUseFrequency() {
        return useFrequency;
    }
}
