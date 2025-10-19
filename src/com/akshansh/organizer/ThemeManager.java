package com.akshansh.organizer;

import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    public enum Theme { SYSTEM, LIGHT, DARK }

    private static Theme current = Theme.SYSTEM;

    public static Theme getCurrent() {
        return current;
    }

    public static void applyTheme(Theme theme, Window window) {
        current = theme;
        try {
            if (theme == Theme.SYSTEM) {
                System.setProperty("apple.awt.application.appearance", "system");
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                // Minimal tweaks to make things a bit flatter
                installGlobalFont(preferredFont());
            } else {
                // Use the current LAF but override palette to emulate light/dark modern look
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                installGlobalFont(preferredFont());
                if (theme == Theme.DARK) {
                    applyDarkPalette();
                } else {
                    applyLightPalette();
                }
            }
        } catch (Exception ignored) {}

        if (window != null) {
            SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
        }
    }

    private static Font preferredFont() {
        // Choose a modern, legible default font per OS
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) {
            return new Font("SF Pro Text", Font.PLAIN, 13);
        } else if (os.contains("win")) {
            return new Font("Segoe UI", Font.PLAIN, 12);
        } else {
            return new Font("Inter", Font.PLAIN, 12);
        }
    }

    private static void installGlobalFont(Font font) {
        if (font == null) return;
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
        }
    }

    private static void applyLightPalette() {
        Color bg = new Color(0xFAFAFA);
        Color fg = new Color(0x222222);
        Color control = new Color(0xFFFFFF);
        Color controlBorder = new Color(0xDDDDDD);
        Color selectionBg = new Color(0xCCE0FF);
        Color selectionFg = fg;
        Color accent = new Color(0x4F8CFF);

        applyCommonPalette(bg, fg, control, controlBorder, selectionBg, selectionFg, accent);
    }

    private static void applyDarkPalette() {
        Color bg = new Color(0x121212);
        Color fg = new Color(0xE0E0E0);
        Color control = new Color(0x1E1E1E);
        Color controlBorder = new Color(0x2A2A2A);
        Color selectionBg = new Color(0x2A4B7C);
        Color selectionFg = new Color(0xFFFFFF);
        Color accent = new Color(0x4F8CFF);

        applyCommonPalette(bg, fg, control, controlBorder, selectionBg, selectionFg, accent);
    }

    private static void applyCommonPalette(Color bg, Color fg, Color control, Color controlBorder, Color selectionBg, Color selectionFg, Color accent) {
        // Panels and containers
        UIManager.put("Panel.background", bg);
        UIManager.put("Panel.foreground", fg);
        UIManager.put("Viewport.background", bg);
        UIManager.put("ScrollPane.background", bg);

        // Labels
        UIManager.put("Label.foreground", fg);

        // Buttons
        UIManager.put("Button.background", control);
        UIManager.put("Button.foreground", fg);
        UIManager.put("Button.select", selectionBg);
        UIManager.put("Button.focus", accent);
        UIManager.put("Button.border", BorderFactory.createLineBorder(controlBorder));

        // Text components
        UIManager.put("TextField.background", control);
        UIManager.put("TextField.foreground", fg);
        UIManager.put("TextField.caretForeground", fg);
        UIManager.put("TextField.selectionBackground", selectionBg);
        UIManager.put("TextField.selectionForeground", selectionFg);

        UIManager.put("TextArea.background", control);
        UIManager.put("TextArea.foreground", fg);
        UIManager.put("TextArea.caretForeground", fg);
        UIManager.put("TextArea.selectionBackground", selectionBg);
        UIManager.put("TextArea.selectionForeground", selectionFg);

        // Tables
        UIManager.put("Table.background", control);
        UIManager.put("Table.foreground", fg);
        UIManager.put("Table.gridColor", controlBorder);
        UIManager.put("Table.selectionBackground", selectionBg);
        UIManager.put("Table.selectionForeground", selectionFg);
        UIManager.put("TableHeader.background", control);
        UIManager.put("TableHeader.foreground", fg);

        // Progress bar
        UIManager.put("ProgressBar.background", control);
        UIManager.put("ProgressBar.foreground", accent);

        // OptionPane
        UIManager.put("OptionPane.background", bg);
        UIManager.put("OptionPane.foreground", fg);
        UIManager.put("Panel.background", bg);
    }
}
