package tetris.heuristic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import tetris.State;
import tetris.feature.Feature;
import tetris.feature.HoleFeature;
import tetris.feature.RowsClearedFeature;
import tetris.feature.TotalHeightFeature;
import tetris.feature.UnevenFeature;


public class Heuristic {
  private static final HashMap<Integer, Class<?>> FEATUREMAP = initializeFeatures();
  private final int size;
  private double[] weights;
  private ArrayList<Feature> features;

  private static HashMap<Integer, Class<?>> initializeFeatures() {
    HashMap<Integer, Class<?>> map = new HashMap<Integer, Class<?>>();
    map.put(1, HoleFeature.class);
    map.put(2, RowsClearedFeature.class);
    map.put(3, TotalHeightFeature.class);
    map.put(4, UnevenFeature.class);
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

  public Heuristic(ArrayList<Feature> features, double[] heuristicArray) {
    this.features = features;
    this.size = this.features.size();
    this.weights = heuristicArray;

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

  public static Integer getFeatureIndex(Feature feature) {
    for (Entry<Integer, Class<?>> entry: FEATUREMAP.entrySet()) {
      if(feature.getClass() == entry.getValue()) {
        return entry.getKey();
      }
    }
    return null;
  }

  public void save(File file) {
    StringBuilder stringBuilder = new StringBuilder();
    List<String> featureString = features.stream()
                                 .map(feature -> getFeatureIndex(feature).toString())
                                 .collect(Collectors.toList());
    stringBuilder.append(String.join(",", featureString));
    stringBuilder.append("\n");
    for (int i = 0; i < weights.length; i++) {
      stringBuilder.append(weights[i]);
      if (i != weights.length-1) {
        stringBuilder.append(",");
      }
    }

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(file));
      writer.write(stringBuilder.toString());
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
