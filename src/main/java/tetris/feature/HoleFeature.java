package tetris.feature;

import tetris.State;

public class HoleFeature extends Feature {
  @Override
  public double getValue(State s) {
    int highest[] = s.getTop();
    int holes = 0;
    int[][] field = s.getField();
    for (int col = 0; col < State.COLS; col++) {
      for (int row = 0; row < highest[col]; row++) {
        if (field[row][col] == 0) {
          holes++;
        }
      }
    }
    return holes;
  }
}
