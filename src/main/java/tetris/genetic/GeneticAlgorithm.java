package tetris.genetic;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class GeneticAlgorithm {
  public static void main(String[] args) {
    File newFile = new File("heuristics.txt");
    if (!newFile.exists()) {
      try {
        BufferedWriter writer = new BufferedWriter(new FileWriter("heuristics.txt"));
        Random r = new Random();
        Double[][] heuristicArray = new Double[100][5];
        for (int i = 0; i < heuristicArray.length; i++) {
          for (int j = 0; j < heuristicArray[0].length; j++) {
            if (j==4) {
              heuristicArray[i][j] = 0.0;
            } else {
              heuristicArray[i][j] = r.nextDouble() * 2 - 1.0;
            }
          }
        }
        for (int i = 0; i < heuristicArray.length; i++) {
          for (int j = 0; j < heuristicArray[0].length; j++) {
            writer.write(heuristicArray[i][j].toString() + ",");
          }
          writer.newLine();
        }
        writer.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    Double[][] heuristicArray = new Double[100][4];
    Double[] heuristicScore = new Double[100];
    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader("heuristics.txt"));
      String[] weightArray = new String[5];
      for (int i = 0; i < heuristicArray.length; i++) {
        weightArray = bufferedReader.readLine().split(",");
        for (int j = 0; j < weightArray.length; j++) {
          if (j==weightArray.length-1) {
            heuristicScore[i] = Double.parseDouble(weightArray[j]);
          } else {
            heuristicArray[i][j] = Double.parseDouble(weightArray[j]);
          }
        }
      }

      generateNewHeuristics(heuristicArray);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void generateNewHeuristics(Double[][] heuristicArray) {
    
  }
}
