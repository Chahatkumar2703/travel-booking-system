package view;

import util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Base JFrame providing shared behavior, styling, and alert dialogues.
 */
public abstract class BaseFrame extends JFrame {

    public BaseFrame(String title, int width, int height) {
        super(title);
        setSize(width, height);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BG_MAIN);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    public boolean confirmAction(String message) {
        int choice = JOptionPane.showConfirmDialog(this, message, "Confirm Action",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }
}
