package tetris.swarm;

import tetris.Pair;
import tetris.feature.*;
import tetris.heuristic.Heuristic;
import tetris.scorer.Scorer;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

public class Particle {
    // Hyper-parameters:
    public static final Double INERTIA = 0.7d; //Weight decay
    public static final Double C1 = 2d; //Weight of individual best position
    public static final Double C2 = 2d; //Weight of global best position
    public static final int NUM_GAMES = 3; //Number of games to play


    private Double[] velocity;
    private Heuristic position;
    private Heuristic best;
    private Double bestScore = 0d;
    private final Random random = new Random();

    /**
     * Generates particle with random weights and velocity
     * @param features feature vector
     */
    public Particle(ArrayList<Feature> features) {
        position = new Heuristic(features);
        best = position;
        velocity = new Double[features.size()];
        // Initialize velocity to ~U(-1, 1)^n
        for(int i = 0; i < velocity.length; i++) {
            velocity[i] = random.nextDouble() * 2d - 1d;
        }
    }

    /**
     * Plays games, then returns a pair of score and position
     * @param globalBest global best heuristic
     * @return Pair<Double, Heuristic> pair of score and position
     */
    public Callable<Pair<Double, Heuristic>> play(Heuristic globalBest) {
        update(globalBest);
        return new HeuristicRunner();
    }

    /**
     * Generates new velocity based on individual and global best, then updates position with this velocity
     * @param globalBest global best heuristic
     */
    private void update(Heuristic globalBest) {
        Double r1 = random.nextDouble();
        Double r2 = random.nextDouble();
        Double[] positionWeights = position.getWeights();
        Double[] indivBestWeights = best.getWeights();
        Double[] globalBestWeights = globalBest.getWeights();
        //Update velocity
        for(int i = 0; i < velocity.length; i++) {
            velocity[i] = INERTIA * velocity[i]
                    + r1 * C1 * (indivBestWeights[i] - positionWeights[i])
                    + r2 * C2 * (globalBestWeights[i] - positionWeights[i]);
        }
        //Calculate resultant position using position and velocity
        Double[] resultant = new Double[velocity.length];
        for(int i = 0; i < velocity.length; i++) {
            resultant[i] = velocity[i] + positionWeights[i];
        }
        //Sets position to be new heuristic
        position = new Heuristic(position.getFeatures(), resultant);
    }

    private class HeuristicRunner implements Callable<Pair<Double, Heuristic>> {
        @Override
        public Pair<Double, Heuristic> call() {
            Scorer scorer = new Scorer(position);
            for(int i = 0; i < NUM_GAMES; i++) {
                scorer.play();
            }
            Double score = scorer.getAverageScore();
            if(score > bestScore) {
                best = position;
                bestScore = score;
            }
            return new Pair<>(score, position);
        }
    }
}
