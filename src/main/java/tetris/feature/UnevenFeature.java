package tetris.feature;

import tetris.State;

public class UnevenFeature extends Feature {
  @Override
  public double getValue(State s) {
  	int highest[] = s.getTop();
  	double score = 0;
  	for (int i = 1; i < State.COLS; i++) {
  	  score += Math.abs(highest[i] - highest[i-1]);
  	}
  	return score;
  };
}
