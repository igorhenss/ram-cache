package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Row {
    private Integer blockSize;
    private Integer frequency = 0;
    private List<Cell> cells = new ArrayList<>();
    private LocalDateTime addedTime = LocalDateTime.now();

    public Row(Integer blockSize) {
        this.blockSize = blockSize;
    }

    public void setCells(List<Cell> cells) {
        cells.forEach(this::addCell);
    }

    private void addCell(Cell cell) {
        if (cells.size() + 1 <= blockSize) {
            cells.add(cell);
        }
    }

    public Cell read(Integer tagToRead) {
        Optional<Cell> foundCell = cells.stream().filter(cell -> cell.tagFound(tagToRead)).findAny();
        if (foundCell.isEmpty()) {
            throw new IllegalArgumentException("Tag not found.");
        }
        frequency++;
        return foundCell.get();
    }

    public void write(Integer tagToWriteOn, String newValue) {
        cells.forEach(cell -> {
            if (cell.tagFound(tagToWriteOn)) {
                cell.setValue(newValue);
                frequency++;
            }
        });
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
}
