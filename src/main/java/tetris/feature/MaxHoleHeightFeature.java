package tetris.feature;

import tetris.NextState;
import tetris.State;

public class MaxHoleHeightFeature extends Feature {
    @Override
    public double getValue(NextState s) {
        int maxHeight = -1;
        int[][] field = s.getField();
        int highest[] = s.getTop();
        for (int col = 0; col < State.COLS; col++) {
            int hole = -1;
            for (int row = 0; row < highest[col]; row++) {
                if (field[row][col] == 0) {
                    hole = row;
                }
            }
            maxHeight = Math.max(maxHeight, hole);
        }

        return Math.pow(maxHeight, 2);
    }
}
