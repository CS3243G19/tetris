package tetris.feature;

import tetris.NextState;
import tetris.State;

public class MaxWellFeature extends Feature {
    @Override
    public double getValue(NextState s) {
        int max = -1;
        int[][] field = s.getField();
        int[] top = s.getTop();
        int curr = 0;
        for (int col = 0; col < State.COLS; col++) {
            if (top[col] == 1) {
                continue;
            }
            for (int row = 0; row < State.ROWS; row++) {
                if (field[row][col] == 0) {
                    if (col == 0) {
                        if (field[row][col+1] == 1) {
                            curr++;
                        }
                    } else if (col == State.COLS - 1) {
                        if (field[row][col-1] == 1) {
                            curr++;
                        }
                    } else if (field[row][col - 1] == 1 && field[row][col + 1] == 1) {
                        curr++;
                    }
                } else {
                    break;
                }
            }
            if (curr > max) {
                max = curr;
            }
        }
        return max;
    }

  @Override
  public boolean equals(Object o) {
    return o instanceof MaxWellFeature;
  }
}
