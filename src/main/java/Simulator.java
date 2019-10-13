import entity.RAM;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

public class Simulator {
    private JPanel Simulator;
    private JRadioButton applyFIFO;
    private JRadioButton applyLFU;
    private JRadioButton applyLRU;
    private RAM ram = new RAM(2);
    private List<String> cellsIds = new ArrayList<>();
    private List<String> cellsValues = new ArrayList<>();
    private JList ramIds;
    private JList ramValues;
    private JScrollPane ramScrollPane;

    public Simulator() {
        applyFIFO.setFocusable(false);
        applyLFU.setFocusable(false);
        applyLRU.setFocusable(false);

        setRows();

        applyFIFO.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (clickEvent(e)) {
                    System.out.println("Applying FIFO.");
                    unclick(applyLRU, applyLRU.getText());
                    System.out.println();
                }
            }
        });

        applyLFU.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (clickEvent(e)) {
                    System.out.println("Applying LFU.");
                    unclick(applyLRU, applyLRU.getText());
                    System.out.println();
                }
            }
        });

        applyLRU.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (clickEvent(e)) {
                    System.out.println("Applying LRU.");
                    unclick(applyFIFO, applyFIFO.getText());
                    unclick(applyLFU, applyLFU.getText());
                    System.out.println();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("RAM-Cache Simulator");
        Simulator sim = new Simulator();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(sim.Simulator);
        frame.pack();
        frame.setVisible(true);
    }

    private boolean clickEvent(ItemEvent e) {
        return e.getStateChange() == ItemEvent.SELECTED;
    }

    private void unclick(JRadioButton button, String name) {
        button.setSelected(false);
        System.out.println("Turned off " + name + ".");
    }

    private void click(JRadioButton button, String name) {
        button.setSelected(true);
        System.out.println("Turned on " + name + ".");
    }

    private void setRows() {
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) ramValues.getCellRenderer();
        renderer.setHorizontalAlignment(JLabel.LEFT);

        ram.getCells().forEach(cell -> {
            cellsIds.add(cell.getId().toString());
            cellsValues.add(cell.getValue());
        });

        ramIds.setListData(cellsIds.toArray());
        ramIds.setVisibleRowCount(cellsIds.size());
        ramIds.setFixedCellHeight(50);
        ramIds.setFixedCellWidth(138);

        ramValues.setListData(cellsValues.toArray());
        ramValues.setVisibleRowCount(cellsValues.size());
        ramValues.setFixedCellHeight(50);
        ramValues.setFixedCellWidth(30);
    }
}
