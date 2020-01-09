package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Cache {
    private List<Row> rows = new ArrayList<>();
    private List<Cell> celulas = new ArrayList<>();
    private Integer size;

    public Cache(Integer size) {
        this.size = size;
    }

    public boolean contains(List<Cell> cells) {
        var cacheCells = new ArrayList<Cell>();
        rows.forEach(row -> cacheCells.addAll(row.getCells()));
        return cells.stream().anyMatch(cell -> contains(cacheCells, cell));
    }

    private boolean contains(List<Cell> cells, Cell cell) {
        return cells.contains(cell);
    }

    public int getAmountOfRows() {
        return getRows().size();
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public void addRow(Row row) {
        rows.add(row);
    }

    public Integer getMaxAmountOfRows() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
