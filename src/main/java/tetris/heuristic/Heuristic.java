package tetris.heuristic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

<<<<<<< HEAD
=======
import tetris.NextState;
>>>>>>> e437644524928528297558372d460f957c56cf56
import tetris.State;
import tetris.feature.Feature;
import tetris.feature.HoleFeature;
import tetris.feature.HoleSquaredFeature;
import tetris.feature.MaxHoleHeightFeature;
import tetris.feature.RowsClearedFeature;
import tetris.feature.TotalHeightFeature;
import tetris.feature.UnevenFeature;


public class Heuristic implements Comparable<Heuristic>{
  private static final HashMap<Integer, Class<?>> FEATUREMAP = initializeFeatures();
  private final int size;
  private Double[] weights;
  private Double score;
  private ArrayList<Feature> features;

  private static HashMap<Integer, Class<?>> initializeFeatures() {
    HashMap<Integer, Class<?>> map = new HashMap<Integer, Class<?>>();
    map.put(1, HoleFeature.class);
    map.put(2, RowsClearedFeature.class);
    map.put(3, TotalHeightFeature.class);
    map.put(4, UnevenFeature.class);
    map.put(5, HoleSquaredFeature.class);
    map.put(6, MaxHoleHeightFeature.class);
    return map;
  }

  public Heuristic(File file) {
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
    this.weights = new Double[this.size];
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
    Random r = new Random();
    this.weights = new Double[this.size];
    for (int i = 0; i < this.size; i++) {
      this.weights[i] = r.nextDouble() * 10 - 5;
    }
  }

  public Heuristic(ArrayList<Feature> features, Double[] heuristicArray, Double score) {
    this.features = features;
    this.size = this.features.size();
    this.weights = heuristicArray;
    this.score = score;

  }

  private void addFeature(String feature) {
    int featIndex = Integer.parseInt(feature);
    Class<?> feat = FEATUREMAP.get(featIndex);
    if (feat != null) {
      try {
        features.add((Feature) feat.newInstance());
      } catch (Exception e) {
        e.printStackTrace();
    }
    }
  }

  public double getValue(NextState s) {
    double sum = 0;
    for (int i = 0; i < this.size; i++) {
      sum += this.weights[i] * this.features.get(i).getValue(s);
    }

    return sum;
  }

  public Double[] getWeights() {
    return weights;
  }

  public void setScore(Double score) {
    this.score = score;
  }


  public Double getScore() {
    return score;
  }

  public ArrayList<Feature> getFeatures() {
    return features;
  }

  public static Integer getFeatureIndex(Feature feature) {
    for (Entry<Integer, Class<?>> entry: FEATUREMAP.entrySet()) {
      if(feature.getClass() == entry.getValue()) {
        return entry.getKey();
      }
    }
    return null;
  }

  @Override
  public int compareTo (Heuristic o) {
    // We invert for decremental sorting
    return Double.compare(o.getScore(), this.getScore());
  }
}
