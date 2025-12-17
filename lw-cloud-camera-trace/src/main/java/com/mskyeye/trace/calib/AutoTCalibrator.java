package com.mskyeye.trace.calib;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * =========================================================
 * 雷达-光电 T 值自动标定器（Auto T Calibrator）
 *
 * 一、设计目标
 * ---------------------------------------------------------
 * 1. 通过无人机在多个方向飞行，自动采集雷达与光电的 T 偏差
 * 2. 严格区分【有效采样阶段】与【返航 / 非标定阶段】
 * 3. 防止返航数据污染下一个方向的数据
 * 4. 基于采样数据拟合 T(distance, azimuth) 的补偿模型
 * 5. 标定结果支持自动持久化与下次启动加载
 *
 * 二、核心思想
 * ---------------------------------------------------------
 * 只有在：
 *   running == true
 *   collectEnabled == true
 * 这两个条件同时满足时，才允许采集数据
 *
 * abortDirection() 的唯一目的：
 *   立即切断采样通道，确保返航数据不被采集
 *
 * =========================================================
 */
public class AutoTCalibrator {

    /* =====================================================
     * 一、内部数据结构定义
     * ===================================================== */

    /**
     * 单条标定采样数据
     *
     * 含义：
     *  无人机在某一时刻的
     *  - 雷达测距
     *  - 雷达方位角
     *  - 光电T - 雷达T 的偏差
     */
    static class CalibSample {

        /** 雷达测得的目标距离（单位：米） */
        public double distance;

        /** 雷达测得的方位角（单位：度，范围 0~360） */
        public double az;

        /** T 偏差 = 光电T - 雷达T（单位：度） */
        public double deltaT;

        /** Jackson 反序列化用的空构造 */
        public CalibSample() {}

        /**
         * 构造一条标定样本
         *
         * @param distance 雷达距离（米）
         * @param az       雷达方位角（度，0~360）
         * @param deltaT   光电T - 雷达T（度）
         */
        public CalibSample(double distance, double az, double deltaT) {
            this.distance = distance;
            this.az = az;
            this.deltaT = deltaT;
        }
    }

    /**
     * 标定结果模型
     *
     * 数学模型：
     *   deltaT = a
     *          + b * distance
     *          + c * sin(az)
     *          + d * cos(az)
     *
     * 说明：
     *  - a：整体零偏
     *  - b：距离相关误差
     *  - c / d：方位周期误差
     */
    public static class CalibResult {

        /** 常数项 */
        public double a;

        /** 距离系数 */
        public double b;

        /** sin(az) 系数 */
        public double c;

        /** cos(az) 系数 */
        public double d;

        /** 当前结果是否有效 */
        public boolean valid = false;
    }

    /* =====================================================
     * 二、运行状态变量
     * ===================================================== */

    /**
     * 是否处于一次标定任务中
     *
     * true  ：startCalib() 已调用，finishCalib() 尚未调用
     * false ：未标定 / 标定已结束
     */
    private boolean running = false;

    /**
     * 是否允许采集样本（最关键的安全开关）
     *
     * true  ：当前处于“有效方向飞行阶段”
     * false ：返航 / 未开始 / 已结束
     */
    private boolean collectEnabled = false;

    /**
     * 当前采样方向名称（仅用于日志和调试）
     *
     * 示例：EAST / WEST / NORTH / SOUTH
     */
    private String currentDirection = null;

    /**
     * 当前标定任务累计采集的所有样本
     */
    private final List<CalibSample> samples = new ArrayList<>();

    /**
     * 当前标定结果
     */
    private CalibResult calibResult = new CalibResult();

    /* =====================================================
     * 三、持久化相关
     * ===================================================== */

    /** 标定结果保存文件 */
    private static final File SAVE_FILE = new File("t_calib.json");

    /** JSON 序列化工具 */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * 构造函数
     *
     * 功能：
     *  - 系统启动时自动尝试加载历史标定结果
     */
    public AutoTCalibrator() {
        load();
    }

    /* =====================================================
     * 四、标定流程控制接口
     * ===================================================== */

    /**
     * 开始一次完整的标定任务
     *
     * 调用时机：
     *  - 用户点击“开始标定”
     *  - 无人机尚未起飞前
     *
     * 注意：
     *  - 只调用一次
     *  - 不会立即采样
     */
    public void startCalib() {
        samples.clear();
        running = true;
        collectEnabled = false;
        calibResult.valid = false;

        System.out.println("[CALIB] start calibration");
    }

