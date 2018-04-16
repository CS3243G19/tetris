package tetris.swarm;

import tetris.Pair;
import tetris.feature.*;
import tetris.heuristic.Heuristic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParticleSwarm {
    //Hyper parameters
    public static final int NUM_ITERATIONS = 100;
    public static final int NUM_PARTICLES = 50;

    private Heuristic globalBest;
    private Double globalBestScore = 0d;
    private ArrayList<Feature> features = new ArrayList<>();
    private ArrayList<Particle> particles = new ArrayList<>();
    private int iterations = 0;

    public ParticleSwarm() {
        //Features
        features.add(new RowsClearedFeature());
        features.add(new RowTransitionsFeature());
        features.add(new ColTransitionsFeature());
        features.add(new HoleFeature());
        features.add(new MaxHoleHeightFeature());
        features.add(new BlocksOnHoleFeature());
        features.add(new WellFeature());
        features.add(new UnevenFeature());
        features.add(new TotalHeightFeature());

        globalBest = new Heuristic(features);

        for(int i = 0; i < NUM_PARTICLES; i++)
            particles.add(new Particle(features));
    }

    private void log() {
        System.out.println("Current iteration: "+iterations);
        System.out.println("Best score: "+globalBestScore);
        System.out.println("Best weights: "+Arrays.toString(globalBest.getWeights()));
        // Write heuristics to file
        String filename = "heuristics.txt";
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true));
            bw.write("\nCurrent iteration: "+iterations);
            bw.write("Best score: "+globalBestScore);
            bw.write("Best weights: "+Arrays.toString(globalBest.getWeights()));
            bw.write("-------------------------------");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Runs one iteration
     */
    private void runIteration() {
        ArrayList<Future<Pair<Double, Heuristic>>> futureScores = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(100);

        // Run particle simulations concurrently
        for(int i = 0; i < NUM_PARTICLES; i++) {
            Particle particle = particles.get(i);
            Future<Pair<Double, Heuristic>> futureScore = executor.submit(particle.play(globalBest));
            futureScores.add(futureScore);
        }

        // Get scores and update global bests
        for(int i = 0; i < NUM_PARTICLES; i++) {
            try {
                Future<Pair<Double, Heuristic>> future = futureScores.get(i);
                Pair<Double, Heuristic> result = future.get();
                if(result.getKey() > globalBestScore) {
                    globalBestScore = result.getKey();
                    globalBest = result.getValue();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        log();
    }

    /**
     * Runs n iterations
     * @return pair of best score and best heuristic
     */
    public Pair<Double, Heuristic> run() {
        while(iterations++ < NUM_ITERATIONS)
            runIteration();
        return new Pair<Double, Heuristic>(globalBestScore, globalBest);
    }

    public static void main(String[] args) {
        ParticleSwarm ps = new ParticleSwarm();
        Pair<Double, Heuristic> best = ps.run();
    }
}
