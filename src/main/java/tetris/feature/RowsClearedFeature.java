package tetris.feature;

import tetris.State;


public class RowsClearedFeature extends Feature {
  @Override
  public double getValue(State s) {
    return s.getRowsCleared();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof RowsClearedFeature;
  }
}