    /**
     * 开始某一个方向的采样
     *
     * 调用时机：
     *  - 无人机已经进入稳定飞行
     *  - 正在执行某一个方向（如 EAST）
     *
     * @param directionName 方向名称，仅用于日志
     */
    public void startDirection(String directionName) {
        if (!running) return;

        this.collectEnabled = true;
        this.currentDirection = directionName;

        System.out.println("[CALIB] start direction: " + directionName);
    }

    /**
     * 结束当前方向的采样
     *
     * ⚠️ 极其重要的方法
     *
     * 调用时机：
     *  - 当前方向飞行完成
     *  - 无人机即将返航或转向
     *
     * 效果：
     *  - 立即禁止采样
     *  - 返航期间的所有数据都会被丢弃
     */
    public void abortDirection() {
        if (!running) return;

        this.collectEnabled = false;
        System.out.println("[CALIB] end direction: " + currentDirection);

        this.currentDirection = null;
    }

    /**
     * 采集一条标定样本
     *
     * 调用频率：
     *  - 建议 2~4 秒一次
     *  - 可由定时任务或数据回调触发
     *
     * @param distance 雷达测得的目标距离（米）
     * @param az       雷达测得的方位角（度）
     * @param deltaT  光电T - 雷达T（度）
     */
    public void collectSample(double distance, double az, double deltaT) {
        if (!running) return;

        // ================= 核心防线 =================
        // 非方向飞行阶段（返航 / 切换方向）一律丢弃
        if (!collectEnabled) return;

        // 可选稳定性判断（如 PT 角速度）
        if (!isStable()) return;

        samples.add(new CalibSample(distance, normalize360(az), deltaT));

        System.out.println("[CALIB] sample collected, total=" + samples.size());
    }

    /**
     * 结束整个标定任务
     *
     * 调用时机：
     *  - 四个方向全部完成
     *
     * 功能：
     *  - 拟合补偿模型
     *  - 持久化结果
     *
     * @return 是否标定成功
     */
    public boolean finishCalib() {
        if (!running) return false;

        running = false;
        collectEnabled = false;

        if (samples.size() < 10) {
            System.err.println("[CALIB] not enough samples");
            return false;
        }

        solveModel();
        calibResult.valid = true;
        save();

        System.out.println("[CALIB] calibration finished");
        return true;
    }

    /* =====================================================
     * 五、运行期补偿接口
     * ===================================================== */

    /**
     * 根据当前雷达数据，计算 T 补偿值
     *
     * @param distance 雷达距离（米）
     * @param az       雷达方位角（度）
     * @return T 补偿量（度）
     */
    public double getCompensatedT(double distance, double az) {
        if (!calibResult.valid) return 0.0;

        double rad = Math.toRadians(az);
        return calibResult.a
                + calibResult.b * distance
                + calibResult.c * Math.sin(rad)
                + calibResult.d * Math.cos(rad);
    }

    /* =====================================================
     * 六、内部算法实现
     * ===================================================== */

    /**
     * 使用最小二乘法拟合补偿模型参数
     *
     * 模型：
     *   deltaT = a + b*distance + c*sin(az) + d*cos(az)
     */
    private void solveModel() {
        int n = samples.size();
        double[][] A = new double[n][4];
        double[] Y = new double[n];

        for (int i = 0; i < n; i++) {
            CalibSample s = samples.get(i);
            double rad = Math.toRadians(s.az);

            A[i][0] = 1.0;
            A[i][1] = s.distance;
            A[i][2] = Math.sin(rad);
            A[i][3] = Math.cos(rad);

            Y[i] = s.deltaT;
        }

        // 最小二乘求解参数向量 [a, b, c, d]
        double[] x = LeastSquares.solve(A, Y);

        calibResult.a = x[0];
        calibResult.b = x[1];
        calibResult.c = x[2];
        calibResult.d = x[3];
    }

    /* =====================================================
     * 七、工具方法
     * ===================================================== */

    /**
     * 判断当前数据是否稳定
     *
     * 可扩展：
     *  - PT 角速度阈值
     *  - 无人机速度阈值
     */
    private boolean isStable() {
        return true;
    }

    /**
     * 将角度归一化到 [0, 360)
     */
    private double normalize360(double deg) {
        return ((deg % 360) + 360) % 360;
    }

    /**
     * 保存标定结果到磁盘
     */
    private void save() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(SAVE_FILE, calibResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从磁盘加载历史标定结果
     */
    private void load() {
        try {
            if (SAVE_FILE.exists()) {
                calibResult = mapper.readValue(SAVE_FILE, CalibResult.class);
                System.out.println("[CALIB] loaded from disk");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
