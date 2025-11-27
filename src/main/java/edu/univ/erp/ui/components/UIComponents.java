package edu.univ.erp.ui.components;

import javax.swing.table.DefaultTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;


import javax.swing.*;
import java.awt.*;

public class UIComponents {

    public static final Color PRIMARY_BG = new Color(0x39AEA8);
    public static final Color SECONDARY_BG = new Color(64, 64, 64);
    public static final Color ACTIVE_BG = new Color(0x2563EB);

    public static final Color SIDEBAR_BG = new Color(0xF1F1F1);
    public static final Color SIDEBAR_BORDER = new Color(230, 230, 230);
    public static final Color SIDEBAR_ACTIVE_BG = new Color(0x2563EB);
    public static final Color SIDEBAR_ACTIVE_FG = Color.WHITE;
    public static final Color SIDEBAR_INACTIVE_BG = new Color(0xF1F1F1);
    public static final Color SIDEBAR_INACTIVE_FG = new Color(0x1A1A1A);
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color PRIMARY_BUTTON_BG = new Color(25, 118, 210);
    public static final Color PRIMARY_BUTTON_FG = Color.WHITE;

    // ----------------- buttons -----------------
    public static JButton primaryButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setFocusPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFont(
            new Font(
                "Segoe UI", 
                Font.PLAIN, 
                14
            )
        );

        return button;
    }

    public static JButton emojiButton(String text) {
        JButton button = new JButton(text);

        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(Color.WHITE);

        button.setFont(
            new Font(
                "Segoe UI Emoji", 
                Font.PLAIN, 
                16
            )
        );

        button.setBorder(
            BorderFactory.createEmptyBorder( 12, 18, 12, 10 )
        );

        button.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE, 
                48
            )
        );

        button.setAlignmentX(Component.LEFT_ALIGNMENT);

        return button;
    }

    public static JLabel profileBubble(String letter, Color bg) {
        JLabel bubble = new JLabel(letter, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
                );
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(40, 40);
            }
        };

        bubble.setOpaque(false);
        bubble.setForeground(Color.WHITE);

        bubble.setFont(new Font("Segoe UI", Font.BOLD, 15));
        bubble.setHorizontalAlignment(SwingConstants.CENTER);

        bubble.setBackground(bg);

        return bubble;
    }

    public static JPanel buttonBar(JButton... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        p.setBackground(Color.WHITE);

        for (JButton b : buttons) p.add(b);
        return p;
    }

    // ----------------- panels -----------------
    public static JPanel panel() {
        JPanel p = new JPanel();
        p.setBackground(PANEL_BG);
        return p;
    }

    public static JPanel cardPanel() {
        JPanel card = new JPanel();

        card.setBackground(Color.WHITE);
        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(230, 230, 230)
                ),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
            )
        );

        return card;
    }

    public static JPanel topActionBar(JComponent... components) {
        JPanel bar = new JPanel(
            new FlowLayout(
                FlowLayout.LEFT, 
                10, 
                10
            )
        );

        bar.setBackground(Color.WHITE);

        for (JComponent c : components) {
            bar.add(c);
        }

        return bar;
    }

    public static JTable table(DefaultTableModel model) {
        JTable tabel = new JTable(model);
        tabel.setBackground(Color.WHITE);
        tabel.setForeground(SIDEBAR_INACTIVE_FG);

        JTableHeader header = tabel.getTableHeader();
        header.setOpaque(true);
        header.setBackground(SIDEBAR_BG);
        header.setForeground(SIDEBAR_INACTIVE_FG);

        tabel.setAutoCreateRowSorter(true);
        return tabel;
    }
    
    public static JPanel grid(int columns, JComponent... items) {
        JPanel p = new JPanel(new GridLayout(0, columns, 12, 8));
        p.setBackground(PANEL_BG);

        for (JComponent c : items) {
            p.add(c);
        }
        return p;
    }
    

    // ----------------- inputs -----------------
    public static JTextField textField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    public static JTextField weightField(String defaultValue) {
        JTextField f = new JTextField(defaultValue, 4);
        styleField(f);

        ((AbstractDocument) f.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String str, AttributeSet a)
                    throws BadLocationException {
                if (str.matches("\\d*")) super.insertString(fb, offset, str, a);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String str, AttributeSet a)
                    throws BadLocationException {
                if (str.matches("\\d*")) super.replace(fb, offset, length, str, a);
            }
        });

        f.setPreferredSize(new Dimension(50, 30));
        return f;
    }

    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        styleField(field);
        return field;
    }

    public static JTextField searchField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(260, 28));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(Color.WHITE);

        field.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
            )
        );

        field.setLayout(new BorderLayout());
        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        icon.setForeground(UIComponents.SIDEBAR_INACTIVE_FG);

        field.add(icon, BorderLayout.EAST);

        field.putClientProperty("JTextField.placeholderText", "Search...");

        return field;
    }

    // ----------------- labels-----------------
    public static JLabel maintenanceLabel() {
        JLabel label = new JLabel();

        label.setFont(textField().getFont());
        label.setForeground(
            new Color(90, 90, 90)
        );

        label.setOpaque(true);
        label.setBackground(
            new Color(245, 245, 245)
        );

        label.setBorder(
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        );

        return label;
    }

    public static JLabel titleLabel(String text, Color color) {
        JLabel label = new JLabel(
            text, 
            SwingConstants.CENTER
        );

        label.setForeground(color);
        label.setFont(
            new Font(
                "Segoe UI", 
                Font.BOLD, 
                18
            )
        );

        return label;
    }

    public static JPanel labeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(
            new BorderLayout(5, 5)
        );

        panel.setBackground(PANEL_BG);
        JLabel label = new JLabel(labelText);

        label.setFont(
            new Font(
                "Segoe UI", 
                Font.PLAIN, 
                14
            )
        );

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    public static JPanel labelled(String text, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        p.setBackground(PANEL_BG);

        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        p.add(l);
        p.add(field);
        return p;
    }

    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        l.setForeground(new Color(50, 50, 50));
        return l;
    }


    // ----------------- helpers -----------------
    private static void styleField(JComponent field) {
        field.setBackground(PANEL_BG);

        field.setFont(
            new Font(
                "Segoe UI", 
                Font.PLAIN, 
                14
            )
        );

        field.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(200, 200, 200)
                ),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            )
        );
    }
}