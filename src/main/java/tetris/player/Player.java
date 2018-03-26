package tetris.player;

import tetris.NextState;
import tetris.State;
import tetris.heuristic.Heuristic;

public class Player {
  boolean DEBUG = true;
  public Heuristic heuristic;

  public Player(Heuristic heuristic) {
    this.heuristic = heuristic;
  }

  public int getMove(State s) {
    int[][] legalMoves = s.legalMoves();
    double maxValue = -Double.MAX_VALUE;

    int index = -1;

    for (int i = 0; i < legalMoves.length; i++) {
      NextState nextState = new NextState(s);

      nextState.makeMove(i, legalMoves);
      double averageCloud = spamPly(nextState, 1);

      if (nextState.hasLost()) {
        continue;
      }

      double heuristicValue = heuristic.getValue(nextState);
      //pd("avg Cloud value: " + averageCloud);
      //pd("heuristic value: " + heuristicValue);

      double val = heuristicValue + averageCloud;
      if (val > maxValue) {
        maxValue = val;
        index = i;
      }
    }

    // Every move leads to a loss.
    if (index == -1) {
      index = 0; // make a random move
    }

    return index;
  }

  public double spamPly(NextState s, int depth) {
    double totalValue = 0;
    // give it all 7 possible pieces
    for (int i=0; i<7; i++) {
      NextState nextPiece = new NextState(s);
      nextPiece.setNextPiece(i);
      int[][] legalMoves = new State().getLegalMoves(i);

      double maxValue = -Double.MAX_VALUE;
      for (int j=0; j<legalMoves.length; j++) {
        NextState nextState = new NextState(nextPiece);
        nextState.makeMove(j, legalMoves);
        if (nextState.hasLost()) {
          continue;
        }
        double val = heuristic.getValue(nextState);
        if (val > maxValue) {
          maxValue = val;
        }
      }
      //pd("maxValue:   " + maxValue);
      totalValue += maxValue/7.0;
      //pd("totalValue: " + totalValue);
    }
    return totalValue;
  }

  public void pd(Object a) {
    if(DEBUG) {
      System.out.println(a);
    }
  }
}
