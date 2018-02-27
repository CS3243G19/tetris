package tetris;

import tetris.feature.*;
import tetris.heuristic.Heuristic;

import java.util.ArrayList;

public class PlayerSkeleton {
  public int pickMove(State s, int[][] legalMoves, Heuristic h) {

    double maxValue = Double.MIN_VALUE;
    int index = -1;

    for (int i = 0; i < legalMoves.length; i++) {
      State nextState = new State(s);
      nextState.makeMove(i);
      if (nextState.hasLost()) {
        continue;
      }

      double val = h.getValue(nextState);
      if (val > maxValue) {
        maxValue = val;
        index = i;
      }
    }

    return index;
  }

  public static void main(String[] args) {
    State s = new State();
    new TFrame(s);
    PlayerSkeleton p = new PlayerSkeleton();
    ArrayList<Feature> features = new ArrayList<Feature>();
    features.add(new RowsClearedFeature());
    features.add(new TotalHeightFeature());
    features.add(new HoleFeature());
    features.add(new UnevenFeature());

    Heuristic h = new Heuristic(features);
    while(!s.hasLost()) {

      s.makeMove(p.pickMove(s,s.legalMoves(), h));
      s.draw();
      s.drawNext(0,0);
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("You have completed "+s.getRowsCleared()+" rows.");
  }

}
