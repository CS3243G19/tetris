package tetris.feature;

import tetris.NextState;
import tetris.State;

public class BlocksOnHoleFeature extends Feature {

    // Blockade
    // number of blocks on top of holes
    // we want to penalize blocks from being placed on top of holes
    @Override
    public double getValue(NextState s) {
        int[][] field = s.getField();
        int highest[] = s.getTop();
        int blockade = 0;
        for (int col = 0; col < State.COLS; col++) {
            boolean seenHole = false;
            for (int row = 0; row < highest[col]; row++) {
                if (!seenHole && field[row][col] == 0) {
                    seenHole = true;
                } else if (seenHole && field[row][col] == 1) {
                    blockade++;
                }
            }
        }
        return blockade;
    }
}
