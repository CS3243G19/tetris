package tetris.feature;

import tetris.State;

public abstract class Feature {
  public Feature() {};

  public abstract double getValue(State s);
}
