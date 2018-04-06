package tetris.feature;

import tetris.NextState;
import tetris.State;

public class WellFeature extends Feature {
    @Override
    public double getValue(NextState s) {
        int wells = 0;
        int[][] field = s.getField();
        int[] top = s.getTop();
        for (int col = 0; col < State.COLS; col++) {
            if (top[col] == 1) {
                continue;
            }
            for (int row = 0; row < State.ROWS; row++) {
                if (field[row][col] == 0) {
                    if (col == 0) {
                        if (field[row][col+1] == 1) {
                            wells++;
                        }
                    } else if (col == State.COLS - 1) {
                        if (field[row][col-1] == 1) {
                            wells++;
                        }
                    } else if (field[row][col - 1] == 1 && field[row][col + 1] == 1) {
                        wells++;
                    }
                }
            }
        }
        return wells;
    }

  @Override
  public boolean equals(Object o) {
    return o instanceof WellFeature;
  }
}
