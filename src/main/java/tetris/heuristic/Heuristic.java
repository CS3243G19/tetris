package tetris.heuristic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import tetris.NextState;
import tetris.feature.ColTransitionsFeature;
import tetris.feature.Feature;
import tetris.feature.HoleFeature;
import tetris.feature.RowTransitionsFeature;
import tetris.feature.RowsClearedFeature;
import tetris.feature.WellFeature;


public class Heuristic implements Comparable<Heuristic>{
    private static final HashMap<Integer, Class<?>> FEATUREMAP = initializeFeatures();
    private final int size;
    private Double[] weights;
    private Double score;
    private ArrayList<Feature> features;

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
            this.weights[i] = r.nextDouble() - 0.5; // [-0.5, 0.5]
        }
    }

    public Heuristic(ArrayList<Feature> features, Double[] heuristicArray, Double score) {
        this.features = features;
        this.size = this.features.size();
        this.weights = heuristicArray;
        this.score = score;

    }

    private static HashMap<Integer, Class<?>> initializeFeatures() {
        HashMap<Integer, Class<?>> map = new HashMap<Integer, Class<?>>();
        map.put(1, RowsClearedFeature.class);
        map.put(2, HoleFeature.class);
        map.put(3, RowTransitionsFeature.class);
        map.put(4, ColTransitionsFeature.class);
        map.put(5, WellFeature.class);
        return map;
    }

    public static Integer getFeatureIndex(Feature feature) {
        for (Map.Entry<Integer, Class<?>> entry: FEATUREMAP.entrySet()) {
            if(feature.getClass() == entry.getValue()) {
                return entry.getKey();
            }
        }
        return null;
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

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }

    @Override
    public int compareTo (Heuristic o) {
        // We invert for decremental sorting
        return Double.compare(o.getScore(), this.getScore());
    }
}
