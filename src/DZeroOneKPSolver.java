package bag;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * D{0-1}背包问题求解器主类
 * 实现动态规划算法求解D{0-1}背包问题
 * 包含GUI界面和完整功能
 */
public class DZeroOneKPSolver extends JFrame {

    private static final long serialVersionUID = 1L;

    // ============ 界面常量 ============
    private static final Color COLOR_BG          = new Color(235, 241, 250);
    private static final Color COLOR_HEADER_BG   = new Color(52, 73, 94);
    private static final Color COLOR_HEADER_FG   = Color.WHITE;
    private static final Color COLOR_PANEL_BG    = new Color(245, 248, 252);
    private static final Color COLOR_BORDER      = new Color(189, 204, 220);
    private static final Color COLOR_ACCENT_BLUE = new Color(52, 152, 219);
    private static final Color COLOR_ACCENT_GRN  = new Color(39, 174, 96);
    private static final Color COLOR_ACCENT_RED  = new Color(231, 76, 60);
    private static final Color COLOR_TEXT        = new Color(44, 62, 80);
    private static final Color COLOR_STATUS_BG   = new Color(52, 73, 94);
    private static final Color COLOR_STATUS_FG   = Color.WHITE;

    private static final Font  FONT_TITLE     = new Font("微软雅黑", Font.BOLD,   18);
    private static final Font  FONT_UI        = new Font("微软雅黑", Font.PLAIN,  13);
    private static final Font  FONT_MONO      = new Font("Consolas", Font.PLAIN, 13);
    private static final Font  FONT_TABLE_HDR  = new Font("微软雅黑", Font.BOLD,  12);
    private static final Font  FONT_TABLE_CELL = new Font("微软雅黑", Font.PLAIN, 12);

    // ============ 组件声明 ============
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu solveMenu;
    private JMenu viewMenu;
    private JMenu helpMenu;
    private JMenuItem openItem;
    private JMenuItem saveItem;
    private JMenuItem exitItem;
    private JMenuItem solveItem;
    private JMenuItem sortItem;
    private JMenuItem scatterPlotItem;
    private JMenuItem aboutItem;

    private JPanel headerPanel;
    private JPanel mainPanel;
    private JPanel controlPanel;
    private JPanel dataPanel;
    private JPanel resultPanel;

    private JButton openButton;
    private JButton solveButton;
    private JButton sortButton;
    private JButton plotButton;
    private JButton saveButton;
    private JButton backButton;

    private JTextArea dataTextArea;
    private JTextArea resultTextArea;
    private JScrollPane dataScrollPane;
    private JScrollPane resultScrollPane;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    private JLabel capacityLabel;
    private JTextField capacityField;
    private JLabel statusLabel;

    private JTabbedPane dataTabbedPane;
    private D01kpScatterPlotPanel scatterPlotPanel;

    // 数据存储
    private List<ItemSet> itemSets;
    private int capacity;
    private Solution currentSolution;

    /**
     * 项集类，表示一个项集包含3个物品（包内可见，供散点图等组件使用）
     */
    static class ItemSet {
        int setId;
        Item[] items = new Item[3];

        ItemSet(int setId, Item item1, Item item2, Item item3) {
            this.setId = setId;
            items[0] = item1;
            items[1] = item2;
            items[2] = item3;
        }

        double getThirdItemRatio() {
            if (items[2].weight == 0) {
                return Double.MAX_VALUE;
            }
            return (double) items[2].value / items[2].weight;
        }
    }

    /**
     * 物品类（包内可见，供散点图等组件使用）
     */
    static class Item {
        int value;
        int weight;

        Item(int value, int weight) {
            this.value = value;
            this.weight = weight;
        }
    }

    /**
     * 解决方案类
     */
    private static class Solution {
        int optimalValue;
        List<Integer> selections;
        long solveTime;

        Solution(int optimalValue, List<Integer> selections, long solveTime) {
            this.optimalValue = optimalValue;
            this.selections = selections;
            this.solveTime = solveTime;
        }
    }

    /**
     * 动态规划求解器
     */
    private static class DPSolver {

