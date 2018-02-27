package tetris.heuristic;

import tetris.feature.Feature;
import tetris.State;


import java.util.ArrayList;


public class Heuristic {
  private ArrayList<Double> weights;
  private ArrayList<Feature> features;

  public Heuristic(ArrayList<Feature> features) {
    this.features = features;
    ArrayList<Double> weights = new ArrayList<Double>(features.size());
    for (Double weight : weights) {
      weight = Math.random() * 2 - 1;
    }

    this.weights = new ArrayList<Double>();
  }
  public double getValue(State s) {
    double sum = 0;
    for (int i = 0; i < this.weights.size(); i++) {
      sum += this.weights.get(i) * this.features.get(i).getValue(s);
    }

    return sum;
  }
}
