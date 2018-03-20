package tetris.feature;

import tetris.NextState;
import tetris.State;


public class RowsClearedFeature extends Feature {
  @Override
  public double getValue(NextState s) {
    return s.getRowsCleared();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof RowsClearedFeature;
  }
}
