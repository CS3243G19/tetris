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

import java.util.Random;
import java.util.concurrent.*;

public class GeneticAlgorithm {

  private static final Integer NUM_GENERATIONS = 1000;
  private static final Integer NUM_GAMES = 100;
  private static final Integer POPULATION_SIZE = 1000;

  private static final Double MUTATION_RATE = 0.1;
  private static final Double DEFAULT_SCORE = 0.0;

  private static final Integer SURVIVORS = 1;
  private static final Integer CROSSED_OVER = ((int) Math.floor(POPULATION_SIZE / 3));

  private static final ArrayList<Feature> FEATURES = new ArrayList<>();
  private Heuristic[] heuristicArray;
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

    for (int i = 0; i < NUM_GENERATIONS; i++) {
      try {
        ga.heuristicArray = ga.readHeuristics();
        System.out.println("Iteration " + ga.currIteration);
        ga.generateNextGeneration();
        System.out.println("Best Heuristic: " + Arrays.toString(ga.heuristicArray[0].getWeights()));
        System.out.println("Score: " + ga.heuristicArray[0].getScore());
        ga.saveHeuristics();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /** We read heuristics from heuristics.txt, and parse it into a Heuristic[]
   *
   * @return
   * @throws IOException
   */
  private Heuristic[] readHeuristics() throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new FileReader("heuristics.txt"));
    Heuristic[] resultArr = new Heuristic[POPULATION_SIZE];
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

      // We create the new Heuristic and store the result
      Heuristic result = new Heuristic(FEATURES, weight, score);
      resultArr[i] = result;
    }
    bufferedReader.close();
    return resultArr;
  }

  /** We create random heuristics, if there is no existing heuristics.txt
   *
   * @throws IOException
   */
  private void initialiseHeuristics() throws IOException {
    Random r = new Random();
    heuristicArray = new Heuristic[POPULATION_SIZE];
    for (int i = 0; i < heuristicArray.length; i++) {
      Double score = 0.0;
      Double[] weight = new Double[FEATURES.size()];
      for (int j = 0; j < FEATURES.size(); j++) {
          weight[j] = r.nextDouble() * 2 - 1.0;
      }
      Heuristic curr = new Heuristic(FEATURES, weight,score);
      heuristicArray[i] = curr;
    }
    currIteration = 0;
    BufferedWriter writer = new BufferedWriter(new FileWriter("heuristics.txt"));
    writer.write("Iteration " + currIteration);
    writer.newLine();
    writeToFile(writer, heuristicArray);
    writer.close();
  }

  /** We use this method to save our new heuristics, while appending old iterations to the text file
   *
   * @throws IOException
   */
  private void saveHeuristics() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter("temp.txt"));
    writer.write("Iteration " + (currIteration + 1));
    writer.newLine();
    writeToFile(writer, heuristicArray);
    String text = new String(Files.readAllBytes(Paths.get("heuristics.txt")), StandardCharsets.UTF_8);
    writer.write(text);
    writer.close();
    Files.move(Paths.get("heuristics.txt"), Paths.get("heuristics.old"), StandardCopyOption.REPLACE_EXISTING);
    Files.move(Paths.get("temp.txt"), Paths.get("heuristics.txt"), StandardCopyOption.REPLACE_EXISTING);
  }

  private void writeToFile(BufferedWriter writer, Heuristic[] heuristicArray) throws IOException {
    for (int i = 0; i < heuristicArray.length; i++) {
      Heuristic curr = heuristicArray[i];
      for (int j = 0; j < FEATURES.size(); j++) {
        Double[] weight = curr.getWeights();
        writer.write(weight[j].toString() + ",");
      }
      writer.write(curr.getScore().toString());
      writer.newLine();
    }

  }

  /** Our primary method of obtaining our next set of heuristics
   *
   * @throws Exception
   */
  private void generateNextGeneration() throws Exception {
    Random r = new Random();
    Heuristic[] newHeuristicArray = new Heuristic[POPULATION_SIZE];

    for (int i = 0; i < SURVIVORS; i++) {
      newHeuristicArray[i] = this.heuristicArray[i];
    }

    // We keep our fittest SURVIVORS individuals  in our population
    // We perform crossing over for a fixed number of individuals, as defined in CROSSED_OVER
    for (int i = SURVIVORS; i < CROSSED_OVER + SURVIVORS; i++) {
      Integer winner1 = randomSelection();
      Integer winner2 = randomSelection();
      Heuristic heuristic1 = heuristicArray[winner1];
      Heuristic heuristic2 = heuristicArray[winner2];

      Double[] resultWeight = crossover(heuristic1, heuristic2);
      Heuristic result = new Heuristic(FEATURES, resultWeight,DEFAULT_SCORE);
      Double mutChance = r.nextDouble();
      if (mutChance <= MUTATION_RATE) {
        result = mutate(result, r);
      }
      newHeuristicArray[i] = result;
    }

    // We also include some genetic drift to introduce new genes into the population
    for (int i = CROSSED_OVER + SURVIVORS; i < POPULATION_SIZE; i++) {
      Double[] weight = new Double[FEATURES.size()];
      for (int j = 0; j < FEATURES.size(); j++) {
        weight[j] = r.nextDouble() * 2 - 1.0;
      }
      Heuristic newIndividual = new Heuristic(FEATURES, weight, DEFAULT_SCORE);
      newHeuristicArray[i] = newIndividual;
    }

    // Finally, we test the fitness of these new heuristics
    heuristicArray = newHeuristicArray;
    score();

  }

  /** We thread this for each heuristic, which will each play games as shown in HeuristicRunner
   *
   * @throws Exception
   */
  private void score() throws Exception {

    ExecutorService executor = Executors.newFixedThreadPool(100);

    for (int i = 0; i < POPULATION_SIZE; i++) {
      Heuristic curr = heuristicArray[i];
      Double[] weight = curr.getWeights();
      Heuristic currHeuristic = new Heuristic(FEATURES, weight, DEFAULT_SCORE);
      HeuristicRunner runner = new HeuristicRunner(i, currHeuristic);
      executor.execute(runner);
    }
    executor.shutdown();
    executor.awaitTermination(1000, TimeUnit.MINUTES);

    Arrays.sort(heuristicArray);
  }

  /** Randomly mutates all members in a population, based on chance
   *
   */
  private Heuristic mutate(Heuristic heuristic, Random r) {
      Double[] currWeight = heuristic.getWeights();
      Double currScore = heuristic.getScore();
      for (int j = 0; j < FEATURES.size(); j++) {
        Double mutChance = r.nextDouble();
        if (mutChance <= MUTATION_RATE) {
          currWeight[j] = currWeight[j] + (r.nextDouble() * 2 - 1.0);
        }
      }
      Heuristic result = new Heuristic(FEATURES, currWeight, currScore);
      return result;
  }

  /** We pick 2 heuristics, and do a weighted crossover based on their scores. Higher scores have a greater chance
   * of their weights being selected.
   *
   * @param heuristic1
   * @param heuristic2
   * @return
   */
  private Double[] crossover(Heuristic heuristic1, Heuristic heuristic2) {
    Double score1 = heuristic1.getScore();
    Double score2 = heuristic2.getScore();
    Double[] weight1 = heuristic1.getWeights();
    Double[] weight2 = heuristic2.getWeights();
    Double crossoverRate = score1.doubleValue() /(score1.doubleValue() + score2.doubleValue());
//    Double crossoverRate = 0.5;
    Double[] resultHeuristics = new Double[FEATURES.size()];

    for (int i = 0; i < FEATURES.size(); i++) {
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

  /** We pick the parent randomly, through a normalised random selection
   *
   * @return individual
   */
  private Integer randomSelection() {
    Integer result = 0;
    Double [] fitnessValues = new Double[POPULATION_SIZE];

    for (int i = 0; i < POPULATION_SIZE; i++) {
      fitnessValues[i] = heuristicArray[i].getScore();
    }

    fitnessValues = normalize(fitnessValues);
    Random r = new Random();
    Double prob = r.nextDouble();
    Double currTotal = 0.0;

    for (int i = 0; i < fitnessValues.length; i++) {
      currTotal += fitnessValues[i];
      if (prob <= currTotal) {
        result = i;
        break;
      }
    }
    return result;
  }


  /** Full credit to aimacode/aima-java
   *
   * @param probDist
   * @return
   */
  public Double[] normalize(Double[] probDist) {
    int len = probDist.length;
    Double total = 0.0;
    for (int i = 0; i < len; i++) {
      total = total + probDist[i];
    }

    Double[] normalized = new Double[len];
    for (int i = 0; i < normalized.length; i++) {
      normalized[i] = 0.0;
    }
    if (total != 0) {
      for (int i = 0; i < len; i++) {
        normalized[i] = probDist[i] / total;
      }
    }

    return normalized;
  }

  /** We create a runnable, which will play the games using a Scorer
   *
   */
  private class HeuristicRunner implements Runnable {
    private int id;
    private Scorer scorer;
    private Heuristic heuristic;

    public HeuristicRunner (int id, Heuristic heuristic) {
      this.id = id;
      this.scorer = new Scorer(heuristic);
      this.heuristic = heuristic;
    }

    public void run() {

      for (int j = 0; j < NUM_GAMES; j++) {
        scorer.play();
      }
      Double averageScore = scorer.getAverageScore();
//      System.out.println("Average Score is: " + averageScore);
      this.heuristic.setScore(averageScore);
      heuristicArray[id] = this.heuristic;
    }
  }

  private class GameRunner implements Callable<Integer> {

    private Scorer scorer;

    public GameRunner(Scorer scorer) {
      this.scorer = scorer;
    }

    public Integer call() {
      scorer.play();
      Integer result = scorer.getLatestScore();
      return result;
    }
  }

}
