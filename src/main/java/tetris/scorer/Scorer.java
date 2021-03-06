package tetris.scorer;

import java.util.ArrayList;

import tetris.State;
import tetris.TFrame;
import tetris.heuristic.Heuristic;
import tetris.player.Player;

public class Scorer {
    private static final int SLEEPTIME = 300;
    protected int game;
    public ArrayList<Integer> scores;
    public Player player;

    public Scorer(Heuristic heuristic) {
        this.player = new Player(heuristic);
        this.scores = new ArrayList<Integer>();
        this.game = 0;
    }

    public void play() {
        play(false);
    }

    public void play(boolean graphics) {
        game++;
        State state = new State();
        if (graphics) {
            new TFrame(state);
        }
        while(!state.hasLost()) {
            int move = player.getMove(state);
            state.makeMove(move);

            if (graphics) {
                state.draw();
                state.drawNext(0,0);
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        int score = state.getRowsCleared();
        scores.add(score);
    }

    public int getLatestScore() {
        return scores.get(scores.size() - 1);
    }

    public double getAverageScore() {
        int acc = 0;
        for (Integer i : scores) acc += i;
        return (double) acc / scores.size();
    }

}
