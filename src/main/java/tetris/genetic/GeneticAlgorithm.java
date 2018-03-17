package tetris.genetic;

import tetris.heuristic.Heuristic;
import tetris.scorer.Scorer;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Stream;

public class GeneticAlgorithm {

  public static final Integer FEATURES = 4;
  public static final Integer HEURISTICS = 100;
  public static final Double MUTATION_RATE = 0.0015;
  public static final Integer RETENTION = 75;
  public static final Integer DRIFT = HEURISTICS - RETENTION - 1;
  public static final Integer SELECTION = 5;

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
    } catch (IOException e) {
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
      for (int j = 0; j < FEATURES; j++) {
        if (j==4) {
          heuristicArray[i][j] = 0.0;
        } else {
          heuristicArray[i][j] = r.nextDouble() * 2 - 1.0;
        }
      }
    }
    saveHeuristics(heuristicArray);
  }

  private static void saveHeuristics(Double[][] heuristicArray) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter("heuristics.txt"));
    for (int i = 0; i < heuristicArray.length; i++) {
      for (int j = 0; j < heuristicArray[0].length; j++) {
        writer.write(heuristicArray[i][j].toString() + ",");
      }
      writer.newLine();
    }
    writer.close();
  }

  //@TODO: Add mutations, and sort results of new run before saving to text file
  private static void generateNewHeuristics(Double[] heuristicScore, Double[][] heuristicArray) {
    Random r = new Random();

    // We keep our fittest individual heuristicArray[0] in our population
    // We perform crossing over for a fixed number of individuals, as defined in RETENTION
    for (int i = 1; i < RETENTION+1; i++) {
      Integer winner1 = naturalSelection(heuristicScore);
      Integer winner2 = naturalSelection(heuristicScore);
      Double[] heuristic1 = heuristicArray[winner1];
      Double[] heuristic2 = heuristicArray[winner2];
      heuristicArray[i] = crossover(heuristic1, heuristic2, winner1, winner2);
    }

    // We then perform mutation

    // We also include some genetic drift to introduce new genes into the population
    Double[][] newHeuristicArray = new Double[HEURISTICS][FEATURES];
    Double[] newHeuristicScore = new Double[HEURISTICS];

    for (int i = RETENTION + 1; i < HEURISTICS; i++) {
      for (int j = 0; j < FEATURES; j++) {
        heuristicArray[i][j] = r.nextDouble() * 2 - 1.0;
      }
    }

    // Finally, we test the fitness of these new heuristics
    for (int i = 0; i < HEURISTICS; i++) {
      double[] heurArr = Stream.of(heuristicArray[i]).mapToDouble(Double::doubleValue).toArray();
      Heuristic currHeuristic = new Heuristic(heurArr);
      Scorer scorer = new Scorer(currHeuristic);
      for (int j = 0; j < 100; j++) {
        scorer.play();
      }
      Double averageScore = scorer.getAverageScore();

    }

  }

  private static Double[] crossover(Double[] heuristic1, Double[] heuristic2, Integer score1, Integer score2) {
    Double crossoverRate = score1.doubleValue() /(score1.doubleValue() + score2.doubleValue());
    Double[] resultHeuristics = new Double[FEATURES];

    for (int i = 0; i < FEATURES; i++) {
      Random r = new Random();
      Double next = r.nextDouble();
      if (next <= crossoverRate) {
        resultHeuristics[i] = heuristic1[i];
      } else {
        resultHeuristics[i] = heuristic2[i];
      }
    }
    return resultHeuristics;
  }

  private static Integer naturalSelection(Double[] heuristicScore) {
    Integer result = 0;
    Double curr = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < SELECTION; i++) {
      Random r = new Random();
      Integer next = r.nextInt(heuristicScore.length);
      Double score = heuristicScore[next];
      if (score > curr) {
        curr = score;
        result = next;
      } else {
        continue;
      }
    }

    return result;
  }
}
