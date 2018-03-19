package tetris.genetic;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import tetris.feature.Feature;
import tetris.feature.HoleFeature;
import tetris.feature.RowsClearedFeature;
import tetris.feature.TotalHeightFeature;
import tetris.feature.UnevenFeature;
import tetris.heuristic.Heuristic;
import tetris.scorer.Scorer;

public class GeneticAlgorithm {

  private static final Integer NUM_GENERATIONS = 100;
  private static final Integer NUM_GAMES = 100;

  private static final Double MUTATION_RATE = 0.1;
  private static final Integer SELECTION = 10;
  private static final Double DEFAULT_SCORE = 0.0;

  private static final Integer SURVIVORS = 15;
  private static final Integer CROSSED_OVER = 75;

  private static final ArrayList<Feature> FEATURES = new ArrayList<>();
  private static final Integer POPULATION_SIZE = 100;

  private WeightScorePair[] heuristicArray;
  private Integer currIteration;

  public static void main(String[] args) {
    FEATURES.add(new HoleFeature());
    FEATURES.add(new RowsClearedFeature());
    FEATURES.add(new TotalHeightFeature());
    FEATURES.add(new UnevenFeature());

    File newFile = new File("heuristics.txt");
    GeneticAlgorithm ga = new GeneticAlgorithm();
    if (!newFile.exists()) {
      try {
        ga.initialiseHeuristics();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    ga.heuristicArray = new WeightScorePair[POPULATION_SIZE];

    for (int i = 0; i < NUM_GENERATIONS; i++) {
      try {
        ga.heuristicArray = ga.readHeuristics();
        ga.generateNewHeuristics();
        ga.saveHeuristics();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private WeightScorePair[] readHeuristics() throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader("heuristics.txt"));
    WeightScorePair[] resultArr = new WeightScorePair[POPULATION_SIZE];
    String[] iteration = bufferedReader.readLine().split(" ");
    this.currIteration = Integer.parseInt(iteration[1]);

    String[] weightArray;
    for (int i = 0; i < POPULATION_SIZE; i++) {
      weightArray = bufferedReader.readLine().split(",");
      Double[] weight = new Double[FEATURES.size()];
      Double score = 0.0;
      for (int j = 0; j < weightArray.length; j++) {
        if (j==weightArray.length-1) {
          score = Double.parseDouble(weightArray[j]);
        } else {
          weight[j] = Double.parseDouble(weightArray[j]);
        }
      }

      // We create the new WeightScorePair and store the result
      WeightScorePair result = new WeightScorePair(weight,score);
      resultArr[i] = result;
    }
    bufferedReader.close();
    return resultArr;
  }

  private void initialiseHeuristics() throws IOException {
    SecureRandom r = new SecureRandom();
    heuristicArray = new WeightScorePair[POPULATION_SIZE];
    for (int i = 0; i < heuristicArray.length; i++) {
      Double score = 0.0;
      Double[] weight = new Double[FEATURES.size()];
      for (int j = 0; j < FEATURES.size(); j++) {
          weight[j] = r.nextDouble() * 2 - 1.0;
      }
      WeightScorePair curr = new WeightScorePair(weight,score);
      heuristicArray[i] = curr;
    }
    currIteration = 0;
    BufferedWriter writer = new BufferedWriter(new FileWriter("heuristics.txt"));
    writer.write("Iteration " + currIteration);
    writer.newLine();
    writeToFile(writer, heuristicArray);
    writer.close();
  }

  private void saveHeuristics() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter("temp.txt"));
    BufferedReader reader = new BufferedReader(new FileReader("heuristics.txt"));
    writer.write("Iteration " + (currIteration + 1));
    writer.newLine();
    writeToFile(writer, heuristicArray);
    String text = new String(Files.readAllBytes(Paths.get("heuristics.txt")), StandardCharsets.UTF_8);
    writer.write(text);
    writer.close();
    Files.move(Paths.get("heuristics.txt"), Paths.get("heuristics.old"), StandardCopyOption.REPLACE_EXISTING);
    Files.move(Paths.get("temp.txt"), Paths.get("heuristics.txt"), StandardCopyOption.REPLACE_EXISTING);
  }

  private void writeToFile(BufferedWriter writer, WeightScorePair[] heuristicArray) throws IOException {
    for (int i = 0; i < heuristicArray.length; i++) {
      WeightScorePair curr = heuristicArray[i];
      for (int j = 0; j < FEATURES.size(); j++) {
        Double[] weight = curr.getWeight();
        writer.write(weight[j].toString() + ",");
      }
      writer.write(curr.getScore().toString());
      writer.newLine();
    }

  }

  private void generateNewHeuristics() throws Exception {
    SecureRandom r = new SecureRandom();
    WeightScorePair[] newHeuristicArray = new WeightScorePair[POPULATION_SIZE];

    for (int i = 0; i < SURVIVORS; i++) {
      newHeuristicArray[i] = this.heuristicArray[i];
    }

    // We keep our fittest SURVIVORS individuals  in our population
    // We perform crossing over for a fixed number of individuals, as defined in CROSSED_OVER
    for (int i = SURVIVORS; i < CROSSED_OVER + SURVIVORS; i++) {
      Integer winner1 = naturalSelection();
      Integer winner2 = naturalSelection();
      WeightScorePair heuristic1 = heuristicArray[winner1];
      WeightScorePair heuristic2 = heuristicArray[winner2];

      Double[] resultWeight = crossover(heuristic1, heuristic2);
      WeightScorePair result = new WeightScorePair(resultWeight,DEFAULT_SCORE);
      newHeuristicArray[i] = result;
    }

    // We then perform mutation
    mutate();

    // We also include some genetic drift to introduce new genes into the population
    for (int i = CROSSED_OVER + SURVIVORS; i < POPULATION_SIZE; i++) {
      Double[] weight = new Double[FEATURES.size()];
      for (int j = 0; j < FEATURES.size(); j++) {
        weight[j] = r.nextDouble() * 2 - 1.0;
      }
      newHeuristicArray[i] = new WeightScorePair(weight, DEFAULT_SCORE);
    }

    // Finally, we test the fitness of these new heuristics
    heuristicArray = newHeuristicArray;
    score();

  }

  private void score() throws Exception {

    ExecutorService executor = Executors.newFixedThreadPool(POPULATION_SIZE);

    for (int i = 0; i < POPULATION_SIZE; i++) {
      HeuristicRunner runner = new HeuristicRunner();
      runner.setId(i);
      executor.execute(runner);
    }
    executor.shutdown();
    executor.awaitTermination(1000, TimeUnit.MINUTES);

//    for (int i = 0; i < POPULATION_SIZE; i++) {
//      WeightScorePair curr = heuristicArray[i];
//      Double[] weight = curr.getWeight();
//      double[] heurArr = Stream.of(weight).mapToDouble(Double::doubleValue).toArray();
//      Heuristic currHeuristic = new Heuristic(FEATURES, heurArr);
//      Scorer scorer = new Scorer(currHeuristic);
//      for (int j = 0; j < NUM_GAMES; j++) {
//        scorer.play();
//      }
//      Double averageScore = scorer.getAverageScore();
//      WeightScorePair scored = new WeightScorePair(weight, averageScore);
//      heuristicArray[i] = scored;
//    }

    Arrays.sort(heuristicArray);
  }

  private void mutate() {
    SecureRandom r = new SecureRandom();
    for (int i = 1; i < CROSSED_OVER +1; i ++) {
      WeightScorePair curr = heuristicArray[i];
      Double[] currWeight = curr.getWeight();
      for (int j = 0; j < FEATURES.size(); j++) {
        Double mutChance = r.nextDouble();
        if (mutChance <= MUTATION_RATE) {
          currWeight[j] = currWeight[j] + (r.nextDouble() * 2 - 1.0);
        }
      }
      WeightScorePair result = new WeightScorePair(currWeight, DEFAULT_SCORE);
      heuristicArray[i] = result;
    }
  }

  private Double[] crossover(WeightScorePair heuristic1, WeightScorePair heuristic2) {
    Double score1 = heuristic1.getScore();
    Double score2 = heuristic2.getScore();
    Double[] weight1 = heuristic1.getWeight();
    Double[] weight2 = heuristic2.getWeight();
    Double crossoverRate = score1.doubleValue() /(score1.doubleValue() + score2.doubleValue());
    Double[] resultHeuristics = new Double[FEATURES.size()];

    for (int i = 0; i < FEATURES.size(); i++) {
      SecureRandom r = new SecureRandom();
      Double next = r.nextDouble();
      if (next <= crossoverRate) {
        resultHeuristics[i] = weight1[i];
      } else {
        resultHeuristics[i] = weight2[i];
      }
    }
    return resultHeuristics;
  }

  private Integer naturalSelection() {
    Integer result = 0;
    Double curr = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < SELECTION; i++) {
      SecureRandom r = new SecureRandom();
      Integer next = r.nextInt(POPULATION_SIZE);
      Double score = heuristicArray[next].getScore();
      if (score > curr) {
        curr = score;
        result = next;
      }
    }

    return result;
  }

  private class HeuristicRunner implements Runnable {
    private int id;

    public void setId (int id) {
      this.id = id;
    }

    public void run() {
      WeightScorePair curr = heuristicArray[id];
      Double[] weight = curr.getWeight();
      Heuristic currHeuristic = new Heuristic(FEATURES, weight);
      Scorer scorer = new Scorer(currHeuristic);
      for (int j = 0; j < NUM_GAMES; j++) {
        scorer.play();
      }
      Double averageScore = scorer.getAverageScore();
      System.out.println(averageScore);
      WeightScorePair scored = new WeightScorePair(weight, averageScore);
      heuristicArray[id] = scored;
    }
  }
}
