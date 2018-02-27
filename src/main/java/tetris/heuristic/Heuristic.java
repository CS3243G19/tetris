package tetris.heuristic;

import tetris.feature.Feature;
import tetris.State;


import java.util.ArrayList;
import java.util.Random;


public class Heuristic {
  private final int size;
  private double[] weights;
  private ArrayList<Feature> features;

  public Heuristic(ArrayList<Feature> features) {
    this.features = features;
    this.size = this.features.size();
    this.weights = new Random().doubles(this.size, -1, 1).toArray();

  }

  public double getValue(State s) {
    double sum = 0;
    for (int i = 0; i < this.size; i++) {
      sum += this.weights[i] * this.features.get(i).getValue(s);
    }

    return sum;
  }
}
