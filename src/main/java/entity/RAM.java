package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RAM {
    private List<Cell> cells = new ArrayList<>();

    public RAM(Integer blockSize) {
        fillCells(blockSize);
    }

    public boolean contains(List<Cell> cells) {
        return cells.stream().anyMatch(this::contains);
    }

    private boolean contains(Cell cell) {
        return cells.contains(cell);
    }

    private void fillCells(Integer blockSize) {
        for (int i = 0; i < 1024 * blockSize; i++) {
            cells.add(generateCell(i));
        }
    }

    private Cell generateCell(Integer id) {
        return new Cell(id);
    }

    public List<Cell> getCells() {
        return Collections.unmodifiableList(cells);
    }
}
