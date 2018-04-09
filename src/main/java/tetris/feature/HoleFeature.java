package tetris.feature;

import static tetris.State.COLS;

import tetris.NextState;
import tetris.State;

public class HoleFeature extends Feature {
  @Override
  public double getValue(NextState s) {
      int[][] field = s.getField();
    int[] top = s.getTop();

            int numHoles = 0;
        for (int j = 0;  j < COLS;  j++) {
            if (top[j] != 0) {
                for (int i = top[j] - 1;  i >= 0;  i--) {
                    if (field[i][j] == 0) {
                        numHoles++;
                    }
                }
            }
        }
        return (double) numHoles;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof HoleFeature;
  }
}
