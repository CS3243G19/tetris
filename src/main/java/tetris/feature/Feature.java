package tetris.feature;

import tetris.NextState;
import tetris.State;

public abstract class Feature {
  public Feature() {};

  public abstract double getValue(NextState s);
}
