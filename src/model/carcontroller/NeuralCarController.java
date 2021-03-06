package model.carcontroller;

import model.CollisionEntity;
import model.entity.Car;
import model.entity.EvolutionCar;
import model.LinearUtil;
import model.NeuronBehavior;
import model.neuronbehavior.SigmoidBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NeuralCarController implements CarController {

  public static final int WHEEL_TURNS_PER_SECOND = 32;
  private List<double[][]> myWeights;
  private Car myCar;
  private double myScore;
  private NeuronBehavior myNeuronBehavior = new SigmoidBehavior();

  public NeuralCarController(Car car) {
    myCar = car;
    myCar.subscribe(this);
    myWeights = initRandomWeights(new int[]{8,7},6,3);
  }

  private NeuralCarController(Car car, List<double[][]> parentWeights) {
    myCar = car;
    myCar.subscribe(this);
    myWeights = mutateWeights(parentWeights, 0.075);
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

  private List<double[][]> initRandomWeights(int[] layerSizes, int numInputs, int numOutputs) {
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
  public CarController produceOffspring(Car car) {
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
    myCar.pressPedal((actions[0] * 2) - 1);
//    myCar.pressPedal(0.5);
    //myCar.turnWheel(WHEEL_TURN_PER_SECOND * elapsedTime * (actions[1]-0.5));

    myCar.turnWheel(myCar.getWheelTurn() * -1);
    double wheelTurn  = (actions[1] * 2 - 1) * EvolutionCar.MAX_WHEEL_ROTATION * 3;
    myCar.turnWheel(wheelTurn);
    myCar.pressBrakes((actions[2] * 2) - 1);
//    myCar.turnWheel((actions[1]-0.5) * EvolutionCar.MAX_WHEEL_ROTATION * elapsedTime * WHEEL_TURNS_PER_SECOND);
//    myCar.pressBrakes(actions[2]*2 - 1);
  }

  @Override
  public double getScore() {
    return myScore;
  }

  private double[] readCarParams() {
    return new double[]{
            //myCar.getPedalPress(),
//            myCar.getWheelTurn() / (EvolutionCar.MAX_WHEEL_ROTATION),
            myCar.getForwardDistance() / EvolutionCar.SENSOR_LENGTH,
            myCar.getLeftDistance() / EvolutionCar.SENSOR_LENGTH,
            myCar.getForwardLeftDistance() / EvolutionCar.SENSOR_LENGTH,
            myCar.getRightDistance() / EvolutionCar.SENSOR_LENGTH,
            myCar.getForwardRightDistance() / EvolutionCar.SENSOR_LENGTH,
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
      layerResult[i] = myNeuronBehavior.applyNeuronFunction(layerResult[i]);
    }
  }

  @Override
  public void reactToPointGain(double pointGain) {
    myScore += pointGain;
  }

  @Override
  public void reactToPositionChange(double newX, double newY) {

  }

  @Override
  public void reactToDirectionChange(double directionDegrees) {

  }

  @Override
  public void reactToRemoval(CollisionEntity entity) {
    //Doesn't care about the car being dead.
  }
}
