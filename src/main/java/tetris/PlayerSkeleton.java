package tetris;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static tetris.State.COLS;
import static tetris.State.ROWS;


public class PlayerSkeleton {
    public static final ArrayList<Heuristic> HEURISTICS = new ArrayList<>();

    public static void main(String[] args) {
        PlayerSkeleton p = new PlayerSkeleton();
        p.run();
    }
    public void run() {
        State state = new State();
//        new TFrame(state);

        public static final ArrayList<Feature> feature1 = new ArrayList<Feature>();
        // Maximize
        feature1.add(new RowsClearedFeature());
        // Minimize
        feature1.add(new RowTransitionsFeature());
        feature1.add(new ColTransitionsFeature());
        feature1.add(new HoleFeature());
        feature1.add(new MaxHoleHeightFeature());
        feature1.add(new BlocksOnHoleFeature());
        feature1.add(new WellFeature());
        feature1.add(new UnevenFeature());
        feature1.add(new TotalHeightFeature());

        Double[] weights1 = {0.1568925886140755, -0.8233209529744112, -0.44592794975510663, -0.9933542921083782, -0.0738032558547442, -0.3493473637407587, -0.6388625428086235, -1.0197401234839614, -0.011791669327957721};
        Double[] weights2 = {0.2947984770276658, -0.04852207418003762, -0.33130033882083265, -0.2273410758386305, -0.44761298613456046, 0.47536562684091355, -0.27931295838987324, -0.486666819161619, 0.009553660751433046};
        Double[] weights3 = {0.2947984770276658, -0.04852207418003762, -0.34326001649621085, -0.2273410758386305, -0.44761298613456046, 0.47536562684091355, -0.27931295838987324, -0.486666819161619, 0.009553660751433046};


        Heuristic heuristic1 = new Heuristic(feature1, weights1);
        HEURISTICS.add(heuristic1);
        Scorer scorer = new Scorer(HEURISTICS);
        scorer.play(false);

        System.out.printf("Rows cleared: %d", scorer.getLatestScore());
    }

    public static class Scorer {
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
                int currVotes = 0;
                if (votes.containsKey(move)) {
                     currVotes = votes.get(move);
                }
                if (!topRankedInMove.containsKey(move)) {
                    topRankedInMove.put(move, i);
                }
                votes.put(move, currVotes +1 );
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

    public class Heuristic {
        private final int size;
        private Double[] weights;
        private ArrayList<Feature> features;

        /**
         * Initialize new Heuristic with random weights
         * @param features
         */
        public Heuristic(ArrayList<Feature> features) {
            this.features = features;
            this.size = this.features.size();
            Random r = new Random();
            this.weights = new Double[this.size];
            for (int i = 0; i < this.size; i++) {
                this.weights[i] = r.nextDouble() - 0.5; // [-0.5, 0.5]
            }
        }

        /**
         * Initialize new Heuristic with given weights
         * @param features
         * @param heuristicArray
         */
        public Heuristic(ArrayList<Feature> features, Double[] heuristicArray) {
            this.features = features;
            this.size = this.features.size();
            this.weights = heuristicArray;
        }

        public double getValue(NextState s) {
            double sum = 0;
            for (int i = 0; i < this.size; i++) {
                sum += this.weights[i] * this.features.get(i).getValue(s);
            }

            return sum;
        }

        public Double[] getWeights() {
            return weights;
        }

        public ArrayList<Feature> getFeatures() {
            return features;
        }

    }

    public abstract class Feature {
        public Feature() {};

        public abstract double getValue(NextState s);
    }

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

    public class HoleFeature extends Feature {
        @Override
        public double getValue(NextState s) {
            int[][] field = s.getField();
            int[] top = s.getTop();

            int numHoles = 0;
            for (int j = 0;  j < COLS;  j++) {
                if (top[j] != 0) {
                    for (int i = top[j] - 1;  i >= 0;  i--) {
                        if (field[i][j] == 0) {
                            numHoles++;
                        }
                    }
                }
            }
            return (double) numHoles * 10;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HoleFeature;
        }
    }


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

            return maxHeight;
        }
    }

    public class RowsClearedFeature extends Feature {
        @Override
        public double getValue(NextState s) {
            return s.getRowsCleared();
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof RowsClearedFeature;
        }
    }

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

    public class TotalHeightFeature extends Feature {

        @Override
        public double getValue(NextState s) {
            int highest[] = s.getTop();
            double sum = 0;
            for (int i : highest) {
                sum += i;
            }
            return sum;
        };

        @Override
        public boolean equals(Object o) {
            return o instanceof TotalHeightFeature;
        }
    }

    public class UnevenFeature extends Feature {
        @Override
        public double getValue(NextState s) {
            int highest[] = s.getTop();
            double score = 0;
            for (int i = 1; i < State.COLS; i++) {
                score += Math.abs(highest[i] - highest[i-1]);
            }
            return score;
        };

        @Override
        public boolean equals(Object o) {
            return o instanceof UnevenFeature;
        }
    }

    public class WellFeature extends Feature {
        @Override
        public double getValue(NextState s) {
            int[][] field = s.getField();
            int[] top = s.getTop();
            int wellSum = 0;
            for (int j = 0;  j < State.COLS;  j++) {
                for (int i = State.ROWS -1;  i >= 0;  i--) {
                    if (field[i][j] == 0) {
                        if (j == 0 || field[i][j - 1] != 0) {
                            if (j == State.COLS - 1 || field[i][j + 1] != 0) {
                                int wellHeight = i - top[j] + 1;
                                wellSum += wellHeight * (wellHeight + 1) / 2;
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
            return wellSum;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof WellFeature;
        }
    }

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

}
