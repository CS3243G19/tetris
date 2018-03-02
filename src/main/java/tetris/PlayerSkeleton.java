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
    features.add(new RowsClearedFeature());
    features.add(new TotalHeightFeature());
    features.add(new HoleFeature());
    features.add(new UnevenFeature());

    Heuristic heuristic = new Heuristic(features);
    Scorer scorer = new Scorer(heuristic);
    scorer.play(true);
    System.out.printf("Rows cleared: %d", scorer.getLatestScore());
  }
}
