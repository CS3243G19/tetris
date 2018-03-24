package tetris.feature;

import tetris.NextState;
import tetris.State;

public class RowTransitionsFeature extends Feature {
    @Override
    public double getValue(NextState s) {
        int[][] field = s.getField();
        int transitions = 0;
        for (int row = 0; row < State.ROWS; row++) {
            boolean previousIsHole;
            if (field[row][0] == 0) {
                previousIsHole = true;
            } else {
                previousIsHole = false;
            }
            for (int col = 1; col < State.COLS; col++) {
                if (field[row][col] == 0) {
                    if (!previousIsHole) {
                        transitions++;
                    }
                    previousIsHole = true;
                } else {
                    if (previousIsHole) {
                        transitions++;
                    }
                    previousIsHole = false;
                }
            }
        }
        return (double) transitions;
    }
}
