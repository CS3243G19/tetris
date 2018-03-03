package tetris.heuristic;

import tetris.feature.Feature;
import tetris.feature.*;

import tetris.State;
import tetris.feature.HoleFeature;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Heuristic {
  private static final HashMap<Integer, Feature> FEATUREMAP = initializeFeatures();
  private final int size;
  private double[] weights;
  private ArrayList<Feature> features;

  private static HashMap<Integer, Feature> initializeFeatures() {
    HashMap<Integer, Feature> map = new HashMap<Integer, Feature>();
    map.put(1, new HoleFeature());
    map.put(2, new RowsClearedFeature());
    map.put(3, new TotalHeightFeature());
    map.put(4, new UnevenFeature());
    return map;
  }

  public Heuristic(String filepath) {
    File file = new File(filepath);
    features = new ArrayList<Feature>();
    String[] featureArray = new String[1];
    String[] weightArray = new String[1];
    String features;
    String weights;

    try {
      FileReader fileReader = new FileReader(file);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      features = bufferedReader.readLine();
      weights = bufferedReader.readLine();
      featureArray = features.split(",");
      weightArray = weights.split(",");
    } catch (IOException e) {
      System.out.printf("Unable to read heuristic configuration from file.");
      e.printStackTrace();
    }

    this.size = featureArray.length;
    this.weights = new double[this.size];
    for (int i = 0; i < weightArray.length; i++) {
      this.weights[i] = (Double.parseDouble(weightArray[i]));
    }

    for (String feat: featureArray) {
      addFeature(feat);
    }

    if (this.weights.length != this.features.size()) {
      System.out.printf("Initialisation went wrong: weight and feature array not equal size");
    }
  }

  public Heuristic(ArrayList<Feature> features) {
    this.features = features;
    this.size = this.features.size();
    this.weights = new Random().doubles(this.size, -1, 1).toArray();
  }

  private void addFeature(String feature) {
    int featIndex = Integer.parseInt(feature);
    Feature feat = FEATUREMAP.get(featIndex);
    if (feat != null) {
      features.add(feat);
    }
  }

  public double getValue(State s) {
    double sum = 0;
    for (int i = 0; i < this.size; i++) {
      sum += this.weights[i] * this.features.get(i).getValue(s);
    }

    return sum;
  }

  public double[] getWeights() {
    return weights;
  }

  public ArrayList<Feature> getFeatures() {
    return features;
  }
}
