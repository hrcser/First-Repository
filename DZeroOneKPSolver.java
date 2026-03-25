package bag;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
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

    // 组件声明
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
        initComponents();
        setupLayout();
        setupEventHandlers();
        setTitle("D{0-1}背包问题求解器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        menuBar = new JMenuBar();

        fileMenu = new JMenu("文件(F)");
        fileMenu.setMnemonic('F');

        openItem = new JMenuItem("打开数据文件(O)");
        openItem.setMnemonic('O');
        saveItem = new JMenuItem("保存结果(S)");
        saveItem.setMnemonic('S');
        exitItem = new JMenuItem("退出(X)");
        exitItem.setMnemonic('X');

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        solveMenu = new JMenu("求解(S)");
        solveMenu.setMnemonic('S');

        solveItem = new JMenuItem("求解最优解(S)");
        solveItem.setMnemonic('S');
        sortItem = new JMenuItem("排序项集(T)");
        sortItem.setMnemonic('T');

        solveMenu.add(solveItem);
        solveMenu.add(sortItem);

        viewMenu = new JMenu("视图(V)");
        viewMenu.setMnemonic('V');

        scatterPlotItem = new JMenuItem("显示散点图(P)");
        scatterPlotItem.setMnemonic('P');
        viewMenu.add(scatterPlotItem);

        helpMenu = new JMenu("帮助(H)");
        helpMenu.setMnemonic('H');

        aboutItem = new JMenuItem("关于(A)");
        aboutItem.setMnemonic('A');
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(solveMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        openButton = new JButton("打开文件");
        solveButton = new JButton("求解");
        sortButton = new JButton("排序");
        plotButton = new JButton("散点图");
        saveButton = new JButton("保存结果");
        backButton = new JButton("返回");
        backButton.setVisible(false);

        dataTextArea = new JTextArea(10, 40);
        dataTextArea.setEditable(false);
        dataTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        dataScrollPane = new JScrollPane(dataTextArea);

        resultTextArea = new JTextArea(10, 40);
        resultTextArea.setEditable(false);
        resultTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        resultScrollPane = new JScrollPane(resultTextArea);

        String[] columnNames = {"项集ID", "物品1(价值)", "物品1(重量)",
                "物品2(价值)", "物品2(重量)", "物品3(价值)", "物品3(重量)", "第三项价值/重量"};
        tableModel = new DefaultTableModel(columnNames, 0);
        dataTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(dataTable);

        capacityLabel = new JLabel("背包容量:");
        capacityField = new JTextField(10);
        capacityField.setEditable(false);

        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());

        mainPanel = new JPanel(new BorderLayout());
        controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dataPanel = new JPanel(new BorderLayout());
        resultPanel = new JPanel(new BorderLayout());

        itemSets = new ArrayList<>();
        currentSolution = null;

        scatterPlotPanel = new D01kpScatterPlotPanel();
    }

    /**
     * 设置布局
     */
    private void setupLayout() {
        controlPanel.add(openButton);
        controlPanel.add(solveButton);
        controlPanel.add(sortButton);
        controlPanel.add(plotButton);
        controlPanel.add(saveButton);
        controlPanel.add(capacityLabel);
        controlPanel.add(capacityField);

        dataTabbedPane = new JTabbedPane();
        dataTabbedPane.addTab("文本视图", dataScrollPane);
        dataTabbedPane.addTab("表格视图", new JScrollPane(dataTable));
        JPanel plotTab = new JPanel(new BorderLayout());
        plotTab.add(scatterPlotPanel, BorderLayout.CENTER);
        dataTabbedPane.addTab("散点图", plotTab);
        dataPanel.setBorder(BorderFactory.createTitledBorder("数据视图"));
        dataPanel.add(dataTabbedPane, BorderLayout.CENTER);

        resultPanel.setBorder(BorderFactory.createTitledBorder("求解结果"));
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);
        resultPanel.add(backButton, BorderLayout.SOUTH);

        mainPanel.add(controlPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dataPanel, resultPanel);
        splitPane.setResizeWeight(0.6);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        add(mainPanel);
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
                statusLabel.setText("已加载文件: " + file.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "读取文件失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("文件读取失败");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "文件格式错误: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("文件格式错误");
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

            int itemSetCount = Integer.parseInt(parts[0]);
            capacity = Integer.parseInt(parts[1]);

            itemSets.clear();

            for (int i = 0; i < itemSetCount; i++) {
                Item[] items = new Item[3];
                for (int j = 0; j < 3; j++) {
                    String line = reader.readLine();
                    if (line == null) {
                        throw new IllegalArgumentException(
                                "文件过早结束: 期望第" + (i * 3 + j + 2) + "行");
                    }

                    String[] itemParts = line.trim().split("\\s+");
                    if (itemParts.length != 2) {
                        throw new IllegalArgumentException("第" + (i * 3 + j + 2) + "行格式错误");
                    }

                    int value = Integer.parseInt(itemParts[0]);
                    int weight = Integer.parseInt(itemParts[1]);
                    items[j] = new Item(value, weight);
                }

                itemSets.add(new ItemSet(i + 1, items[0], items[1], items[2]));
            }

            capacityField.setText(String.valueOf(capacity));
        }
    }

    /**
     * 更新数据显示
     */
    private void updateDataDisplay() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("项集数量: %d, 背包容量: %d%n%n", itemSets.size(), capacity));
        sb.append("项集ID\t物品1(价值,重量)\t物品2(价值,重量)\t物品3(价值,重量)\t第三项价值/重量\n");

        for (ItemSet set : itemSets) {
            sb.append(String.format("%d\t(%d,%d)\t\t(%d,%d)\t\t(%d,%d)\t\t%.2f%n",
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
                    String.format("%.2f", set.getThirdItemRatio())
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

        try {
            currentSolution = DPSolver.solve(itemSets, capacity);
            displaySolution();
            statusLabel.setText(
                    "求解完成，耗时: " + (currentSolution.solveTime / 1000000.0) + "ms");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "求解失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            statusLabel.setText("求解失败");
        }
    }

    /**
     * 显示解决方案
     */
    private void displaySolution() {
        if (currentSolution == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== D{0-1}背包问题求解结果 ===\n\n");
        sb.append(String.format("背包容量: %d%n", capacity));
        sb.append(String.format("项集数量: %d%n", itemSets.size()));
        sb.append(String.format("最优总价值: %d%n", currentSolution.optimalValue));
        sb.append(String.format("求解时间: %.3f ms%n%n", currentSolution.solveTime / 1000000.0));

        sb.append("各项集选择详情:\n");
        sb.append("项集ID\t选择\t价值\t重量\n");

        int totalWeight = 0;
        for (int i = 0; i < itemSets.size(); i++) {
            ItemSet set = itemSets.get(i);
            int selection = currentSolution.selections.get(i);

            if (selection == 0) {
                sb.append(String.format("%d\t不选\t0\t0%n", set.setId));
            } else {
                Item selectedItem = set.items[selection - 1];
                totalWeight += selectedItem.weight;
                sb.append(String.format("%d\t物品%d\t%d\t%d%n",
                        set.setId, selection, selectedItem.value, selectedItem.weight));
            }
        }

        sb.append(String.format("%n总重量: %d (容量: %d)%n", totalWeight, capacity));
        sb.append(String.format("背包利用率: %.2f%%%n", (totalWeight * 100.0 / capacity)));

        resultTextArea.setText(sb.toString());
        backButton.setVisible(true);
    }

    /**
     * 返回初始界面
     */
    private void resetToInitialState() {
        resultTextArea.setText("");
        currentSolution = null;
        backButton.setVisible(false);
        statusLabel.setText("已返回初始界面");
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

        itemSets.sort(new Comparator<ItemSet>() {
            @Override
            public int compare(ItemSet s1, ItemSet s2) {
                return Double.compare(s2.getThirdItemRatio(), s1.getThirdItemRatio());
            }
        });

        for (int i = 0; i < itemSets.size(); i++) {
            itemSets.get(i).setId = i + 1;
        }

        updateDataDisplay();
        statusLabel.setText("已按第三项价值重量比非递增排序");
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
        statusLabel.setText("已切换到散点图视图");
    }

    /**
     * 保存结果
     */
    private void saveResult() {
        if (currentSolution == null) {
            JOptionPane.showMessageDialog(this, "请先求解问题",
                    "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存结果");
        fileChooser.setFileFilter(new FileNameExtensionFilter("文本文件 (*.txt)", "txt"));
        fileChooser.setSelectedFile(new File("D01KP_result_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("D{0-1}背包问题求解结果");
                writer.println("生成时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                writer.println("========================================");
                writer.println();
                writer.println("问题参数:");
                writer.println("背包容量: " + capacity);
                writer.println("项集数量: " + itemSets.size());
                writer.println();
                writer.println("求解结果:");
                writer.println("最优总价值: " + currentSolution.optimalValue);
                writer.println("求解时间: " + (currentSolution.solveTime / 1000000.0) + " ms");
                writer.println();
                writer.println("详细选择:");
                writer.println("项集ID\t选择\t价值\t重量");

                for (int i = 0; i < itemSets.size(); i++) {
                    ItemSet set = itemSets.get(i);
                    int selection = currentSolution.selections.get(i);

                    if (selection == 0) {
                        writer.printf("%d\t不选\t0\t0%n", set.setId);
                    } else {
                        Item selectedItem = set.items[selection - 1];
                        writer.printf("%d\t物品%d\t%d\t%d%n",
                                set.setId, selection, selectedItem.value, selectedItem.weight);
                    }
                }

                JOptionPane.showMessageDialog(this, "结果已保存到: " + file.getAbsolutePath(),
                        "成功", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("结果已保存");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("保存失败");
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
