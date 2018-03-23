package tetris.genetic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tetris.feature.Feature;
import tetris.feature.HoleFeature;
import tetris.feature.RowsClearedFeature;
import tetris.feature.TotalHeightFeature;
import tetris.feature.UnevenFeature;
import tetris.heuristic.Heuristic;
import tetris.scorer.Scorer;

public class AimaGeneticAlgorithm {
    private static final double MUTATION_PROBABILITY = 0.1;
    private final int POPULATION_SIZE = 1000;
    private final int NUM_ITERATIONS = 100;
    private final ArrayList<Feature> FEATURES = new ArrayList<>();
    private ArrayList<Heuristic> population;
    private int currIteration;
    private double[] scores;
    private final Random random = new Random();

    public AimaGeneticAlgorithm() {
        this.population = newRandomPopulation();
        FEATURES.add(new HoleFeature());
        FEATURES.add(new RowsClearedFeature());
        FEATURES.add(new TotalHeightFeature());
        FEATURES.add(new UnevenFeature());
        currIteration = 0;
    }

    private ArrayList<Heuristic> newRandomPopulation() {
        ArrayList<Heuristic> newPopulation = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            newPopulation.add(new Heuristic(FEATURES));
        }

        return newPopulation;
    }

    private void run() {
        do {
            population = nextGeneration(population);
            logIteration();
            currIteration++;
        } while (currIteration < NUM_ITERATIONS);
    }

    private ArrayList<Heuristic> nextGeneration(ArrayList<Heuristic> population) {
        playGames();
        ArrayList<Heuristic> newPopulation = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            Heuristic x = randomSelection(population);
            Heuristic y = randomSelection(population);

            Heuristic child = reproduce(x, y);

            if (random.nextDouble() < MUTATION_PROBABILITY) {
                child = mutate(child);
            }

            newPopulation.add(child);
        }
        return newPopulation;
    }

    private Heuristic mutate(Heuristic child) {
        Double[] weights = child.getWeights();
        int c = random.nextInt(FEATURES.size());
        Double[] newWeights = new Double[weights.length];
        for (int i = 0; i < weights.length; i++) {
            newWeights[i] = weights[i];
        }

        newWeights[c] = newWeights[c] + random.nextDouble() - 0.5;

        return new Heuristic(FEATURES, newWeights, 0.0);
    }

    private void playGames() {
        List<Future<Double>> futureScores = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            ExecutorService executor = Executors.newFixedThreadPool(100);

            for (int j = 0; j < POPULATION_SIZE; j++) {
                Heuristic individual = population.get(j);
                HeuristicRunner runner = new HeuristicRunner(individual);
                Future<Double> score = executor.submit(runner);
                futureScores.add(score);
            }

            for (int k = 0; k < futureScores.size(); k++) {
                try {
                    Double score = futureScores.get(k).get();
                    scores[k] = score;
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            executor.shutdown();
        }
    }

    private Heuristic reproduce(Heuristic x, Heuristic y) {
//        Double score1 = x.getScore();
//        Double score2 = y.getScore();
        Double[] weight1 = x.getWeights();
        Double[] weight2 = y.getWeights();
//        Double crossoverRate = score1.doubleValue() /(score1.doubleValue() + score2.doubleValue());
    Double crossoverRate = 0.5;
        Double[] resultHeuristics = new Double[FEATURES.size()];

        for (int i = 0; i < FEATURES.size(); i++) {
            Double next = random.nextDouble();
            if (next <= crossoverRate) {
                resultHeuristics[i] = weight1[i];
            } else {
                resultHeuristics[i] = weight2[i];
            }
        }
        return new Heuristic(FEATURES, resultHeuristics, 0.0);
    }

    private Heuristic randomSelection(ArrayList<Heuristic> population) {
        Heuristic selected = population.get(population.size() - 1);

        double[] fValues  = new double[population.size()];
        for (int i = 0; i < population.size(); i++) {
            fValues[i] = scores[i];
        }

        fValues = normalize(fValues);
        double prob = random.nextDouble();
        double totalSoFar = 0.0;

        for (int i = 0; i < fValues.length; i++) {
            totalSoFar += fValues[i];
            if (prob <= totalSoFar) {
                selected = population.get(i);
                break;
            }
        }

        return selected;
    }

    private double[] normalize(double[] values) {
        int len = values.length;
        double total = 0.0;
        for (double d : values) {
            total = total + d;
        }

        double[] normalized = new double[len];
        if (total != 0) {
            for (int i = 0; i < len; i++) {
                normalized[i] = values[i] / total;
            }
        }

        return normalized;
    }

    private void logIteration() {
        int best = bestIndividual();
        System.out.println("Iteration: " + currIteration);
        System.out.println("Best Individual: " + population.get(best).getWeights());
        System.out.println("Score: " + scores[best]);
    }

    private int bestIndividual() {
        int index = -1;
        double max = - Double.MAX_VALUE;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > max) {
                max = scores[i];
                index = i;
            }
        }
        return index;
    }

    public static void main(String[] args) {
        AimaGeneticAlgorithm ga = new AimaGeneticAlgorithm();
        ga.run();
    }

    private class HeuristicRunner implements Callable<Double> {
        private static final int NUM_GAMES = 100;
        private Scorer scorer;

        public HeuristicRunner (Heuristic heuristic) {
            this.scorer = new Scorer(heuristic);
        }

        @Override
        public Double call() {
            for (int j = 0; j < NUM_GAMES; j++) {
                scorer.play();
            }

            Double averageScore = scorer.getAverageScore();

            return averageScore;
        }
    }

}
