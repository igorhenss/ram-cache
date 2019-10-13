package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cache {
    private List<Row> rows = new ArrayList<>();
    private Integer size;

    public Cache(Integer size) {
        this.size = size;
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public void addRow(Row row) {
        rows.add(row);
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
