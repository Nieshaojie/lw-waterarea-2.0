package com.mskyeye.trace.calib;

/**
 * ===============================
 * 最小二乘求解工具类
 *
 * 解线性方程：
 *   min ||A * x - y||^2
 *
 * 使用正规方程：
 *   (A^T A) x = A^T y
 *
 * 适用场景：
 * - 小规模参数拟合（<10）
 * - 工程标定
 * ===============================
 */
public class LeastSquares {

    /**
     * 求解最小二乘问题
     *
     * @param A 设计矩阵（n x m）
     * @param y 观测值（n）
     * @return x 参数解（m）
     */
    public static double[] solve(double[][] A, double[] y) {
        int n = A.length;
        int m = A[0].length;

        // 计算 A^T * A （m x m）
        double[][] ata = new double[m][m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += A[k][i] * A[k][j];
                }
                ata[i][j] = sum;
            }
        }

        // 计算 A^T * y （m）
        double[] aty = new double[m];
        for (int i = 0; i < m; i++) {
            double sum = 0;
            for (int k = 0; k < n; k++) {
                sum += A[k][i] * y[k];
            }
            aty[i] = sum;
        }

        // 解线性方程 (A^T A) x = A^T y
        return gaussianElimination(ata, aty);
    }

    /**
     * 高斯消元法解线性方程
     */
    private static double[] gaussianElimination(double[][] A, double[] b) {
        int n = b.length;

        for (int p = 0; p < n; p++) {
            // 选主元（防止数值不稳定）
            int max = p;
            for (int i = p + 1; i < n; i++) {
                if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                    max = i;
                }
            }

            // 行交换
            double[] temp = A[p];
            A[p] = A[max];
            A[max] = temp;

            double t = b[p];
            b[p] = b[max];
            b[max] = t;

            // 奇异检查
            if (Math.abs(A[p][p]) < 1e-10) {
                throw new RuntimeException("Singular matrix in least squares");
            }

            // 消元
            for (int i = p + 1; i < n; i++) {
                double alpha = A[i][p] / A[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < n; j++) {
                    A[i][j] -= alpha * A[p][j];
                }
            }
        }

        // 回代
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0;
            for (int j = i + 1; j < n; j++) {
                sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
        }

        return x;
    }
}
