package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RAM {
    private Integer blockSize;
    private List<Cell> cells = new ArrayList<>();

    public RAM(Integer blockSize) {
        defineBlockSize(blockSize);
        fillCells();
    }

    private void defineBlockSize(Integer size) {
        this.blockSize = size;
    }

    private void fillCells() {
        for (int i = 0; i < 1024 * blockSize; i++) {
            cells.add(generateCell(i));
        }
    }

    private Cell generateCell(Integer id) {
        return new Cell(id);
    }

    public void show() {
        System.out.println(" _____________");
        System.out.println("|             |");
        System.out.println("|     RAM     |");
        System.out.println("|_____________|");
        cells.forEach(
                cell -> {
                    System.out.println(cell.toString());
                    if ((cell.getId() + 1) % blockSize == 0) {
                        System.out.println("|-------------|");
                    }
                }
        );
        System.out.println("|             |");
        System.out.println("|     END     |");
        System.out.println("|_____________|");
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public List<Cell> getCells() {
        return Collections.unmodifiableList(cells);
    }
}
