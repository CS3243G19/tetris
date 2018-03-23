package tetris.feature;

import tetris.NextState;

public class MaxHeightFeature extends Feature{
    @Override
    public double getValue(NextState s) {
        int maxHeight = 0;
        int arrayOfHeights[] = s.getTop();
        for (int i = 0; i < arrayOfHeights.length; i++) {
            int curr = arrayOfHeights[i];
            if (curr > maxHeight) {
                maxHeight = curr;
            }
        }
        return (double) maxHeight;
    }
}
