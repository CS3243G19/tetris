package tetris.genetic;

import tetris.heuristic.Heuristic;
import tetris.scorer.Scorer;

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
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

public class GeneticAlgorithm {

  public static final Integer FEATURES = 4;
  public static final Integer HEURISTICS = 100;
  public static final Double MUTATION_RATE = 0.25;
  public static final Integer RETENTION = 75;
  public static final Integer SELECTION = 10;
  public static final Double DEFAULT_SCORE = 0.0;
  public static final Integer SURVIVAL = 15;

  private static WeightScorePair[] heuristicArray;
  private static Integer currIteration;

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

    for (int i = 0; i < 100; i++) {
      try {
        heuristicArray = readHeuristics();
        generateNewHeuristics();
        saveHeuristics();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static WeightScorePair[] readHeuristics() throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader("heuristics.txt"));
    WeightScorePair[] resultArr = new WeightScorePair[HEURISTICS];
    String[] iteration = bufferedReader.readLine().split(" ");
    currIteration = Integer.parseInt(iteration[1]);

    String[] weightArray;
    for (int i = 0; i < HEURISTICS; i++) {
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
      resultArr[i] = result;
    }
    bufferedReader.close();
    return resultArr;
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
    currIteration = 0;
    BufferedWriter writer = new BufferedWriter(new FileWriter("heuristics.txt"));
    writer.write("Iteration " + currIteration);
    writer.newLine();
    writeToFile(writer, heuristicArray);
    writer.close();
  }

  private static void saveHeuristics() throws IOException {
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

  private static void writeToFile(BufferedWriter writer, WeightScorePair[] heuristicArray) throws IOException {
    for (int i = 0; i < heuristicArray.length; i++) {
      WeightScorePair curr = heuristicArray[i];
      for (int j = 0; j < FEATURES; j++) {
        Double[] weight = curr.getWeight();
        writer.write(weight[j].toString() + ",");
      }
      writer.write(curr.getScore().toString());
      writer.newLine();
    }

  }

  private static void generateNewHeuristics() throws Exception {
    Random r = new Random();
    WeightScorePair[] newHeuristicArray = new WeightScorePair[HEURISTICS];

    for (int i = 0; i < SURVIVAL; i++) {
      newHeuristicArray[i] = heuristicArray[i];
    }

    // We keep our fittest individual heuristicArray[0] in our population
    // We perform crossing over for a fixed number of individuals, as defined in RETENTION
    for (int i = SURVIVAL; i < RETENTION + SURVIVAL; i++) {
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
    for (int i = RETENTION + SURVIVAL ; i < HEURISTICS; i++) {
      Double[] weight = new Double[FEATURES];
      for (int j = 0; j < FEATURES; j++) {
        weight[j] = r.nextDouble() * 2 - 1.0;
      }
      newHeuristicArray[i] = new WeightScorePair(weight, DEFAULT_SCORE);
    }

    // Finally, we test the fitness of these new heuristics
    heuristicArray = newHeuristicArray;
    score();

  }

  private static void score() {
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

      WeightScorePair scored = new WeightScorePair(weight, averageScore);
      heuristicArray[i] = scored;
    }
    Arrays.sort(heuristicArray);
  }

  private static void mutate() {
    Random r = new Random();
    for (int i = 1; i < RETENTION+1; i ++) {
      WeightScorePair curr = heuristicArray[i];
      Double[] currWeight = curr.getWeight();
      for (int j = 0; j < FEATURES; j++) {
        Double mutChance = r.nextDouble();
        if (mutChance <= MUTATION_RATE) {
          currWeight[j] = currWeight[j] + (r.nextDouble() * 2 - 1.0) / (currIteration + 1);
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
