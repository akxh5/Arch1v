package com.akshansh.organizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.util.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;

public class MainGUI extends JFrame {
    private JTextArea logArea;
    private JButton selectFolderButton;
    private JButton organizeButton;
    private JButton detectDuplicatesButton;
    private JButton decategorizeButton;
    private JButton clearLogButton;
    private JTextField folderField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private SwingWorker<Void, String> currentWorker;
    private File selectedDirectory;
    private JTabbedPane tabbedPane;
    private JTable duplicatesTable;
    private javax.swing.table.DefaultTableModel duplicatesModel;
    private JButton revealSelectedButton;
    private JButton revealAllButton;
    private JButton clearDuplicatesButton;
    private JButton copyPathButton;
    private JMenuBar menuBar;
    private JMenu themeMenu;

    public MainGUI() {
        super("Smart File Organizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
    setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(8, 8, 8, 8));

    // Menu bar with Theme switcher
    menuBar = new JMenuBar();
    themeMenu = new JMenu("Theme");
    ButtonGroup themeGroup = new ButtonGroup();
    JRadioButtonMenuItem systemTheme = new JRadioButtonMenuItem("System", true);
    JRadioButtonMenuItem lightTheme = new JRadioButtonMenuItem("Light");
    JRadioButtonMenuItem darkTheme = new JRadioButtonMenuItem("Dark");
    themeGroup.add(systemTheme);
    themeGroup.add(lightTheme);
    themeGroup.add(darkTheme);
    themeMenu.add(systemTheme);
    themeMenu.add(lightTheme);
    themeMenu.add(darkTheme);
    menuBar.add(themeMenu);
    setJMenuBar(menuBar);

        // Top panel for folder selection
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(new JLabel("Folder:"), gbc);

        folderField = new JTextField("No folder selected");
        folderField.setEditable(false);
        folderField.setPreferredSize(new Dimension(450, 28));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(folderField, gbc);

    selectFolderButton = new JButton("Browse…");
        selectFolderButton.setToolTipText("Choose a folder to organize");
        selectFolderButton.setMnemonic('B');
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        topPanel.add(selectFolderButton, gbc);

        add(topPanel, BorderLayout.NORTH);

        // Center area with tabs (Log and Duplicates)
        tabbedPane = new JTabbedPane();

        // Log tab
        logArea = new JTextArea(15, 50);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        tabbedPane.addTab("Activity Log", logScrollPane);

        // Duplicates tab
        duplicatesModel = new javax.swing.table.DefaultTableModel(new Object[]{"Original File", "Duplicate"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        duplicatesTable = new JTable(duplicatesModel);
        duplicatesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane dupScrollPane = new JScrollPane(duplicatesTable);

        JPanel dupButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        revealSelectedButton = new JButton("Reveal Selected");
        revealSelectedButton.setToolTipText("Reveal selected files in Finder/Explorer");
        revealAllButton = new JButton("Reveal All");
        clearDuplicatesButton = new JButton("Clear");
        copyPathButton = new JButton("Copy Path");
        dupButtons.add(revealSelectedButton);
        dupButtons.add(revealAllButton);
        dupButtons.add(copyPathButton);
        dupButtons.add(clearDuplicatesButton);

        JPanel dupPanel = new JPanel(new BorderLayout(8, 8));
        dupPanel.add(dupScrollPane, BorderLayout.CENTER);
        dupPanel.add(dupButtons, BorderLayout.SOUTH);

        tabbedPane.addTab("Duplicates", dupPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom panel for action buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    organizeButton = new JButton("Organize Files");
        organizeButton.setToolTipText("Categorize files into folders by type");
        organizeButton.setMnemonic('O');
    detectDuplicatesButton = new JButton("Detect Duplicates");
        detectDuplicatesButton.setToolTipText("Find files with identical content");
        detectDuplicatesButton.setMnemonic('D');
    decategorizeButton = new JButton("Decategorize Files");
        decategorizeButton.setToolTipText("Move files back to the base folder");
        decategorizeButton.setMnemonic('C');
    clearLogButton = new JButton("Clear Log");
        clearLogButton.setToolTipText("Clear the log output");
        clearLogButton.setMnemonic('L');

        buttonPanel.add(organizeButton);
        buttonPanel.add(detectDuplicatesButton);
        buttonPanel.add(decategorizeButton);
        buttonPanel.add(clearLogButton);

        JPanel statusPanel = new JPanel(new BorderLayout(8, 0));
        statusLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.CENTER);

        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // Button actions
        systemTheme.addActionListener(e -> ThemeManager.applyTheme(ThemeManager.Theme.SYSTEM, this));
        lightTheme.addActionListener(e -> ThemeManager.applyTheme(ThemeManager.Theme.LIGHT, this));
        darkTheme.addActionListener(e -> ThemeManager.applyTheme(ThemeManager.Theme.DARK, this));

        selectFolderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView());
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = chooser.showOpenDialog(MainGUI.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedDirectory = chooser.getSelectedFile();
                    folderField.setText(selectedDirectory.getAbsolutePath());
                    log("Selected folder: " + selectedDirectory.getAbsolutePath());
                }
            }
        });

        organizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!checkFolderSelected()) return;
                runTask("Organizing files…", new Runnable() {
                    @Override
                    public void run() {
                        FileScanner scanner = new FileScanner(selectedDirectory.getAbsolutePath());
                        scanner.organizeFiles();
                        publishLog("Files organized successfully.");
                    }
                });
            }
        });

        detectDuplicatesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!checkFolderSelected()) return;
                runDuplicatesTask("Detecting duplicates…");
            }
        });

        decategorizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!checkFolderSelected()) return;
                int confirm = JOptionPane.showConfirmDialog(MainGUI.this,
                        "Move all files back to the base folder?",
                        "Confirm Decategorize",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) return;
                runTask("Decategorizing files…", new Runnable() {
                    @Override
                    public void run() {
                        FileScanner scanner = new FileScanner(selectedDirectory.getAbsolutePath());
                        scanner.decategorizeFiles();
                        publishLog("Files decategorized successfully.");
                    }
                });
            }
        });

        clearLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logArea.setText("");
            }
        });

        // Duplicates buttons actions
        revealSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = duplicatesTable.getSelectedRows();
                for (int r : rows) {
                    String path = (String) duplicatesModel.getValueAt(r, 1);
                    revealInFileManager(new File(path));
                }
            }
        });

        revealAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int r = 0; r < duplicatesModel.getRowCount(); r++) {
                    String path = (String) duplicatesModel.getValueAt(r, 1);
                    revealInFileManager(new File(path));
                }
            }
        });

        copyPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] rows = duplicatesTable.getSelectedRows();
                StringBuilder sb = new StringBuilder();
                for (int r : rows) {
                    if (sb.length() > 0) sb.append('\n');
                    sb.append((String) duplicatesModel.getValueAt(r, 1));
                }
                StringSelection sel = new StringSelection(sb.toString());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            }
        });

        clearDuplicatesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearDuplicatesView();
            }
        });

        // Enable drag-and-drop of a folder onto the window
        new DropTarget(this.getContentPane(), DnDConstants.ACTION_COPY, new java.awt.dnd.DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> dropped = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (dropped != null && !dropped.isEmpty()) {
                        File f = dropped.get(0);
                        if (f.isDirectory()) {
                            selectedDirectory = f;
                            folderField.setText(selectedDirectory.getAbsolutePath());
                            log("Selected folder: " + selectedDirectory.getAbsolutePath());
                        } else {
                            JOptionPane.showMessageDialog(MainGUI.this, "Please drop a directory.", "Not a folder", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    dtde.dropComplete(true);
                } catch (Exception ex) {
                    dtde.rejectDrop();
                }
            }
        }, true);
    }

    private boolean checkFolderSelected() {
        if (selectedDirectory == null) {
            JOptionPane.showMessageDialog(this, "Please select a folder first.", "Folder Required", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    // Background task runner with progress bar and System.out capture
    private void runTask(String statusText, Runnable task) {
        if (currentWorker != null && !currentWorker.isDone()) {
            JOptionPane.showMessageDialog(this, "Another operation is in progress.", "Please wait", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        setControlsEnabled(false);
        statusLabel.setText(statusText);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        PrintStream redirectStream = new PrintStream(new TextAreaOutputStream());
        System.setOut(redirectStream);
        System.setErr(redirectStream);

        currentWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    task.run();
                } catch (Exception ex) {
                    publish("Error: " + ex.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String s : chunks) {
                    log(s);
                }
            }

            @Override
            protected void done() {
                // Restore System.out
                System.setOut(originalOut);
                System.setErr(originalErr);
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                statusLabel.setText("Ready");
                setControlsEnabled(true);
            }
        };
        currentWorker.execute();
    }

    // Specialized task for duplicate detection: updates the Duplicates tab and avoids noisy per-file logs
    private void runDuplicatesTask(String statusText) {
        if (currentWorker != null && !currentWorker.isDone()) {
            JOptionPane.showMessageDialog(this, "Another operation is in progress.", "Please wait", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        setControlsEnabled(false);
        statusLabel.setText(statusText);
        progressBar.setIndeterminate(true);
        progressBar.setVisible(true);

        currentWorker = new SwingWorker<>() {
            private String resultText;

            @Override
            protected Void doInBackground() {
                try {
                    FileScanner scanner = new FileScanner(selectedDirectory.getAbsolutePath());
                    // Don't redirect System.out to keep the UI log clean for this operation.
                    resultText = scanner.detectDuplicates();
                } catch (Exception ex) {
                    resultText = "Error detecting duplicates: " + ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                progressBar.setIndeterminate(false);
                progressBar.setVisible(false);
                statusLabel.setText("Ready");
                setControlsEnabled(true);

                updateDuplicatesView(resultText);
            }
        };
        currentWorker.execute();
    }

    private void updateDuplicatesView(String duplicatesText) {
        clearDuplicatesView();
        if (duplicatesText == null || duplicatesText.trim().isEmpty()) {
            tabbedPane.setSelectedIndex(0);
            return;
        }
        if (duplicatesText.toLowerCase().contains("no duplicates")) {
            publishLog("No duplicates found.");
            tabbedPane.setSelectedIndex(0);
            return;
        }
        // Parse the text format generated by FileScanner.detectDuplicates
        String[] lines = duplicatesText.split("\n");
        String currentOriginal = null;
        int added = 0;
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("Duplicates of ") && line.endsWith(":")) {
                currentOriginal = line.substring("Duplicates of ".length(), line.length() - 1);
            } else if (line.startsWith("- ")) {
                String dup = line.substring(2).trim();
                if (currentOriginal != null && !dup.isEmpty()) {
                    duplicatesModel.addRow(new Object[]{currentOriginal, dup});
                    added++;
                }
            }
        }
        if (added == 0) {
            publishLog("No duplicates found.");
            tabbedPane.setSelectedIndex(0);
        } else {
            publishLog("Found " + added + " duplicate file(s).");
            tabbedPane.setSelectedIndex(1);
        }
    }

    private void clearDuplicatesView() {
        while (duplicatesModel.getRowCount() > 0) {
            duplicatesModel.removeRow(0);
        }
    }

    private void revealInFileManager(File file) {
        try {
            if (file == null) return;
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                // Reveal in Finder
                new ProcessBuilder("open", "-R", file.getAbsolutePath()).start();
            } else if (os.contains("win")) {
                new ProcessBuilder("explorer", "/select,", file.getAbsolutePath()).start();
            } else {
                // Linux/others: open the parent directory
                File parent = file.getParentFile();
                if (parent != null && parent.exists()) {
                    new ProcessBuilder("xdg-open", parent.getAbsolutePath()).start();
                } else if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
            }
        } catch (Exception ignored) {
        }
    }

    // Helper to append to log safely from any thread
    private void publishLog(String msg) {
        SwingUtilities.invokeLater(() -> log(msg));
    }

    private void setControlsEnabled(boolean enabled) {
        selectFolderButton.setEnabled(enabled);
        organizeButton.setEnabled(enabled);
        detectDuplicatesButton.setEnabled(enabled);
        decategorizeButton.setEnabled(enabled);
        clearLogButton.setEnabled(enabled);
    }

    // Stream that forwards System.out prints to the log area on the EDT
    private class TextAreaOutputStream extends OutputStream {
        private final StringBuilder buffer = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            if (b == '\n') {
                flushBuffer();
            } else {
                buffer.append((char) b);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (int i = off; i < off + len; i++) {
                write(b[i]);
            }
        }

        private void flushBuffer() {
            final String text = buffer.toString();
            buffer.setLength(0);
            if (!text.isEmpty()) {
                SwingUtilities.invokeLater(() -> log(text));
            }
        }

        @Override
        public void flush() throws IOException {
            flushBuffer();
        }
    }

    public static void main(String[] args) {
        // Apply system theme by default and allow runtime switching
        ThemeManager.applyTheme(ThemeManager.Theme.SYSTEM, null);
        SwingUtilities.invokeLater(() -> {
            MainGUI gui = new MainGUI();
            ThemeManager.applyTheme(ThemeManager.Theme.SYSTEM, gui);
            gui.setVisible(true);
        });
    }
}