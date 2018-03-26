package tetris.player;

import tetris.NextState;
import tetris.State;
import tetris.heuristic.Heuristic;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Player {
  private static final int DEPTH = 1;
  public Heuristic heuristic;

  public Player(Heuristic heuristic) {
    this.heuristic = heuristic;
  }

  public int getMove(State s) {
    int[][] legalMoves = s.legalMoves();
    double maxValue = -Double.MAX_VALUE;

    int index = -1;

    ExecutorService executor = Executors.newFixedThreadPool(legalMoves.length);
    ArrayList<Future<Double>> avgValues = new ArrayList<>();

    for (int i = 0; i < legalMoves.length; i++) {
      NextState nextState = new NextState(s);
      nextState.makeMove(i, legalMoves);
      PlyRunner runner = new PlyRunner(nextState);
      Future<Double> value =  executor.submit(runner);
      avgValues.add(value);
    }
    executor.shutdown();

    for (int i = 0;i < avgValues.size(); i++) {
      try{
        Double val = avgValues.get(i).get();
        if (val > maxValue) {
          maxValue = val;
          index = i;
        }
      } catch (Exception e) {
        e.printStackTrace();
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
    for (int i=0; i<State.N_PIECES; i++) {
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
      totalValue = totalValue + maxValue/State.N_PIECES;
    }
    return totalValue;
  }

  private class PlyRunner implements Callable<Double> {
    private NextState nextState;

    public PlyRunner(NextState nextState) {
      this.nextState = nextState;
    }

    @Override
    public Double call() {
      Double averageValue = spamPly(nextState, DEPTH);
      return averageValue;
    }
  }
}
