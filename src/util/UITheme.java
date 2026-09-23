package util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Design system and UI theme utilities for a clean, modern desktop interface.
 */
public class UITheme {

    // Primary Travel Theme Palette
    public static final Color PRIMARY = new Color(16, 86, 143);         // Deep Ocean Blue
    public static final Color PRIMARY_DARK = new Color(11, 57, 96);      // Darker Navy
    public static final Color PRIMARY_LIGHT = new Color(237, 244, 252);  // Soft Tint
    public static final Color ACCENT = new Color(242, 100, 25);          // Sunset Orange
    public static final Color BG_MAIN = new Color(246, 248, 251);        // Off-white / light slate
    public static final Color BG_CARD = Color.WHITE;
    public static final Color TEXT_DARK = new Color(33, 37, 41);
    public static final Color TEXT_MUTED = new Color(110, 117, 124);
    public static final Color BORDER = new Color(222, 226, 230);
    public static final Color SUCCESS = new Color(40, 167, 69);
    public static final Color DANGER = new Color(220, 53, 69);
    public static final Color WARNING = new Color(255, 165, 0);

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public static String formatCurrency(double amount) {
        return "₹ " + String.format("%,.2f", amount);
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton createAccentButton(String text) {
        return createButton(text, ACCENT, Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = createButton(text, Color.WHITE, TEXT_DARK);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(8, 16, 8, 16)));
        return btn;
    }

    public static JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setOpaque(true);

        // Hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (bg.equals(PRIMARY)) btn.setBackground(PRIMARY_DARK);
                else if (bg.equals(ACCENT)) btn.setBackground(ACCENT.darker());
                else if (bg.equals(DANGER)) btn.setBackground(DANGER.darker());
                else if (bg.equals(Color.WHITE)) btn.setBackground(new Color(240, 242, 245));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleInput(field);
        return field;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleInput(field);
        return field;
    }

    public static void styleInput(JTextField field) {
        field.setFont(FONT_REGULAR);
        field.setBackground(Color.WHITE);
        field.setForeground(TEXT_DARK);
        field.setCaretColor(PRIMARY);
        field.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(7, 10, 7, 10)));
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(15, 15, 15, 15)));
        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(PRIMARY_DARK);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.setDefaultRenderer(Integer.class, centerRenderer);
        table.setDefaultRenderer(Double.class, centerRenderer);
    }

    public static JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel("  " + text + "  ");
        badge.setFont(FONT_SMALL);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 6, 3, 6));
        return badge;
    }
}
