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


public class Heuristic {
    private final int size;
    private Double[] weights;
    private ArrayList<Feature> features;

    /**
     * Initialize new Heuristic with random weights
     * @param features
     */
    public Heuristic(ArrayList<Feature> features) {
        this.features = features;
        this.size = this.features.size();
        Random r = new Random();
        this.weights = new Double[this.size];
        for (int i = 0; i < this.size; i++) {
            this.weights[i] = r.nextDouble() - 0.5; // [-0.5, 0.5]
        }
    }

    /**
     * Initialize new Heuristic with given weights
     * @param features
     * @param heuristicArray
     */
    public Heuristic(ArrayList<Feature> features, Double[] heuristicArray) {
        this.features = features;
        this.size = this.features.size();
        this.weights = heuristicArray;
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

    public ArrayList<Feature> getFeatures() {
        return features;
    }

}
