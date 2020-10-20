package model.neuronbehavior;

import model.NeuronBehavior;

public class StepNeuronBehavior implements NeuronBehavior {
  @Override
  public double applyNeuronFunction(double inValue) {
    if (inValue > 0) {
      return 1;
    }
    return 0;
  }
}
