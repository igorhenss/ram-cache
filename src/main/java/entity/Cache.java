package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Cache {
    private List<Row> rows = new ArrayList<>();
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

    public Row lfuRow() {
        var sortedRows = rows;
        var comparator = lfuComparator();
        sortedRows.sort(comparator);
        return sortedRows.get(0);
    }

    public List<Row> getRowsWithFrequency(int frequency) {
        return rows.stream()
                .filter(row -> row.getFrequency().equals(frequency))
                .collect(Collectors.toList());
    }

    public Row getFifoRow(List<Row> rows) {
        var comparator = fifoComparator();
        rows.sort(comparator);
        return rows.get(0);
    }

    private Comparator<Row> lfuComparator() {
        return Comparator.comparing(Row::getFrequency);
    }

    private Comparator<Row> fifoComparator() {
        return Comparator.comparing(Row::getAddedTime);
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
