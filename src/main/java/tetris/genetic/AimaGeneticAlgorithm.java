package tetris.genetic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tetris.feature.BlocksOnHoleFeature;
import tetris.feature.ColTransitionsFeature;
import tetris.feature.Feature;
import tetris.feature.HoleFeature;
import tetris.feature.MaxHeightFeature;
import tetris.feature.MaxHoleHeightFeature;
import tetris.feature.RowTransitionsFeature;
import tetris.feature.RowsClearedFeature;
import tetris.feature.TotalHeightFeature;
import tetris.feature.UnevenFeature;
import tetris.feature.WellFeature;
import tetris.heuristic.Heuristic;
import tetris.scorer.Scorer;

public class AimaGeneticAlgorithm {
    private static final int NUM_GAMES = 3;
    private static final double MUTATION_PROBABILITY = 0.05;
    private static final String EXPERIMENTS_DIR = "experiments/";
    private static final String HEURISTICS_FILE = EXPERIMENTS_DIR + "heuristics_%s.txt";
    private static final String BEST_HEURISTICS_FILE = EXPERIMENTS_DIR + "/best_heuristic.txt";
    private static final int POPULATION_SIZE = 1000;
    private static final int NUM_ITERATIONS = 1000;
    private static final ArrayList<Feature> FEATURES = new ArrayList<>();
    private ArrayList<Heuristic> population;
    private int currIteration;
    private ArrayList<Double> scores;
    private Heuristic currentBestHeuristic;
    private Double currentBestScore;
    private final Random random = new Random();

    public AimaGeneticAlgorithm() {
        // Make experiments directory
        new File(EXPERIMENTS_DIR).mkdirs();

        // Minimize
        FEATURES.add(new RowsClearedFeature());
        // Maximize
        FEATURES.add(new HoleFeature());
        FEATURES.add(new MaxHoleHeightFeature());
        FEATURES.add(new TotalHeightFeature());
        FEATURES.add(new UnevenFeature());
        FEATURES.add(new MaxHeightFeature());
        FEATURES.add(new BlocksOnHoleFeature());
        FEATURES.add(new WellFeature());
        FEATURES.add(new RowTransitionsFeature());
        FEATURES.add(new ColTransitionsFeature());
        FEATURES.add(new MaxWellFeature());

        this.currentBestHeuristic = new Heuristic(FEATURES);
        this.currentBestScore = 0.;

        this.population = newRandomPopulation();

        scores = new ArrayList<>(Collections.nCopies(POPULATION_SIZE, 0.0));
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
            File heuristicsFile = new File(String.format(HEURISTICS_FILE, currIteration));
            File bestHeuristicsFile = new File(BEST_HEURISTICS_FILE);
            population = nextGeneration(population);
            logIteration();
            writeToFile(heuristicsFile);
            updateBest(bestHeuristicsFile);
            currIteration++;
        } while (currIteration < NUM_ITERATIONS);
    }

    private void updateBest(File file) {
        int best = bestIndividual();
        double score = scores.get(best);
        if (score > currentBestScore) {
            currentBestHeuristic = population.get(best);
            currentBestScore = score;
            writeBestHeuristic(file);
        }
    }

    private void writeBestHeuristic(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            Double[] weights = currentBestHeuristic.getWeights();
            for (int i = 0; i < FEATURES.size(); i++) {
                writer.write(weights[i].toString() + ",");
            }
            writer.write(currentBestScore.toString());
            writer.newLine();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < population.size(); i++) {
                Heuristic curr = population.get(i);
                for (int j = 0; j < FEATURES.size(); j++) {
                    Double[] weight = curr.getWeights();
                    writer.write(weight[j].toString() + ",");
                }
                writer.write(scores.get(i).toString());
                writer.newLine();
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                this.scores.set(k, score);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
    }

    private Heuristic reproduce(Heuristic x, Heuristic y) {
        Double[] weight1 = x.getWeights();
        Double[] weight2 = y.getWeights();
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
            fValues[i] = scores.get(i);
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
        System.out.println("Iteration's Best Individual: " + Arrays.toString(population.get(best).getWeights()));
        System.out.println("Score: " + scores.get(best));
    }

    private int bestIndividual() {
        int index = -1;
        double max = - Double.MAX_VALUE;
        for (int i = 0; i < scores.size(); i++) {
            Double score = scores.get(i);
            if (score > max) {
                max = score;
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
