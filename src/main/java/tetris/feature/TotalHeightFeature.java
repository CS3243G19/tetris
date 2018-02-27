package tetris.feature;

import tetris.State;

public class TotalHeightFeature extends Feature {
  @Override
  public double getValue(State s) {
  	int highest[] = s.getTop();
  	double sum = 0;
  	for (int i : highest) {
  	  sum += i;
  	}
  	return sum;
  };


}
