package tetris.genetic;

import tetris.heuristic.Heuristic;
import tetris.scorer.Scorer;

import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

public class GeneticAlgorithm {

  public static final Integer FEATURES = 4;
  public static final Integer HEURISTICS = 100;
  public static final Double MUTATION_RATE = 0.0015;
  public static final Integer RETENTION = 75;
  public static final Integer SELECTION = 5;
  public static final Double DEFAULT_SCORE = 0.0;

  private static WeightScorePair[] heuristicArray;

  public static void main(String[] args) {
    File newFile = new File("heuristics.txt");
    if (!newFile.exists()) {
      try {
        initialiseHeuristics();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    heuristicArray = new WeightScorePair[HEURISTICS];
    try {
      readHeuristics();
      generateNewHeuristics();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void readHeuristics() throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader("heuristics.txt"));

    String[] weightArray;
    for (int i = 0; i < heuristicArray.length; i++) {
      weightArray = bufferedReader.readLine().split(",");
      Double[] weight = new Double[FEATURES];
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
      heuristicArray[i] = result;
    }
    bufferedReader.close();
  }

  private static void initialiseHeuristics() throws IOException {
    Random r = new Random();
    heuristicArray = new WeightScorePair[HEURISTICS];
    for (int i = 0; i < heuristicArray.length; i++) {
      Double score = 0.0;
      Double[] weight = new Double[FEATURES];
      for (int j = 0; j < FEATURES; j++) {
          weight[j] = r.nextDouble() * 2 - 1.0;
      }
      WeightScorePair curr = new WeightScorePair(weight,score);
      heuristicArray[i] = curr;
    }
    saveHeuristics();
  }

  private static void saveHeuristics() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter("heuristics.txt"));
    for (int i = 0; i < heuristicArray.length; i++) {
      WeightScorePair curr = heuristicArray[i];
      for (int j = 0; j < FEATURES; j++) {
        Double[] weight = curr.getWeight();
        writer.write(weight[j].toString() + ",");
      }
      writer.write(curr.getScore().toString());
      writer.newLine();
    }
    writer.close();
  }

  private static void generateNewHeuristics() throws Exception {
    Random r = new Random();

    // We keep our fittest individual heuristicArray[0] in our population
    // We perform crossing over for a fixed number of individuals, as defined in RETENTION
    for (int i = 1; i < RETENTION+1; i++) {
      Integer winner1 = naturalSelection();
      Integer winner2 = naturalSelection();
      WeightScorePair heuristic1 = heuristicArray[winner1];
      WeightScorePair heuristic2 = heuristicArray[winner2];

      Double[] resultWeight = crossover(heuristic1, heuristic2);
      WeightScorePair result = new WeightScorePair(resultWeight,DEFAULT_SCORE);
      heuristicArray[i] = result;
    }

    // We then perform mutation
    mutate();

    // We also include some genetic drift to introduce new genes into the population
    WeightScorePair[] newHeuristicArray = new WeightScorePair[HEURISTICS];

    for (int i = RETENTION + 1; i < HEURISTICS; i++) {
      Double[] weight = new Double[FEATURES];
      for (int j = 0; j < FEATURES; j++) {
        weight[j] = r.nextDouble() * 2 - 1.0;
      }
      heuristicArray[i] = new WeightScorePair(weight, DEFAULT_SCORE);
    }

    // Finally, we test the fitness of these new heuristics
    for (int i = 0; i < HEURISTICS; i++) {
      WeightScorePair curr = heuristicArray[i];
      Double[] weight = curr.getWeight();
      double[] heurArr = Stream.of(weight).mapToDouble(Double::doubleValue).toArray();
      Heuristic currHeuristic = new Heuristic(heurArr);
      Scorer scorer = new Scorer(currHeuristic);
      for (int j = 0; j < 100; j++) {
        scorer.play();
      }
      Double averageScore = scorer.getAverageScore();

      WeightScorePair scored = new WeightScorePair(weight,averageScore);
      heuristicArray[i] = scored;
    }
    Arrays.sort(heuristicArray);
    saveHeuristics();

  }

  private static void mutate() {
    Random r = new Random();
    for (int i = 1; i < RETENTION+1; i ++) {
      WeightScorePair curr = heuristicArray[i];
      Double[] currWeight = curr.getWeight();
      for (int j = 0; j < FEATURES; j++) {
        Double mutChance = r.nextDouble();
        if (mutChance <= MUTATION_RATE) {
          currWeight[j] = r.nextDouble() * 2 - 1.0;
        }
      }
      WeightScorePair result = new WeightScorePair(currWeight, DEFAULT_SCORE);
      heuristicArray[i] = result;
    }
  }

  private static Double[] crossover(WeightScorePair heuristic1, WeightScorePair heuristic2) {
    Double score1 = heuristic1.getScore();
    Double score2 = heuristic2.getScore();
    Double[] weight1 = heuristic1.getWeight();
    Double[] weight2 = heuristic2.getWeight();
    Double crossoverRate = score1.doubleValue() /(score1.doubleValue() + score2.doubleValue());
    Double[] resultHeuristics = new Double[FEATURES];

    for (int i = 0; i < FEATURES; i++) {
      Random r = new Random();
      Double next = r.nextDouble();
      if (next <= crossoverRate) {
        resultHeuristics[i] = weight1[i];
      } else {
        resultHeuristics[i] = weight2[i];
      }
    }
    return resultHeuristics;
  }

  private static Integer naturalSelection() {
    Integer result = 0;
    Double curr = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < SELECTION; i++) {
      Random r = new Random();
      Integer next = r.nextInt(heuristicArray.length);
      Double score = heuristicArray[next].getScore();
      if (score > curr) {
        curr = score;
        result = next;
      }
    }

    return result;
  }
}
