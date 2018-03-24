package tetris.feature;

import tetris.NextState;
import tetris.State;

public class ColTransitionsFeature extends Feature {
    @Override
    public double getValue(NextState s) {
        int[][] field = s.getField();
        int transitions = 0;
        for (int col = 0; col < State.COLS; col++) {
            boolean previousIsHole;
            if (field[0][col] == 0) {
                previousIsHole = true;
            } else {
                previousIsHole = false;
            }
            for (int row = 1; row < State.ROWS; row++) {
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
