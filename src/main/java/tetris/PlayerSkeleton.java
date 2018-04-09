package tetris;

import tetris.feature.*;
import tetris.heuristic.Heuristic;

import tetris.scorer.Scorer;

import java.io.File;
import java.util.ArrayList;

public class PlayerSkeleton {
    public static final ArrayList<Feature> FEATURES = new ArrayList<Feature>();
    public static void main(String[] args) {
        State state = new State();
        new TFrame(state);
        PlayerSkeleton p = new PlayerSkeleton();

        // Maximize
        FEATURES.add(new RowsClearedFeature());
        // Minimize
        FEATURES.add(new RowTransitionsFeature());
        FEATURES.add(new ColTransitionsFeature());
        FEATURES.add(new HoleFeature());
        FEATURES.add(new WellFeature());

        Double[] weights = new Double[]{
                0.14859893753929043,
                -0.3988580287056608,
                -0.05147732402369354,
                -0.30161953479781256,
                -0.2543786543434735
        };

        Heuristic heuristic = new Heuristic(FEATURES, weights);
        Scorer scorer = new Scorer(heuristic);
        scorer.play(false);

        System.out.printf("Rows cleared: %d", scorer.getLatestScore());
    }
}
