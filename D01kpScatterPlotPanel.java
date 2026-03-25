package bag;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * D{0-1}KP 数据散点图面板（重量为横轴，价值为纵轴）
 */
public class D01kpScatterPlotPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int PADDING = 60;
    private static final int LABEL_PADDING = 40;
    private static final int POINT_WIDTH = 8;

    private final Color gridColor = new Color(200, 200, 200, 200);
    private final Color[] itemColors = {Color.RED, Color.GREEN, Color.BLUE};

    private List<DZeroOneKPSolver.ItemSet> itemSets = Collections.emptyList();

    public D01kpScatterPlotPanel() {
        setBackground(Color.WHITE);
        setMinimumSize(new Dimension(400, 300));
    }

    /**
     * 绑定当前项集数据并刷新绘图
     *
     * @param sets 项集列表，可为 null
     */
    public void setItemSets(List<DZeroOneKPSolver.ItemSet> sets) {
        if (sets == null || sets.isEmpty()) {
            this.itemSets = Collections.emptyList();
        } else {
            this.itemSets = new ArrayList<>(sets);
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        int maxWeight = 0;
        int maxValue = 0;
        for (DZeroOneKPSolver.ItemSet set : itemSets) {
            for (int i = 0; i < 3; i++) {
                DZeroOneKPSolver.Item item = set.items[i];
                if (item.weight > maxWeight) {
                    maxWeight = item.weight;
                }
                if (item.value > maxValue) {
                    maxValue = item.value;
                }
            }
        }
        if (maxWeight == 0) {
            maxWeight = 1;
        }
        if (maxValue == 0) {
            maxValue = 1;
        }
        maxWeight = (int) (maxWeight * 1.1);
        maxValue = (int) (maxValue * 1.1);

        drawGridAndAxes(g2, width, height, maxWeight, maxValue);
        drawScatterPoints(g2, width, height, maxWeight, maxValue);
        drawLegend(g2, width, height);

        if (itemSets.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
            String hint = "请先打开数据文件";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(hint, (width - fm.stringWidth(hint)) / 2, height / 2);
        }
    }

    private void drawGridAndAxes(Graphics2D g2, int width, int height,
            int maxWeight, int maxValue) {
        g2.setColor(Color.WHITE);
        g2.fillRect(PADDING + LABEL_PADDING, PADDING,
                width - 2 * PADDING - LABEL_PADDING,
                height - 2 * PADDING - LABEL_PADDING);

        g2.setColor(gridColor);
        int xLabelCount = 10;
        for (int i = 0; i <= xLabelCount; i++) {
            int x = PADDING + LABEL_PADDING
                    + (i * (width - 2 * PADDING - LABEL_PADDING)) / xLabelCount;
            g2.drawLine(x, height - PADDING - LABEL_PADDING, x, PADDING);
            String xLabel = String.valueOf((int) (maxWeight * (i / (double) xLabelCount)));
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(xLabel);
            g2.drawString(xLabel, x - labelWidth / 2, height - PADDING + 20);
        }

        int yLabelCount = 10;
        for (int i = 0; i <= yLabelCount; i++) {
            int y = height - PADDING - LABEL_PADDING
                    - (i * (height - 2 * PADDING - LABEL_PADDING)) / yLabelCount;
            g2.drawLine(PADDING + LABEL_PADDING, y, width - PADDING, y);
            String yLabel = String.valueOf((int) (maxValue * (i / (double) yLabelCount)));
            FontMetrics metrics = g2.getFontMetrics();
            int labelWidth = metrics.stringWidth(yLabel);
            g2.drawString(yLabel, PADDING + LABEL_PADDING - labelWidth - 5,
                    y + (metrics.getHeight() / 2) - 3);
        }

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));

        g2.drawLine(PADDING + LABEL_PADDING, height - PADDING - LABEL_PADDING,
                width - PADDING, height - PADDING - LABEL_PADDING);
        g2.drawLine(PADDING + LABEL_PADDING, height - PADDING - LABEL_PADDING,
                PADDING + LABEL_PADDING, PADDING);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        FontMetrics metrics = g2.getFontMetrics();

        String xAxisLabel = "重量 (Weight)";
        int xLabelWidth = metrics.stringWidth(xAxisLabel);
        g2.drawString(xAxisLabel, (width - xLabelWidth) / 2, height - 10);

        String yAxisLabel = "价值 (Value)";
        int yLabelWidth = metrics.stringWidth(yAxisLabel);
        g2.rotate(-Math.PI / 2);
        g2.drawString(yAxisLabel, -height / 2 - yLabelWidth / 2, PADDING - 20);
        g2.rotate(Math.PI / 2);
    }

    private void drawScatterPoints(Graphics2D g2, int width, int height,
            int maxWeight, int maxValue) {
        if (itemSets.isEmpty()) {
            return;
        }

        int plotWidth = width - 2 * PADDING - LABEL_PADDING;
        int plotHeight = height - 2 * PADDING - LABEL_PADDING;
        int plotX = PADDING + LABEL_PADDING;
        int plotY = PADDING;

        for (DZeroOneKPSolver.ItemSet set : itemSets) {
            for (int i = 0; i < 3; i++) {
                DZeroOneKPSolver.Item item = set.items[i];

                int x = plotX + (int) ((item.weight / (double) maxWeight) * plotWidth);
                int y = plotY + plotHeight
                        - (int) ((item.value / (double) maxValue) * plotHeight);

                g2.setColor(itemColors[i]);
                g2.fillOval(x - POINT_WIDTH / 2, y - POINT_WIDTH / 2, POINT_WIDTH, POINT_WIDTH);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                String label = String.format("S%d-I%d", set.setId, i + 1);
                FontMetrics metrics = g2.getFontMetrics();
                int lw = metrics.stringWidth(label);
                g2.drawString(label, x - lw / 2, y - POINT_WIDTH);
            }
        }
    }

    private void drawLegend(Graphics2D g2, int width, int height) {
        int legendX = width - 150;
        int legendY = PADDING + 20;

        g2.setColor(Color.WHITE);
        g2.fillRect(legendX - 10, legendY - 20, 140, 110);
        g2.setColor(Color.BLACK);
        g2.drawRect(legendX - 10, legendY - 20, 140, 110);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g2.drawString("图例", legendX, legendY);

        String[] labels = {"物品1", "物品2", "物品3"};

        for (int i = 0; i < 3; i++) {
            g2.setColor(itemColors[i]);
            g2.fillOval(legendX, legendY + 20 + i * 25, 10, 10);

            g2.setColor(Color.BLACK);
            g2.drawString(labels[i], legendX + 20, legendY + 28 + i * 25);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 500);
    }
}
