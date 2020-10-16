public class LinearUtil {
  public static double dotProduct(double[] x, double[] y) {
    if (x.length != y.length) {
      System.out.println("Trying to dot product uneven lengths");
      return 0;
    }
    double total = 0;
    for (int i = 0; i < x.length; i ++) {
      total += x[i] * y[i];
    }
    return total;
  }

  /**
   *
   * @param m1
   * @param m2
   * @return M1M2
   */
  public static double[][] matrixMult(double[][] m1, double[][] m2) {
    double[][] result = new double[m1.length][m2[0].length];
    for (int i = 0; i < m1.length; i ++) {
      result[i] = new double[m2[0].length];
      for (int j = 0; j < m2[0].length; j ++) {
        double[] jColumn = new double[m2.length];
        //TODO: Efficiency this n^3 mess.
        for (int k = 0; k < m2.length; k ++) {
          jColumn[k] = m2[k][j];
        }
        result[i][j] = dotProduct(m1[i],jColumn);
      }
    }
    return result;
  }

  /**
   *
   * @param m
   * @param v
   * @return Mv
   */
  public static double[] matVecMult(double[][] m, double[] v) {
    double[][] vectorMatrix = new double[v.length][1];
    for (int i = 0; i < v.length; i ++) {
      vectorMatrix[i][0] = v[i];
    }
    double[][] multResult = matrixMult(m,vectorMatrix); //The result is mx1
    double[] finalResult = new double[multResult.length];
    for (int i = 0; i < multResult.length; i ++) {
      finalResult[i] = multResult[i][0];
    }
    return finalResult;
  }
}
