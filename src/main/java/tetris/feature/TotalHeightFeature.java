package tetris.feature;

import tetris.State;

public class AggregateHeightFeature extends Feature {
  @Override
  public double getValue(State s) {
  	int highest[] = s.getTop();
  	int sum = 0;
  	for (int i : highest) {
  	  sum += i;
  	}
  	return sum;
  };


}
