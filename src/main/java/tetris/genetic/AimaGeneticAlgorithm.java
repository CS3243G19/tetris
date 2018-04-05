package tetris.genetic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import tetris.Pair;
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
    private static final int POPULATION_SIZE = 200;
    private static final int ELITES_TO_KEEP = 5;
    private static final int NUM_ITERATIONS = 1000;
    private static final ArrayList<Feature> FEATURES = new ArrayList<>();
    public static final int NUM_THREADS = 1000;
    private ArrayList<Pair<Heuristic, Double>> population;
    private int currIteration;
    private Pair<Heuristic, Double> currentBestIndividual;
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

        this.currentBestIndividual = new Pair(new Heuristic(FEATURES), 0.);

        this.population = newRandomPopulation();

        currIteration = 0;
    }

    private ArrayList<Pair<Heuristic, Double>> newRandomPopulation() {
        ArrayList<Pair<Heuristic, Double>> newPopulation = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            newPopulation.add(new Pair(new Heuristic(FEATURES), 0.));
        }

        return newPopulation;
    }

    private void sortPopulation() {
        population.sort(new Comparator<Pair<Heuristic, Double>>() {
            @Override
            public int compare(Pair<Heuristic, Double> t1, Pair<Heuristic, Double> t2) {
                return t2.getValue().compareTo(t1.getValue());
            }
        });
    }

    private void run() {
        do {
            File heuristicsFile = new File(String.format(HEURISTICS_FILE, currIteration));
            File bestHeuristicsFile = new File(BEST_HEURISTICS_FILE);
            nextGeneration(population);
            sortPopulation(); // for easier processing
            logIteration();
            writeToFile(heuristicsFile);
            updateBest(bestHeuristicsFile);
            currIteration++;
        } while (currIteration < NUM_ITERATIONS);
    }

    private void updateBest(File file) {
        Pair<Heuristic, Double> pair = population.get(0);
        Double score = pair.getValue();
        if (score > currentBestIndividual.getValue()) {
            currentBestIndividual = population.get(0);
            writeBestHeuristic(file);
        }
    }

    private void writeBestHeuristic(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            Double[] weights = currentBestIndividual.getKey().getWeights();
            for (int i = 0; i < FEATURES.size(); i++) {
                writer.write(weights[i].toString() + ",");
            }
            writer.write(currentBestIndividual.getValue().toString());
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
                Heuristic curr = population.get(i).getKey();
                Double score = population.get(i).getValue();
                for (int j = 0; j < FEATURES.size(); j++) {
                    Double[] weight = curr.getWeights();
                    writer.write(weight[j].toString() + ",");
                }
                writer.write(score.toString());
                writer.newLine();
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextGeneration(ArrayList<Pair<Heuristic, Double>> population) {
        playGames();
        for (int i = ELITES_TO_KEEP; i < POPULATION_SIZE; i++) {
            Heuristic x = randomSelection(population);
            Heuristic y = randomSelection(population);

            Heuristic child = reproduce(x, y);

            if (random.nextDouble() < MUTATION_PROBABILITY) {
                child = mutate(child);
            }

            population.get(i).setKey(child);
        }
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

        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int j = 0; j < POPULATION_SIZE; j++) {
            Heuristic individual = population.get(j).getKey();
            HeuristicRunner runner = new HeuristicRunner(individual);
            Future<Double> score = executor.submit(runner);
            futureScores.add(score);
        }

        for (int k = 0; k < futureScores.size(); k++) {
            try {
                Double score = futureScores.get(k).get();
                this.population.get(k).setValue(score);
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

    private Heuristic randomSelection(ArrayList<Pair<Heuristic, Double>> population) {
        Heuristic selected = population.get(population.size() - 1).getKey();

        double[] fValues  = new double[population.size()];
        for (int i = 0; i < population.size(); i++) {
            fValues[i] = population.get(i).getValue();
        }

        fValues = normalize(fValues);
        double prob = random.nextDouble();
        double totalSoFar = 0.0;

        for (int i = 0; i < fValues.length; i++) {
            totalSoFar += fValues[i];
            if (prob <= totalSoFar) {
                selected = population.get(i).getKey();
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
        Pair<Heuristic, Double> bestIndividual = population.get(0);
        System.out.println("Iteration: " + currIteration);
        System.out.println("Iteration's Best Individual: " + Arrays.toString(bestIndividual.getKey().getWeights()));
        System.out.println("Score: " + bestIndividual.getValue());
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
