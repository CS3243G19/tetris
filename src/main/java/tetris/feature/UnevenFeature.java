package tetris.feature;

import tetris.NextState;
import tetris.State;

public class UnevenFeature extends Feature {
  @Override
  public double getValue(NextState s) {
  	int highest[] = s.getTop();
  	double score = 0;
  	for (int i = 1; i < State.COLS; i++) {
  	  score += Math.abs(highest[i] - highest[i-1]);
  	}
  	return score;
  };

  @Override
  public boolean equals(Object o) {
    return o instanceof UnevenFeature;
  }
}
