import entity.Cache;
import entity.Cell;
import entity.RAM;
import entity.Row;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Simulator {
    private static final Integer BLOCK_SIZE = 2;

    private JPanel simulator;

    private DefaultTableModel cacheModel  = createCacheModel();
    private DefaultTableModel ramModel  = createRamModel();
    private JScrollPane cacheScrollPane;
    private JScrollPane loggerScrollPane;
    private JScrollPane ramScrollPane;
    private JPanel replacementPoliciesPanel;
    private JRadioButton applyFIFO;
    private JRadioButton applyLFU;
    private JRadioButton applyLRU;
    private JTable cacheCells;
    private JTable ramCells;
    private JTextArea logger;
    private JTextArea hitCounter;
    private JTextArea missCounter;

    private RAM ram = new RAM(BLOCK_SIZE);
    private Cache cache;

    public Simulator() {
        removeFocus();

        startRamRows();
        startCacheRows();
        initializeCache();

        hitText();
        missText();

        cacheCells.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                var selectedRow = cacheCells.getSelectedRow();
                var selectedColumn = cacheCells.getSelectedColumn();
                var row = cache.getRows().get(selectedRow);
                var cell = row.getCells().get(selectedColumn - 1);

                var result = cacheCells.getValueAt(selectedRow, selectedColumn).toString();
                cell.setValue(result);

                hitEvent(selectedRow);
                cache.hitRow(getCacheRow(selectedRow));
            }
        });

        ramCells.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!applyLRU.isSelected() && !applyLFU.isSelected() && !applyFIFO.isSelected()) {
                    noReplacementPoliciesEvent();
                } else {
                    var selectedRow = ramCells.getSelectedRow();
                    var selectedCells = getRamCells(selectedRow);
                    var maxAmountOfRows = cache.getMaxAmountOfRows();
                    var amountOfRows = cache.getAmountOfRows();
                    var row = new Row(BLOCK_SIZE);

                    if (cache.contains(selectedCells)) {
                        hitEvent(selectedRow);
                        cache.hitRow(selectedCells);
                    } else {
                        row.setCells(selectedCells);
                        missEvent(selectedRow);
                        if (amountOfRows < maxAmountOfRows) {
                            cache.addRow(amountOfRows, row);
                            cacheModel.addRow(convertToVector(amountOfRows, row));
                        } else {
                            if (applyLFU.isSelected()) {
                                var rowToReplace = cache.lfuRow();
                                var lfuRows = cache.getRowsWithFrequency(rowToReplace.getFrequency());
                                if (applyFIFO.isSelected()) {
                                    if (hasMultipleLfu(lfuRows)) {
                                        rowToReplace = cache.fifoRow(lfuRows);
                                    }
                                }
                                replaceOnRam(rowToReplace);
                                replaceOnCache(row, rowToReplace, selectedRow);
                            } else if (applyLRU.isSelected()) {
                                var rowToReplace = cache.lruRow();
                                replaceOnRam(rowToReplace);
                                replaceOnCache(row, rowToReplace, selectedRow);
                            } else if (applyFIFO.isSelected()) {
                                var rowToReplace = cache.fifoRow();
                                replaceOnRam(rowToReplace);
                                replaceOnCache(row, rowToReplace, selectedRow);
                            }
                        }
                    }
                }
            }
        });

        applyFIFO.addItemListener(e -> {
            if (clickEvent(e)) {
                log(getTime());
                log("Turned on: FIFO.");
                unclick(applyLRU, applyLRU.getText());
                logLineBreak();
            }
        });

        applyLFU.addItemListener(e -> {
            if (clickEvent(e)) {
                log(getTime());
                log("Turned on: LFU.");
                unclick(applyLRU, applyLRU.getText());
                logLineBreak();
            }
        });

        applyLRU.addItemListener(e -> {
            if (clickEvent(e)) {
                log(getTime());
                log("Turned on: LRU.");
                unclick(applyFIFO, applyFIFO.getText());
                unclick(applyLFU, applyLFU.getText());
                logLineBreak();
            }
        });
    }

    private void replaceOnRam(Row rowToReplace) {
        var cellsToReplace = rowToReplace.getCells();
        cellsToReplace.forEach(cell -> {
            var ramCell = getCellByTag(cell.getTag());
            ramCell.setValue(cell.getValue());
        });
        resetRamRows();
    }

    private void replaceOnCache(Row rowToReplace, Row rowToBeReplaced, Integer ramRowTag) {
        cache.setRow(rowToReplace, rowToBeReplaced);
        replacementEvent(ramRowTag, rowToBeReplaced);
        resetCacheRows();
    }

    private Cell getCellByTag(int tag) {
        var cells = ram.getCells();
        return cells.stream()
                .filter(cell -> cell.getTag().equals(tag))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cell not found."));
    }

    private boolean hasMultipleLfu(List<Row> lfuRows) {
        return lfuRows.size() > 1;
    }

    private void noReplacementPoliciesEvent() {
        log(getTime());
        log("Nenhuma política de substituição selecionada.");
        logLineBreak();
    }

    private void hitEvent(Integer tag) {
        log(getTime());
        log("HIT on tag [" + tag + "]");
        cache.hit();
        hitText();
        resetRamRows();
        logLineBreak();
    }

    private void missEvent(Integer tag) {
        log(getTime());
        log("MISS on tag [" + tag + "]");
        cache.miss();
        missText();
        resetRamRows();
        logLineBreak();
    }

    private void replacementEvent(Integer ramRow, Row row) {
        log(getTime());
        log("RAM - Row [" + ramRow + "] selected.");
        log("Cache - Row [" + row.getTagOnCache() + "] replaced.");
        logLineBreak();
    }

    private void hitText() {
        hitCounter.setText("HIT: " + cache.getHitCounter());
    }

    private void missText() {
        missCounter.setText("MISS: " + cache.getMissCounter());
    }

    private void log(String message) {
        logger.append(message);
        logLineBreak();
    }

    private void logLineBreak() {
        logger.append("\n");
    }

    private List<Cell> getRamCells(Integer desiredRow) {
        var cells = new ArrayList<Cell>();
        var mod = desiredRow % BLOCK_SIZE;
        var start = desiredRow - mod;
        for (int i = start; i < start + BLOCK_SIZE; i++) {
            cells.add(ram.getCells().get(i));
        }
        return cells;
    }

    private Row getCacheRow(Integer desiredRow) {
        return cache.getRows().get(desiredRow);
    }

    private Vector convertToVector(Integer tag, Row row) {
        var cells = row.getCells();
        var vector = new Vector();
        vector.add(tag);
        cells.forEach(cell -> vector.add(cell.getValue()));
        return vector;
    }

    public static void main(String[] args) {
        Simulator sim = new Simulator();

        startSimulation(sim);
    }

    private String getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    private static void startSimulation(Simulator simulation) {
        JFrame frame = new JFrame("RAM-Cache Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(simulation.simulator);
        frame.setSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
    }

    private void removeFocus() {
        applyFIFO.setFocusable(false);
        applyLFU.setFocusable(false);
        applyLRU.setFocusable(false);
        ramCells.setFocusable(false);
    }

    private void startRamRows() {
        Vector<String> headers = new Vector<>();

        headers.add("Tag");
        headers.add("Value");

        ramModel.setColumnIdentifiers(headers);
        ramCells.setRowHeight(25);
        ramCells.setModel(ramModel);
        defineColumnProperties(ramCells);

        resetRamRows();
    }

    private void resetRamRows() {
        ramModel.setRowCount(0);
        setRamRows();
    }

    private void resetCacheRows() {
        cacheModel.setRowCount(0);
        setCacheRows();
    }

    private void setRamRows() {
        ram.getCells().forEach(cell ->
                ramModel.addRow(new Object[]{ cell.getTag(), cell.getValue() }));
    }

    private void setCacheRows() {
        var counter = 0;
        for (Row row : cache.getRows()) {
            cacheModel.addRow(convertToVector(counter, row));
            counter++;
        }
    }

    private void startCacheRows() {
        Vector<String> headers = new Vector<>();
        headers.add("Tag");
        for (int i = 0; i < BLOCK_SIZE; i++) {
            headers.add("Value");
        }

        cacheModel.setColumnIdentifiers(headers);
        cacheCells.setRowHeight(25);
        cacheCells.setModel(cacheModel);
        defineColumnProperties(cacheCells);
    }

    private void initializeCache() {
        cache = new Cache(ramCells.getRowCount() / 256);
    }

    private boolean clickEvent(ItemEvent e) {
        return e.getStateChange() == ItemEvent.SELECTED;
    }

    private boolean unclickEvent(ItemEvent e) {
        return e.getStateChange() == ItemEvent.DESELECTED;
    }

    private void unclick(JRadioButton button, String name) {
        if (button.isSelected()) {
            button.setSelected(false);
            log("Turned off: " + name + ".");
        }
    }

    private void click(JRadioButton button, String name) {
        if (!button.isSelected()) {
            button.setSelected(true);
            log("Turned on: " + name + ".");
        }
    }

    private void defineColumnProperties(JTable table) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        Color transparent = new Color(255, 255, 255, 0);

        renderer.setHorizontalAlignment(JLabel.CENTER);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(transparent);
        table.getTableHeader().setPreferredSize(new Dimension(-1, 33));
        table.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 22));

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private DefaultTableModel createRamModel() {
        return new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private DefaultTableModel createCacheModel() {
        return new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
    }

}
