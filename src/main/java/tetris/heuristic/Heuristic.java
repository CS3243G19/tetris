package tetris.heuristic;

import tetris.feature.Feature;
import tetris.State;


import java.util.ArrayList;


public class Heuristic {
  private final int size;
  private ArrayList<Double> weights;
  private ArrayList<Feature> features;

  public Heuristic(ArrayList<Feature> features) {
    this.features = features;
    this.size = features.size();
    this.weights = new ArrayList<Double>(this.size);
    for (int i = 0; i < this.size; i++) {
      this.weights.set(i, Math.random() * 2 - 1);
    }
  }

  public double getValue(State s) {
    double sum = 0;
    for (int i = 0; i < this.size; i++) {
      sum += this.weights.get(i) * this.features.get(i).getValue(s);
    }

    return sum;
  }
}
