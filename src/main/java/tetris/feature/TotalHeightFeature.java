package tetris.feature;

import tetris.NextState;
import tetris.State;

public class TotalHeightFeature extends Feature {

  @Override
  public double getValue(NextState s) {
  	int highest[] = s.getTop();
  	double sum = 0;
  	for (int i : highest) {
  	  sum += i;
  	}
  	return sum;
  };

  @Override
  public boolean equals(Object o) {
    return o instanceof TotalHeightFeature;
  }
}
