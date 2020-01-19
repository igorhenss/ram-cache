package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Cache {
    private Integer hitCounter = 0;
    private Integer missCounter = 0;
    private List<Row> rows = new ArrayList<>();
    private Integer size;

    public Cache(Integer size) {
        this.size = size;
    }

    public void hit() {
        hitCounter++;
    }

    public void miss() {
        missCounter++;
    }

    public boolean contains(List<Cell> cells) {
        var cacheCells = getCacheCells();
        return cells.stream().anyMatch(cell -> contains(cacheCells, cell));
    }

    private boolean contains(List<Cell> cells, Cell cell) {
        return cells.stream().anyMatch(cacheCell -> cacheCell.getTag().equals(cell.getTag()));
    }

    public void hitRow(List<Cell> cells) {
        var rowToHit = findRowFrom(cells);
        rowToHit.hit();
    }

    public void hitRow(Row row) {
        row.hit();
    }

    public Row findRowFrom(List<Cell> cells) {
        return rows.stream()
                .filter(row -> row.contains(cells.get(0)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Row not found."));
    }

    private List<Cell> getCacheCells() {
        var cells = new ArrayList<Cell>();
        rows.forEach(row -> cells.addAll(row.getCells()));
        return cells;
    }

    public int getAmountOfRows() {
        return getRows().size();
    }

    public Row lfuRow() {
        var lfuRow = rows.get(0);
        for (Row cacheRow : rows) {
            if (cacheRow.getFrequency() < lfuRow.getFrequency()) {
                lfuRow = cacheRow;
            }
        }
        return lfuRow;
    }

    public Row fifoRow() {
        return getFifoRow(rows);
    }

    public Row fifoRow(List<Row> rows) {
        return getFifoRow(rows);
    }

    private Row getFifoRow(List<Row> rows) {
        var fifoRow = rows.get(0);
        for (Row cacheRow : rows) {
            if (cacheRow.getAddedTime().isBefore(fifoRow.getAddedTime())) {
                fifoRow = cacheRow;
            }
        }
        return fifoRow;
    }

    public Row lruRow() {
        var lruRow = rows.get(0);
        for (Row cacheRow : rows) {
            if (cacheRow.getLastUseTime().isBefore(lruRow.getLastUseTime())) {
                lruRow = cacheRow;
            }
        }
        return lruRow;
    }

    public List<Row> getRowsWithFrequency(int frequency) {
        return rows.stream()
                .filter(row -> row.getFrequency().equals(frequency))
                .collect(Collectors.toList());
    }

    public List<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public void addRow(Integer tag, Row row) {
        var rowToAdd = new Row(tag, row);
        rows.add(rowToAdd);
    }

    public Integer getMaxAmountOfRows() {
        return size;
    }

    public Integer getHitCounter() {
        return hitCounter;
    }

    public Integer getMissCounter() {
        return missCounter;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public void setRow(Row row, Row target) {
        var targetIndex = rows.indexOf(target);
        rows.get(targetIndex).setRow(row);
    }

}
