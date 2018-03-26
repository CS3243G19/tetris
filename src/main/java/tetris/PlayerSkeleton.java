package tetris;

import tetris.feature.*;
import tetris.heuristic.Heuristic;

import tetris.scorer.Scorer;

import java.util.ArrayList;

public class PlayerSkeleton {
  public static void main(String[] args) {
    State state = new State();
    new TFrame(state);
    PlayerSkeleton p = new PlayerSkeleton();
    ArrayList<Feature> features = new ArrayList<Feature>();
    features.add(new HoleFeature());
    features.add(new HoleSquaredFeature());
    features.add(new MaxHoleHeightFeature());
    features.add(new RowsClearedFeature());
    features.add(new TotalHeightFeature());
    features.add(new UnevenFeature());
    features.add(new MaxHeightFeature());
    features.add(new BlocksOnHoleFeature());
    features.add(new WellFeature());
    features.add(new RowTransitionsFeature());
    features.add(new ColTransitionsFeature());

    Heuristic heuristic = new Heuristic(features);
    Scorer scorer = new Scorer(heuristic);
    scorer.play(true);
    System.out.printf("Rows cleared: %d", scorer.getLatestScore());
  }
}
