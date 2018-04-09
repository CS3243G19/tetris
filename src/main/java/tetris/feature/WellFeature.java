package tetris.feature;

import tetris.NextState;
import tetris.State;

public class WellFeature extends Feature {
    @Override
    public double getValue(NextState s) {
        int[][] field = s.getField();
        int[] top = s.getTop();
        int wellSum = 0;
        for (int j = 0;  j < State.COLS;  j++) {
            for (int i = State.ROWS -1;  i >= 0;  i--) {
                if (field[i][j] == 0) {
                    if (j == 0 || field[i][j - 1] != 0) {
                        if (j == State.COLS - 1 || field[i][j + 1] != 0) {
                            int wellHeight = i - top[j] + 1;
                            wellSum += wellHeight * (wellHeight + 1) / 2;
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return wellSum;
    }

  @Override
  public boolean equals(Object o) {
    return o instanceof WellFeature;
  }
}
