package edu.univ.erp.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.regex.Pattern;

public class SearchFilter {
    public static void attachSearchFilter(JTextField field, JTable table, DefaultTableModel model) {
        field.addActionListener(e -> {
            String text = field.getText().trim();

            TableRowSorter<DefaultTableModel> sorter =
                    (TableRowSorter<DefaultTableModel>) table.getRowSorter();

            if (sorter == null) {
                sorter = new TableRowSorter<>(model);
                table.setRowSorter(sorter);
            }

            if (text.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(
                        RowFilter.regexFilter("(?i)" + Pattern.quote(text))
                );
            }
        });
    }
}