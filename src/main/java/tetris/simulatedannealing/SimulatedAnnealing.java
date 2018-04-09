package tetris.simulatedannealing;

import java.util.ArrayList;
import java.util.Random;

import tetris.feature.Feature;
import tetris.feature.HoleFeature;
import tetris.feature.MaxHoleHeightFeature;
import tetris.feature.RowsClearedFeature;
import tetris.feature.TotalHeightFeature;
import tetris.feature.UnevenFeature;
import tetris.heuristic.Heuristic;
import tetris.scorer.Scorer;

public class SimulatedAnnealing {
    private static final int NUM_GAMES = 5;
    private static final int NUM_ITERATIONS = 100;
    private Scheduler scheduler;
    private double currentScore;
    private Heuristic heuristic;
    private static final ArrayList<Feature> FEATURES = new ArrayList<>();
    private int iteration;
    private Random random;

    public SimulatedAnnealing() {
        this.currentScore = 0;
        FEATURES.add(new HoleFeature());
        FEATURES.add(new MaxHoleHeightFeature());
        FEATURES.add(new RowsClearedFeature());
        FEATURES.add(new TotalHeightFeature());
        FEATURES.add(new UnevenFeature());

        this.random = new Random();

        this.iteration = 0;
        this.scheduler = new Scheduler();
        this.heuristic = new Heuristic(FEATURES);
    }

    public static void main(String[] args) {
        SimulatedAnnealing sa = new SimulatedAnnealing();
        sa.run();
    }

    private void run() {
        do {
            double temperature = scheduler.getTemp(iteration);
            if (temperature == 0.0) {
                System.out.println("Done!");
                return;
            }

            Heuristic nextHeuristic = getNextHeuristic(heuristic);
            Scorer scorer = new Scorer(nextHeuristic);
            for (int i = 0; i < NUM_GAMES; i++) {
                scorer.play();
            }
            double score = scorer.getAverageScore();

            if (accept(temperature, score, currentScore)) {
                heuristic = nextHeuristic;
                currentScore = score;
            }
            log();
            iteration++;
        } while(iteration < NUM_ITERATIONS);
    }

    private void log() {
        System.out.println("Iteration: " + iteration);
        System.out.println("Current Score: " + currentScore);
    }

    private boolean accept(double temperature, double score, double currentScore) {
        double probAccept = Math.exp(score - currentScore / temperature);
        return (score - currentScore > 0.0)
                || (new Random().nextDouble() <= probAccept);
    }

    private Heuristic getNextHeuristic(Heuristic heuristic) {
        Double[] weights = heuristic.getWeights();
        int c = random.nextInt(FEATURES.size());
        Double[] newWeights = new Double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            newWeights[i] = weights[i];
        }

        newWeights[c] = newWeights[c] + random.nextDouble() - 0.5;

        return new Heuristic(FEATURES, newWeights);
    }

    public class Scheduler {

        private final int k, limit;

        private final double lam;

        public Scheduler(int k, double lam, int limit) {
            this.k = k;
            this.lam = lam;
            this.limit = limit;
        }

        public Scheduler() {
            this.k = 20;
            this.lam = 0.045;
            this.limit = NUM_ITERATIONS;
        }

        public double getTemp(int t) {
            if (t < limit)
                return k * Math.exp((-1) * lam * t);
            else
                return 0.0;
        }
    }
}
