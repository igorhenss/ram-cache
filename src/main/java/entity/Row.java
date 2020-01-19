package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Row {
    private Integer tagOnCache;
    private Integer blockSize;
    private Integer frequency = 0;
    private List<Cell> cells = new ArrayList<>();
    private LocalDateTime addedTime = LocalDateTime.now();
    private LocalDateTime lastUseTime = LocalDateTime.now();

    public Row(Integer tag, Row row) {
        tagOnCache = tag;
        blockSize = row.blockSize;
        frequency = row.frequency;
        row.cells.forEach(cell -> cells.add(createCell(cell)));
        addedTime = LocalDateTime.now();
        lastUseTime = LocalDateTime.now();
    }

    private Cell createCell(Cell cell) {
        var cellToCreate = new Cell(cell.getTag());
        cellToCreate.setValue(cell.getValue());
        return cellToCreate;
    }

    public Row(Integer blockSize) {
        this.blockSize = blockSize;
    }

    public boolean contains(Cell cell) {
        return cells.stream().anyMatch(rowCell -> rowCell.getTag().equals(cell.getTag()));
    }

    public void setCells(List<Cell> cells) {
        cells.forEach(this::addCell);
    }

    public void setRow(Row row) {
        cells.clear();
        setCells(row.getCells());
        addedTime = LocalDateTime.now();
        lastUseTime = LocalDateTime.now();
    }

    private void addCell(Cell cell) {
        if (cells.size() + 1 <= blockSize) {
            cells.add(createCell(cell));
        }
    }

    public void hit() {
        frequency++;
        lastUseTime = LocalDateTime.now();
    }

    public Integer getTagOnCache() {
        return tagOnCache;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public LocalDateTime getAddedTime() {
        return addedTime;
    }

    public LocalDateTime getLastUseTime() {
        return lastUseTime;
    }

}
