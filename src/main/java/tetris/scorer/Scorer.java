package tetris.scorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tetris.NextState;
import tetris.State;
import tetris.TFrame;
import tetris.heuristic.Heuristic;

public class Scorer {
    private static final int SLEEPTIME = 300;
    protected int game;
    public ArrayList<Integer> scores;
    public ArrayList<Heuristic> heuristics;

    public Scorer(ArrayList<Heuristic> heuristics) {
        this.heuristics = heuristics;
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
            int move = getMove(state, heuristics);
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

    public int getMove(State s, Heuristic heuristic) {
        int[][] legalMoves = s.legalMoves();
        double maxValue = -Double.MAX_VALUE;

        int index = -1;

        for (int i = 0; i < legalMoves.length; i++) {
            NextState nextState = new NextState(s);

            nextState.makeMove(i, legalMoves);

            if (nextState.hasLost()) {
                continue;
            }

            double val = heuristic.getValue(nextState);
            if (val > maxValue) {
                maxValue = val;
                index = i;
            }
        }


        // Every move leads to a loss.
        if (index == -1) {
            index = 0; // make a random move
        }

        return index;
    }


    public int getMove(State s, ArrayList<Heuristic> heuristics) {
        HashMap<Integer, Integer> votes = new HashMap<>();
        HashMap<Integer, Integer> topRankedInMove = new HashMap<>();
        for (int i = 0; i < heuristics.size(); i++) {
            int move = getMove(s, heuristics.get(i));
            int currVotes = votes.get(move);
            int topRank = topRankedInMove.get(move);
            topRank = Math.min(i, topRank);
            votes.put(move, currVotes +1 );
            topRankedInMove.put(move, topRank);
        }

        int maxMove = -1;
        int maxVote = -1;
        int voteTopRank = -1;

        for (Map.Entry<Integer, Integer> entry : votes.entrySet()) {
            int currMove = entry.getKey();
            int currVote = entry.getValue();
            int currRank = topRankedInMove.get(currMove);
            if (currVote > maxVote) {
                maxVote = currVote;
                maxMove = currMove;
                voteTopRank = currRank;
            } else if (currVote == maxVote && currRank < voteTopRank) {
                   voteTopRank = currRank;
                   maxMove = currMove;
            }
        }

        return maxMove;
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
