package tetris.feature;

import tetris.NextState;
import tetris.State;

public class ColTransitionsFeature extends Feature {
    @Override
    public double getValue(NextState s) {
        int[][] field = s.getField();
        int[] top = s.getTop();
        int colTransitions = 0;
        for (int j = 0;  j < State.COLS;  j++) {
            for (int i = top[j] - 2;  i >= 0;  i--) {
                if ((field[i][j] == 0) != (field[i + 1][j] == 0)) {
                    colTransitions++;
                }
            }
            if (field[0][j] == 0 && top[j] > 0) colTransitions++;
        }
        return (double) colTransitions;
    }
}
