package tetris.feature;

import tetris.NextState;

public class HoleSquaredFeature extends Feature {

    @Override
    public double getValue(NextState s) {
        return Math.pow(new HoleFeature().getValue(s), 2);
    }
}
