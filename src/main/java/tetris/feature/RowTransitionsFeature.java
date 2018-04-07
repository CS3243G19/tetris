package tetris.feature;

import static tetris.State.COLS;
import static tetris.State.ROWS;

import tetris.NextState;
import tetris.State;

public class RowTransitionsFeature extends Feature {
    @Override
    public double getValue(NextState s) {
        int[][] field = s.getField();
        int rowTransitions = 0;
        int lastCell = 1;
        for (int i = 0;  i < ROWS;  i++) {
            for (int j = 0;  j < COLS;  j++) {
                if ((field[i][j] == 0) != (lastCell == 0)) {
                    rowTransitions++;
                }
                lastCell = field[i][j];
            }
            if (lastCell == 0) rowTransitions++;
        }
        return (double) rowTransitions;
    }
}