        /**
         * 求解D{0-1}背包问题
         *
         * @param itemSets 项集列表
         * @param capacity 背包容量
         * @return 解决方案
         */
        public static Solution solve(List<ItemSet> itemSets, int capacity) {
            long startTime = System.nanoTime();

            int n = itemSets.size();
            int C = capacity;

            int[][] dp = new int[n + 1][C + 1];
            int[][] choice = new int[n + 1][C + 1];

            for (int i = 1; i <= n; i++) {
                ItemSet currentSet = itemSets.get(i - 1);

                for (int c = 0; c <= C; c++) {
                    int maxValue = dp[i - 1][c];
                    int bestChoice = 0;

                    for (int itemIdx = 0; itemIdx < 3; itemIdx++) {
                        Item item = currentSet.items[itemIdx];
                        if (c >= item.weight) {
                            int candidateValue = dp[i - 1][c - item.weight] + item.value;
                            if (candidateValue > maxValue) {
                                maxValue = candidateValue;
                                bestChoice = itemIdx + 1;
                            }
                        }
                    }

                    dp[i][c] = maxValue;
                    choice[i][c] = bestChoice;
                }
            }

            List<Integer> selections = new ArrayList<>();
            int remainingCapacity = C;

            for (int i = n; i >= 1; i--) {
                int ch = choice[i][remainingCapacity];
                selections.add(0, ch);

                if (ch != 0) {
                    remainingCapacity -= itemSets.get(i - 1).items[ch - 1].weight;
                }
            }

            long endTime = System.nanoTime();
            long solveTime = endTime - startTime;

            return new Solution(dp[n][C], selections, solveTime);
        }
    }

