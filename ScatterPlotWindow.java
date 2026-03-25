package bag;

import javax.swing.*;
import java.awt.*;

/**
 * 独立窗口形式的散点图（可选，与主界面「散点图」标签共用同一绘制面板逻辑）
 */
public class ScatterPlotWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * @param itemSets 项集数据
     */
    public ScatterPlotWindow(java.util.List<DZeroOneKPSolver.ItemSet> itemSets) {
        setTitle("D{0-1}KP数据散点图");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        D01kpScatterPlotPanel panel = new D01kpScatterPlotPanel();
        panel.setItemSets(itemSets);
        add(panel, BorderLayout.CENTER);
    }
}
