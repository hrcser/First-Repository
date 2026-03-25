package bag;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * 测试数据生成器
 * 生成符合D{0-1}KP格式的测试数据
 */
public class TestDataGenerator {

    private static final int DEFAULT_SEED = 42;
    private static final String SMALL_FILE_NAME = "test_data_small.txt";
    private static final String MEDIUM_FILE_NAME = "test_data_medium.txt";
    private static final String LARGE_FILE_NAME = "test_data_large.txt";
    private static final String EXAMPLE_FILE_NAME = "d01kp_example.txt";

    /**
     * 生成随机D{0-1}KP测试数据
     *
     * @param filename 输出文件名
     * @param itemSetCount 项集数量
     * @param maxCapacity 最大容量
     * @param maxValue 最大价值
     * @param maxWeight 最大重量
     */
    public static void generateTestData(String filename, int itemSetCount,
            int maxCapacity, int maxValue, int maxWeight) {
        Random rand = new Random(DEFAULT_SEED);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            int capacity = rand.nextInt(maxCapacity / 2) + maxCapacity / 2;
            writer.println(itemSetCount + " " + capacity);

            for (int i = 0; i < itemSetCount; i++) {
                int value1 = rand.nextInt(maxValue) + 1;
                int weight1 = rand.nextInt(maxWeight) + 1;

                int value2 = rand.nextInt(maxValue) + 1;
                int weight2 = rand.nextInt(maxWeight) + 1;

                int value3 = value1 + value2;
                int maxWeight3 = weight1 + weight2 - 1;
                int weight3 = rand.nextInt(maxWeight3) + 1;

                writer.println(value1 + " " + weight1);
                writer.println(value2 + " " + weight2);
                writer.println(value3 + " " + weight3);
            }

            System.out.println("测试数据已生成到: " + filename);
            System.out.println("项集数量: " + itemSetCount);
            System.out.println("背包容量: " + capacity);

        } catch (IOException e) {
            System.err.println("生成测试数据失败: " + e.getMessage());
        }
    }

    /**
     * 生成示例测试文件
     */
    public static void generateSampleData() {
        generateTestData(SMALL_FILE_NAME, 5, 50, 30, 20);
        generateTestData(MEDIUM_FILE_NAME, 20, 200, 50, 30);
        generateTestData(LARGE_FILE_NAME, 100, 1000, 100, 80);
    }

    /**
     * 创建预定义的测试数据文件
     */
    public static void createPredefinedTestFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EXAMPLE_FILE_NAME))) {
            writer.println("3 10");

            writer.println("2 3");
            writer.println("3 4");
            writer.println("5 6");

            writer.println("4 2");
            writer.println("5 3");
            writer.println("9 4");

            writer.println("3 5");
            writer.println("2 3");
            writer.println("5 7");

            System.out.println("预定义测试文件已创建: " + EXAMPLE_FILE_NAME);

        } catch (IOException e) {
            System.err.println("创建测试文件失败: " + e.getMessage());
        }
    }

    /**
     * 单元测试
     */
    public static void testDPSolver() {
        System.out.println("=== D{0-1}背包问题求解器单元测试 ===\n");

        java.util.List<DZeroOneKPSolver.ItemSet> itemSets = new java.util.ArrayList<>();

        DZeroOneKPSolver.Item item1_1 = new DZeroOneKPSolver.Item(2, 3);
        DZeroOneKPSolver.Item item1_2 = new DZeroOneKPSolver.Item(3, 4);
        DZeroOneKPSolver.Item item1_3 = new DZeroOneKPSolver.Item(5, 6);
        itemSets.add(new DZeroOneKPSolver.ItemSet(1, item1_1, item1_2, item1_3));

        DZeroOneKPSolver.Item item2_1 = new DZeroOneKPSolver.Item(4, 2);
        DZeroOneKPSolver.Item item2_2 = new DZeroOneKPSolver.Item(5, 3);
        DZeroOneKPSolver.Item item2_3 = new DZeroOneKPSolver.Item(9, 4);
        itemSets.add(new DZeroOneKPSolver.ItemSet(2, item2_1, item2_2, item2_3));

        DZeroOneKPSolver.Item item3_1 = new DZeroOneKPSolver.Item(3, 5);
        DZeroOneKPSolver.Item item3_2 = new DZeroOneKPSolver.Item(2, 3);
        DZeroOneKPSolver.Item item3_3 = new DZeroOneKPSolver.Item(5, 7);
        itemSets.add(new DZeroOneKPSolver.ItemSet(3, item3_1, item3_2, item3_3));

        int capacity = 10;

        DZeroOneKPSolver.Solution solution =
                DZeroOneKPSolver.DPSolver.solve(itemSets, capacity);

        System.out.println("测试用例1:");
        System.out.println("项集数量: " + itemSets.size());
        System.out.println("背包容量: " + capacity);
        System.out.println("最优总价值: " + solution.optimalValue);
        System.out.println("求解时间: " + solution.solveTime + " ns ("
                + (solution.solveTime / 1000000.0) + " ms)");
        System.out.println("选择方案: " + solution.selections);

        System.out.println("\n详细计算:");
        int totalWeight = 0;
        int totalValue = 0;
        for (int i = 0; i < itemSets.size(); i++) {
            int selection = solution.selections.get(i);
            if (selection != 0) {
                DZeroOneKPSolver.Item item = itemSets.get(i).items[selection - 1];
                totalValue += item.value;
                totalWeight += item.weight;
                System.out.printf("项集%d: 选择物品%d (价值=%d, 重量=%d)%n",
                        i + 1, selection, item.value, item.weight);
            } else {
                System.out.printf("项集%d: 不选%n", i + 1);
            }
        }
        System.out.println("总价值: " + totalValue);
        System.out.println("总重量: " + totalWeight);
        System.out.println("背包利用率: " + (totalWeight * 100.0 / capacity) + "%");

        if (totalValue == solution.optimalValue && totalWeight <= capacity) {
            System.out.println("\n✓ 测试通过!");
        } else {
            System.out.println("\n✗ 测试失败!");
        }
    }

    /**
     * 性能测试
     */
    public static void performanceTest() {
        System.out.println("\n=== 性能测试 ===\n");

        int[] testSizes = {10, 20, 50, 100, 200};
        int capacity = 1000;

        Random rand = new Random(DEFAULT_SEED);

        for (int size : testSizes) {
            java.util.List<DZeroOneKPSolver.ItemSet> itemSets = new java.util.ArrayList<>();

            for (int i = 0; i < size; i++) {
                int value1 = rand.nextInt(50) + 1;
                int weight1 = rand.nextInt(30) + 1;

                int value2 = rand.nextInt(50) + 1;
                int weight2 = rand.nextInt(30) + 1;

                int value3 = value1 + value2;
                int weight3 = rand.nextInt(weight1 + weight2 - 1) + 1;

                DZeroOneKPSolver.Item item1 = new DZeroOneKPSolver.Item(value1, weight1);
                DZeroOneKPSolver.Item item2 = new DZeroOneKPSolver.Item(value2, weight2);
                DZeroOneKPSolver.Item item3 = new DZeroOneKPSolver.Item(value3, weight3);

                itemSets.add(new DZeroOneKPSolver.ItemSet(i + 1, item1, item2, item3));
            }

            long startTime = System.nanoTime();
            DZeroOneKPSolver.Solution solution =
                    DZeroOneKPSolver.DPSolver.solve(itemSets, capacity);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            System.out.printf("项集数量: %4d, 求解时间: %8.3f ms, 最优值: %d%n",
                    size, duration / 1000000.0, solution.optimalValue);
        }
    }

    /**
     * 主方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("D{0-1}背包问题测试数据生成器");
        System.out.println("===========================\n");

        createPredefinedTestFile();
        testDPSolver();
        performanceTest();
        generateSampleData();

        System.out.println("\n测试完成！");
        System.out.println("请使用以下命令运行GUI程序:");
        System.out.println("javac bag/DZeroOneKPSolver.java bag/ScatterPlotWindow.java");
        System.out.println("java bag.DZeroOneKPSolver");
    }
}