    /**
     * 构造函数
     */
    public DZeroOneKPSolver() {
        setTitle("D{0-1}背包问题求解器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 820);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        initComponents();
        setupLayout();
        setupEventHandlers();
    }

    // ============ 按钮工厂（统一风格） ============
    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_UI);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPreCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                new EmptyBorder(6, 16, 6, 16)));
        btn.setOpaque(true);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void update(Graphics g, JComponent c) {
                if (c.isOpaque()) {
                    g.setColor(c.getBackground());
                    g.fillRect(0, 0, c.getWidth(), c.getHeight());
                }
                paint(g, c);
            }
        });
        return btn;
    }

    private JButton makeAccentButton(String text) {
        return makeButton(text, COLOR_ACCENT_BLUE, Color.WHITE);
    }

    private JButton makeGreenButton(String text) {
        return makeButton(text, COLOR_ACCENT_GRN, Color.WHITE);
    }

    private JButton makeGrayButton(String text) {
        return makeButton(text, new Color(200, 210, 220), COLOR_TEXT);
    }

    private JButton makeRedButton(String text) {
        return makeButton(text, COLOR_ACCENT_RED, Color.WHITE);
    }

    // ============ 通用文本域（统一样式） ============
    private JTextArea makeTextArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(FONT_MONO);
        ta.setEditable(false);
        ta.setBackground(Color.WHITE);
        ta.setForeground(COLOR_TEXT);
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                new EmptyBorder(6, 8, 6, 8)));
        ta.setSelectionColor(COLOR_ACCENT_BLUE);
        return ta;
    }

    // ============ 通用表格（交替行色 + 美化表头） ============
    private void styleTable(JTable table) {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(24);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(new Color(200, 225, 240));
        table.setSelectionForeground(COLOR_TEXT);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));

        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(FONT_TABLE_HDR);
        hdr.setBackground(COLOR_HEADER_BG);
        hdr.setForeground(COLOR_HEADER_FG);
        hdr.setBorder(BorderFactory.createLineBorder(COLOR_HEADER_BG.darker()));
        hdr.setPreferredSize(new Dimension(hdr.getPreferredSize().width, 30));

        table.setAlternateRowColor(Color.WHITE, new Color(240, 245, 252));
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        // ---- 菜单栏 ----
        menuBar = new JMenuBar();
        menuBar.setBackground(COLOR_HEADER_BG);
        menuBar.setBorderPainted(false);

        fileMenu = new JMenu("文件");
        fileMenu.setForeground(Color.WHITE);
        openItem = new JMenuItem("打开数据文件");
        saveItem = new JMenuItem("保存求解结果");
        exitItem = new JMenuItem("退出");
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        solveMenu = new JMenu("求解");
        solveMenu.setForeground(Color.WHITE);
        solveItem = new JMenuItem("求解最优解");
        sortItem = new JMenuItem("按第三项比值排序");
        solveMenu.add(solveItem);
        solveMenu.add(sortItem);

        viewMenu = new JMenu("视图");
        viewMenu.setForeground(Color.WHITE);
        scatterPlotItem = new JMenuItem("显示散点图");
        viewMenu.add(scatterPlotItem);

        helpMenu = new JMenu("帮助");
        helpMenu.setForeground(Color.WHITE);
        aboutItem = new JMenuItem("关于");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(solveMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // ---- 顶部标题栏 ----
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_HEADER_BG);
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("D{0-1}背包问题求解器");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_HEADER_FG);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // ---- 控制面板按钮 ----
        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        controlPanel.setBackground(COLOR_PANEL_BG);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 1, COLOR_BORDER),
                new EmptyBorder(6, 12, 6, 12)));

        openButton   = makeButton("打开文件", new Color(52, 73, 94), Color.WHITE);
        solveButton  = makeGreenButton("求解");
        sortButton   = makeGrayButton("排序");
        plotButton   = makeAccentButton("散点图");
        saveButton   = makeGrayButton("保存结果");
        backButton   = makeRedButton("返回");
        backButton.setVisible(false);

        openButton.setToolTipText("打开 D{0-1}KP 数据文件");
        solveButton.setToolTipText("使用动态规划求解最优解");
        sortButton.setToolTipText("按第三物品价值/重量比排序");
        plotButton.setToolTipText("切换到散点图视图");
        saveButton.setToolTipText("保存当前求解结果");
        backButton.setToolTipText("返回初始界面");

        capacityLabel = new JLabel("背包容量:");
        capacityLabel.setFont(FONT_UI);
        capacityLabel.setForeground(COLOR_TEXT);
        capacityField = new JTextField(8);
        capacityField.setFont(FONT_MONO);
        capacityField.setEditable(false);
        capacityField.setHorizontalAlignment(JTextField.CENTER);
        capacityField.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT_BLUE, 1));
        capacityField.setBackground(Color.WHITE);

        controlPanel.add(openButton);
        controlPanel.add(solveButton);
        controlPanel.add(sortButton);
        controlPanel.add(plotButton);
        controlPanel.add(saveButton);
        controlPanel.add(Box.createHorizontalStrut(16));
        controlPanel.add(capacityLabel);
        controlPanel.add(capacityField);

        // ---- 数据视图（带标签页） ----
        dataTextArea = makeTextArea();
        dataScrollPane = new JScrollPane(dataTextArea);
        dataScrollPane.getViewport().setBackground(Color.WHITE);

        String[] columnNames = {"项集ID", "物品1价值", "物品1重量",
                "物品2价值", "物品2重量", "物品3价值", "物品3重量", "第三项比值"};
        tableModel = new DefaultTableModel(columnNames, 0);
        dataTable = new JTable(tableModel);
        styleTable(dataTable);
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        dataTabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        dataTabbedPane.setFont(FONT_UI);
        dataTabbedPane.setBackground(COLOR_PANEL_BG);
        dataTabbedPane.addTab("文本视图", dataScrollPane);
        dataTabbedPane.addTab("表格视图", tableScrollPane);

        JPanel plotTab = new JPanel(new BorderLayout());
        plotTab.setBackground(Color.WHITE);
        plotTab.add(scatterPlotPanel, BorderLayout.CENTER);
        dataTabbedPane.addTab("散点图", plotTab);

        dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBackground(COLOR_PANEL_BG);
        dataPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 0, 1, COLOR_BORDER),
                new EmptyBorder(0, 0, 0, 0)));
        dataPanel.add(dataTabbedPane, BorderLayout.CENTER);

        // ---- 结果面板 ----
        resultTextArea = makeTextArea();
        resultScrollPane = new JScrollPane(resultTextArea);
        resultScrollPane.getViewport().setBackground(Color.WHITE);

        JPanel resultBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        resultBtnPanel.setBackground(COLOR_PANEL_BG);
        resultBtnPanel.add(backButton);

        resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBackground(COLOR_PANEL_BG);
        resultPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 1, COLOR_BORDER),
                new EmptyBorder(0, 0, 0, 0)));
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);
        resultPanel.add(resultBtnPanel, BorderLayout.SOUTH);

        // ---- 状态栏 ----
        statusLabel = new JLabel("  就绪");
        statusLabel.setFont(FONT_UI);
        statusLabel.setForeground(COLOR_STATUS_FG);
        statusLabel.setBackground(COLOR_STATUS_BG);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(4, 12, 4, 12));

        // ---- 主面板 ----
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(controlPanel, BorderLayout.AFTER_HEADER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, dataPanel, resultPanel);
        splitPane.setResizeWeight(0.62);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);
        splitPane.setBackground(COLOR_BORDER);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);

        itemSets = new ArrayList<>();
        currentSolution = null;
        scatterPlotPanel = new D01kpScatterPlotPanel();
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        openButton.addActionListener(e -> openFile());
        openItem.addActionListener(e -> openFile());

        solveButton.addActionListener(e -> solveProblem());
        solveItem.addActionListener(e -> solveProblem());

        sortButton.addActionListener(e -> sortItemsByThirdItemRatio());
        sortItem.addActionListener(e -> sortItemsByThirdItemRatio());

        plotButton.addActionListener(e -> showScatterPlot());
        scatterPlotItem.addActionListener(e -> showScatterPlot());

        saveButton.addActionListener(e -> saveResult());
        saveItem.addActionListener(e -> saveResult());

        backButton.addActionListener(e -> resetToInitialState());

        exitItem.addActionListener(e -> System.exit(0));

        aboutItem.addActionListener(e -> showAboutDialog());
    }

    /**
     * 打开数据文件
     */
    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("数据文件 (*.dat)", "dat"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                readDataFromFile(file);
                updateDataDisplay();
                statusLabel.setText("  已加载文件: " + file.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "读取文件失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("  文件读取失败");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "文件格式错误: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("  文件格式错误");
            }
        }
    }

    /**
     * 从文件读取数据
     *
     * @param file 数据文件
     * @throws IOException 文件读写异常
     * @throws IllegalArgumentException 数据格式异常
     */
    private void readDataFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine == null) {
                throw new IllegalArgumentException("文件为空");
            }

            String[] parts = firstLine.trim().split("\\s+");
            if (parts.length != 2) {
                throw new IllegalArgumentException("第一行必须包含两个整数: 项集数量 背包容量");
            }

            int numSets = Integer.parseInt(parts[0]);
            capacity = Integer.parseInt(parts[1]);

            if (numSets <= 0 || capacity <= 0) {
                throw new IllegalArgumentException("项集数量和背包容量必须为正整数");
            }

            itemSets.clear();

            for (int i = 0; i < numSets; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("文件数据行数不足，期望 " + numSets + " 行数据");
                }

                String[] tokens = line.trim().split("\\s+");
                if (tokens.length < 6) {
                    throw new IllegalArgumentException(
                            "第 " + (i + 1) + " 行数据格式错误：需要至少6个整数（3个价值 + 3个重量）");
                }

                int[] items = new int[6];
                for (int j = 0; j < 6; j++) {
                    try {
                        items[j] = Integer.parseInt(tokens[j]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "第 " + (i + 1) + " 行第 " + (j + 1) + " 个值不是有效整数");
                    }
                }

                itemSets.add(new ItemSet(i + 1,
                        new Item(items[0], items[1]),
                        new Item(items[2], items[3]),
                        new Item(items[4], items[5])));
            }
        }
    }

    /**
     * 更新数据显示
     */
    private void updateDataDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("项集数量: %d, 背包容量: %d\n\n", itemSets.size(), capacity));
        sb.append("项集ID  物品1(价值,重量)  物品2(价值,重量)  物品3(价值,重量)  第三项比值\n");
        sb.append("─".repeat(90)).append("\n");

        for (ItemSet set : itemSets) {
            sb.append(String.format("%3d     (%4d, %4d)      (%4d, %4d)      (%4d, %4d)      %.4f\n",
                    set.setId,
                    set.items[0].value, set.items[0].weight,
                    set.items[1].value, set.items[1].weight,
                    set.items[2].value, set.items[2].weight,
                    set.getThirdItemRatio()));
        }

        dataTextArea.setText(sb.toString());

        scatterPlotPanel.setItemSets(itemSets);

        tableModel.setRowCount(0);
        for (ItemSet set : itemSets) {
            Object[] rowData = {
                    set.setId,
                    set.items[0].value,
                    set.items[0].weight,
                    set.items[1].value,
                    set.items[1].weight,
                    set.items[2].value,
                    set.items[2].weight,
                    String.format("%.4f", set.getThirdItemRatio())
            };
            tableModel.addRow(rowData);
        }
    }

    /**
     * 求解问题
     */
    private void solveProblem() {
        if (itemSets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先加载数据文件",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        statusLabel.setText("  正在求解...");
        setAllButtonsEnabled(false);

        new SwingWorker<Solution, Void>() {
            @Override
            protected Solution doInBackground() {
                return DPSolver.solve(itemSets, capacity);
            }

            @Override
            protected void done() {
                try {
                    currentSolution = get();
                    displaySolution();
                    statusLabel.setText("  求解完成，最优价值: " + currentSolution.optimalValue);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(DZeroOneKPSolver.this,
                            "求解失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("  求解失败");
                } finally {
                    setAllButtonsEnabled(true);
                }
            }
        }.execute();
    }

    private void setAllButtonsEnabled(boolean enabled) {
        openButton.setEnabled(enabled);
        solveButton.setEnabled(enabled);
        sortButton.setEnabled(enabled);
        plotButton.setEnabled(enabled);
        saveButton.setEnabled(enabled);
    }

    /**
     * 显示解决方案
     */
    private void displaySolution() {
        if (currentSolution == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════\n");
        sb.append("           D{0-1} 背包问题求解结果\n");
        sb.append("═══════════════════════════════════════════\n\n");
        sb.append(String.format("背包容量: %d\n", capacity));
        sb.append(String.format("项集数量: %d\n", itemSets.size()));
        sb.append(String.format("最优总价值: %d\n", currentSolution.optimalValue));
        sb.append(String.format("求解耗时: %.3f ms\n\n", currentSolution.solveTime / 1000000.0));
        sb.append("───────────────────────────────────────────\n");
        sb.append("各项集选择详情:\n");
        sb.append("项集ID   选择      价值    重量\n");
        sb.append("───────────────────────────────────────────\n");

        int totalWeight = 0;
        for (int i = 0; i < itemSets.size(); i++) {
            ItemSet set = itemSets.get(i);
            int selection = currentSolution.selections.get(i);

            if (selection == 0) {
                sb.append(String.format("%4d     不选       0       0\n", set.setId));
            } else {
                Item selectedItem = set.items[selection - 1];
                totalWeight += selectedItem.weight;
                sb.append(String.format("%4d     物品%d    %4d    %4d\n",
                        set.setId, selection, selectedItem.value, selectedItem.weight));
            }
        }

        sb.append("───────────────────────────────────────────\n");
        sb.append(String.format("总重量: %d  (容量: %d)\n", totalWeight, capacity));
        double utilization = totalWeight * 100.0 / capacity;
        sb.append(String.format("背包利用率: %.2f%%\n", utilization));
        sb.append("═══════════════════════════════════════════\n");

        resultTextArea.setText(sb.toString());
        backButton.setVisible(true);
        dataTabbedPane.setSelectedIndex(0);
    }

    /**
     * 返回初始界面
     */
    private void resetToInitialState() {
        resultTextArea.setText("");
        currentSolution = null;
        backButton.setVisible(false);
        statusLabel.setText("  已返回初始界面");
    }

    /**
     * 按第三项价值重量比排序
     */
    private void sortItemsByThirdItemRatio() {
        if (itemSets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先加载数据文件",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        itemSets.sort(Comparator.comparingDouble(ItemSet::getThirdItemRatio).reversed());
        updateDataDisplay();
        scatterPlotPanel.setItemSets(itemSets);
        statusLabel.setText("  已按第三项比值降序排列");
    }

    /**
     * 显示散点图
     */
    private void showScatterPlot() {
        if (itemSets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先加载数据文件",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        scatterPlotPanel.setItemSets(itemSets);
        if (dataTabbedPane != null) {
            dataTabbedPane.setSelectedIndex(2);
        }
        scatterPlotPanel.repaint();
        statusLabel.setText("  已切换到散点图视图");
    }

    /**
     * 保存结果
     */
    private void saveResult() {
        if (currentSolution == null) {
            JOptionPane.showMessageDialog(this, "请先执行求解操作",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(
                new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.print(resultTextArea.getText());
                statusLabel.setText("  已保存到: " + file.getName());
                JOptionPane.showMessageDialog(this, "保存成功！",
                        "提示", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("  保存失败");
            }
        }
    }

    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        String aboutText = "D{0-1}背包问题求解器 v1.0\n\n"
                + "功能说明:\n"
                + "1. 读取D{0-1}KP数据文件\n"
                + "2. 动态规划求解最优解\n"
                + "3. 按第三项价值重量比排序\n"
                + "4. 显示数据散点图\n"
                + "5. 保存求解结果\n\n"
                + "开发语言: Java\n"
                + "界面库: Swing\n"
                + "算法: 动态规划";

        JOptionPane.showMessageDialog(this, aboutText,
                "关于", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 主方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 使用默认外观
            }
            DZeroOneKPSolver solver = new DZeroOneKPSolver();
            solver.setVisible(true);
        });
    }
}
