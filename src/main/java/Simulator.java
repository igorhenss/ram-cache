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

        setRamRows();
        setCacheRows();
        initializeCache();

        hitText();
        missText();

        cacheCells.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                var selectedRow = cacheCells.getSelectedRow();
                var selectedColumn = cacheCells.getSelectedColumn();
                var row = cache.getRows().get(selectedRow);
                var cell = row.getCells().get(selectedColumn - 1);

                var result = cacheCells.getValueAt(selectedRow, selectedColumn).toString();
                cell.setValue(result);
            }
        });

        cacheCells.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var selectedRow = cacheCells.getSelectedRow();
                var selectedColumn = cacheCells.getSelectedColumn();
                var row = cache.getRows().get(selectedRow);
                var cell = row.getCells().get(selectedColumn - 1);
            }
        });

        ramCells.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                var selectedRow = ramCells.getSelectedRow();
                var selectedCells = getRamCells(selectedRow);
                var maxAmountOfRows = cache.getMaxAmountOfRows();
                var amountOfRows = cache.getAmountOfRows();

                if (cache.contains(selectedCells)) {
                    hitEvent();
                } else {
                    var row = new Row(BLOCK_SIZE);
                    row.setCells(selectedCells);
                    if (amountOfRows < maxAmountOfRows) {
                        missEvent();
                        cache.addRow(row);
                        cacheModel.addRow(convertToVector(amountOfRows, row));
                    } else {
                        // REPLACEMENT
                        if (applyLFU.isSelected()) {
                            var lfuRow = cache.lfuRow();
                            if (applyFIFO.isSelected()) {
                                if (hasMultipleLfu(lfuRow, cache.getRows())) {
                                    var lfuRows = cache.getRowsWithFrequency(lfuRow.getFrequency());
                                    var fifoLfuRow = cache.getFifoRow(lfuRows);
                                    replaceOnRam(fifoLfuRow);
                                }
                            }
                        }
                    }
                }
            }
        });

        applyFIFO.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (clickEvent(e)) {
                    log(getTime());
                    log("Turned on: FIFO.");
                    unclick(applyLRU, applyLRU.getText());
                    logLineBreak();
                }
            }
        });

        applyLFU.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (clickEvent(e)) {
                    log(getTime());
                    log("Turned on: LFU.");
                    unclick(applyLRU, applyLRU.getText());
                    logLineBreak();
                }
            }
        });

        applyLRU.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (clickEvent(e)) {
                    log(getTime());
                    log("Turned on: LRU.");
                    unclick(applyFIFO, applyFIFO.getText());
                    unclick(applyLFU, applyLFU.getText());
                    logLineBreak();
                }
            }
        });
    }

    private void replaceOnRam(Row rowToReplace) {
        var cellsToReplace = rowToReplace.getCells();
        cellsToReplace.forEach(cell -> {
            var ramCell = getCellByTag(cell.getTag());
            ramCell.setValue(cell.getValue());
        });
    }

    private Cell getCellByTag(int tag) {
        var cells = ram.getCells();
        return cells.stream()
                .filter(cell -> cell.getTag().equals(tag))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cell not found."));
    }

    private boolean hasMultipleLfu(Row lfuRow, List<Row> rows) {
        return rows.stream()
                .anyMatch(row -> row.getFrequency().equals(lfuRow.getFrequency()) && !row.equals(lfuRow));
    }

    private void hitEvent() {
        log(getTime());
        log("HIT");
        cache.hit();
        hitText();
        logLineBreak();
    }

    private void missEvent() {
        log(getTime());
        log("MISS");
        cache.miss();
        missText();
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

    private void setRamRows() {
        Vector<String> headers = new Vector<>();

        headers.add("Tag");
        headers.add("Value");

        ramModel.setColumnIdentifiers(headers);
        ramCells.setRowHeight(25);
        ramCells.setModel(ramModel);
        defineColumnProperties(ramCells);

        ram.getCells().forEach(cell ->
                ramModel.addRow(new Object[]{ cell.getTag(), cell.getValue() }));
    }

    private void setCacheRows() {
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
