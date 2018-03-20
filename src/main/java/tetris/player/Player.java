package tetris.player;

import tetris.NextState;
import tetris.State;
import tetris.heuristic.Heuristic;

public class Player {
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

      if (nextState.hasLost()) {
        continue;
      }

      double val = heuristic.getValue(nextState);
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
}
