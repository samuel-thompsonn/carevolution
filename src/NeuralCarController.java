import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralCarController implements CarController{

  public static final int WHEEL_TURN_PER_SECOND = 90;
  private List<double[][]> myWeights;
  private CarVisualizer myCar;
  private double myScore;

  public NeuralCarController(CarVisualizer car) {
    myCar = car;
    myCar.subscribe(this);
    myWeights = initializeWeights(new int[]{8,7,7},6,3);
  }

  private NeuralCarController(CarVisualizer car, List<double[][]> parentWeights) {
    myCar = car;
    myCar.subscribe(this);
    myWeights = mutateWeights(parentWeights, 0.035);
  }

  private List<double[][]> mutateWeights(List<double[][]> parentWeights, double variationProportion) {
    List<double[][]> returnedWeights = new ArrayList<>();
    for (double[][] weightSet : parentWeights) {
      double[][] newWeights = new double[weightSet.length][weightSet[0].length];
      for (int i = 0; i < weightSet.length; i ++) {
        for (int j = 0; j < weightSet[0].length; j ++) {
          Random rand = new Random();
          double normalizedVariation = (rand.nextDouble() * 2) - 1.0;
          double randomOffset = (rand.nextDouble() * 2) - 1.0;

          newWeights[i][j] = weightSet[i][j] + (variationProportion * normalizedVariation * newWeights[i][j]);
          newWeights[i][j] += randomOffset * variationProportion;
          //System.out.printf("(%.02f -> %.02f |%.02f)",weightSet[i][j],newWeights[i][j],randomOffset);
        }
      }
      returnedWeights.add(newWeights);
    }
    //System.out.println();
    return returnedWeights;
  }

  private List<double[][]> initializeWeights(int[] layerSizes, int numInputs, int numOutputs) {
    List<double[][]> returnedList = new ArrayList<>();
    int prevLayerSize = numInputs;
    int[] allLayers = new int[layerSizes.length+1];
    for (int i = 0; i < layerSizes.length; i ++) {
      allLayers[i] = layerSizes[i];
    }
    allLayers[layerSizes.length] = numOutputs;
    for (int numNeurons : allLayers) {
      double[][] layerWeights = new double[numNeurons][prevLayerSize];
      for (int i = 0; i < numNeurons; i ++) {
        for (int j = 0; j < prevLayerSize; j ++) {
          layerWeights[i][j] = (new Random().nextDouble() * 2) - 1.0;
        }
      }
      prevLayerSize = numNeurons;
      returnedList.add(layerWeights);
    }
    return returnedList;
  }

  @Override
  public int compareScores(CarController other) {
    return Double.compare(getScore(),other.getScore());
  }

  @Override
  public CarController produceOffspring(CarVisualizer car) {
    //Problem: I was accidentally using the wrong constructor.
    return new NeuralCarController(car,new ArrayList<>(myWeights));
  }

  @Override
  public void addToScore() {

  }

  @Override
  public void manipulateCar(double elapsedTime) {
    readCarParams();
    double[] actions = calculateMovements(readCarParams());
    myCar.pressPedal(actions[0]);
    if (actions[1] > 0) {
      myCar.turnWheel(-WHEEL_TURN_PER_SECOND * elapsedTime);
    }
    if (actions[2] > 0) {
      myCar.turnWheel(WHEEL_TURN_PER_SECOND * elapsedTime);
    }
  }

  @Override
  public double getScore() {
    return myScore;
  }

  private double[] readCarParams() {
    return new double[]{
            myCar.getPedalPress(),
            myCar.getWheelTurn() / 120.0,
            myCar.getForwardDistance() / CarVisualizer.SENSOR_LENGTH,
            myCar.getLeftDistance() / CarVisualizer.SENSOR_LENGTH,
            myCar.getRightDistance() / CarVisualizer.SENSOR_LENGTH,
            myCar.getSpeed() / 200.0
    };
  }

  private double[] calculateMovements(double[] inputs) {
    double[] layerResult = inputs;
    for (double[][] weightSet : myWeights) {
      layerResult = LinearUtil.matVecMult(weightSet,layerResult);
      applyNeuronThresholds(layerResult);
    }
    return layerResult;
  }

  private void applyNeuronThresholds(double[] layerResult) {
    for (int i = 0; i < layerResult.length; i ++) {
      layerResult[i] = Math.max(layerResult[i],0);
      if (layerResult[i] != 0) {
        layerResult[i] = 1;
      }
    }
  }

  @Override
  public void reactToPointGain(double pointGain) {
    myScore += pointGain;
  }
}
