package tetris;

import tetris.feature.*;
import tetris.heuristic.Heuristic;

import tetris.scorer.Scorer;

import java.io.File;
import java.util.ArrayList;

public class PlayerSkeleton {
  public static void main(String[] args) {
    State state = new State();
    new TFrame(state);
    PlayerSkeleton p = new PlayerSkeleton();
    ArrayList<Feature> features = new ArrayList<Feature>();
    // Minimize
    features.add(new RowsClearedFeature());
    // Maximize
      features.add(new HoleFeature());
    features.add(new RowTransitionsFeature());
    features.add(new ColTransitionsFeature());
    features.add(new WellFeature());

    File file = new File("./experiments/best_heuristic.txt");

    Heuristic heuristic = new Heuristic(file);
    Scorer scorer = new Scorer(heuristic);
    scorer.play(false);
    System.out.printf("Rows cleared: %d", scorer.getLatestScore());
  }
}
