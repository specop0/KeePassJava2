/*
 * Copyright 2015 SpecOp0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author SpecOp0
 */
public class KeePassGUI extends JFrame implements ControllerListener, TableModelListener {

    private static final long serialVersionUID = 1L;

    private final List<ActionButton> topPanelButtons = new ArrayList<>();

    private final JTable dataTable;

    public KeePassGUI(String title, JTree tree, TableModel tableModel) throws HeadlessException {
        super(title);
        this.setLayout(new BorderLayout());

        // top menu bar
        JMenuBar menuBar = new JMenuBar();
        String[] menuArray = new String[]{"Data", "Help"};
        for (String menuName : menuArray) {
            JMenu menu = new JMenu(menuName);
            for (int i = 0; i < 5; i++) {
                JMenuItem item = new JMenuItem("test" + i);
                menu.add(item);
            }
            menuBar.add(menu);
        }

        // top tool bar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        Dimension preferredDimension = new Dimension(32, 32);
        for (ActionType type : ActionType.values()) {
            ActionButton button = new ActionButton(type);
            ImageIcon icon = ActionTypeHelper.getIcon(type);
            if (null != icon) {
                button.setIcon(icon);
                button.setToolTipText(icon.getDescription());
            } else {
                button.setText(type.toString());
            }
            button.setPreferredSize(preferredDimension);
            toolbar.add(button);
            topPanelButtons.add(button);
            if (ActionTypeHelper.isSeperatorAfterwards(type)) {
                toolbar.addSeparator();
            }
        }
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(menuBar, BorderLayout.PAGE_START);
        topPanel.add(toolbar, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.PAGE_START);

        // main window entry tree
        DefaultTreeCellRenderer renderer = new KeePassTreeRenderer();
        tree.setCellRenderer(renderer);
        JScrollPane treePane = new JScrollPane(tree);

        // main window data table
        dataTable = new JTable(tableModel);
        dataTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane dataPane = new JScrollPane(dataTable);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                treePane, dataPane);
        splitPane.setOneTouchExpandable(false);
        splitPane.setDividerLocation(300);
        this.add(splitPane, BorderLayout.CENTER);

        // frame settings
        this.setSize(800, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    @Override
    public void setEnabledAllButtons(boolean enabled) {
        getTopPanelButtons().stream().filter((ActionButton button) -> !ActionTypeHelper.isAlwaysActive(button)).forEach((ActionButton button) -> {
            button.setEnabled(enabled);
        });
    }

    public static File chooseFile() {
        return chooseFile(".");
    }

    public static File chooseFile(String path) {
        JFileChooser chooser = new JFileChooser(path);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("KeePass Database", "kdb", "kdbx");
        chooser.setFileFilter(filter);
        int returnValueFile = chooser.showOpenDialog(null);
        if (returnValueFile == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsoluteFile();
        }
        return null;
    }

    static byte[] enterPassword() {
        JPasswordField passwordField = new JPasswordField();
        int returnValuePassword = JOptionPane.showConfirmDialog(null, passwordField, "Enter Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (returnValuePassword == JOptionPane.OK_OPTION) {
            return new String(passwordField.getPassword()).getBytes();
        }
        return null;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (getDataTable().getModel().getRowCount() > 0) {
            SwingUtilities.invokeLater(() -> {
                getDataTable().setRowSelectionInterval(0, 0);
            });
        }
        getDataTable().getParent().revalidate();
        getDataTable().getParent().repaint();
    }

    @Override
    public void copyUsername() {
        copy(true);
    }

    @Override
    public void copyPassword() {
        copy(false);
    }

    public static void showWarning(String title, String message) {
        JOptionPane.showConfirmDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static boolean showCancelDialog(String title, String message) {
        int returnValue = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_CANCEL_OPTION);
        return returnValue != JOptionPane.YES_OPTION;
    }

    public void copy(boolean isUsername) {
        // only use of KeePass model variants
        KeePassTableModel model = (KeePassTableModel) getDataTable().getModel();
        DatabaseObject object = model.getDatabaseObject(getDataTable().getSelectedRow());
        if (null != object && object.isEntry()) {
            String message;
            if (isUsername) {
                message = object.getEntry().getUsername();
            } else {
                message = object.getEntry().getPassword();
            }
            KeePassController.copyToClipboard(message);
        } else {
            showWarning("Copy to Clipboard Error", "No Entry in table selected (right-hand side).");
        }
    }

    public List<ActionButton> getTopPanelButtons() {
        return topPanelButtons;
    }

    public JTable getDataTable() {
        return dataTable;
    }

}
