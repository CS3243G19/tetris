package tetris.scorer;

import tetris.State;
import tetris.heuristic.Heuristic;
import tetris.feature.*;
import tetris.player.Player;

import java.util.ArrayList;

public class Scorer {
  protected int turn;
  public ArrayList<Integer> scores;
  public Heuristic heuristic;
  public Player player;

  public Scorer(Heuristic heuristic) {
    this.heuristic = heuristic;
    this.player = new Player(heuristic);
    this.scores = new ArrayList<Integer>();
    this.turn = 0;
  }

  public void play() {
    turn++;
    State state = new State();
    while(!state.hasLost()) {
      int move = player.getMove(state);
      state.makeMove(move);
    }

    int score = state.getRowsCleared();
    System.out.printf("Turn %d: %d\n", turn, score);
    scores.add(score);
  }

  public double getAverageScore() {
    int acc = 0;
    for (Integer i : scores) acc += i;
    return acc / scores.size();
  }

  public static void main(String[] args) {
    ArrayList<Feature> features = new ArrayList<Feature>();
    features.add(new RowsClearedFeature());
    features.add(new TotalHeightFeature());
    features.add(new HoleFeature());
    features.add(new UnevenFeature());

    Heuristic heuristic = new Heuristic(features);

    Scorer scorer = new Scorer(heuristic);

    for (int i = 0; i < 100; i++) {
      scorer.play();
    }

    System.out.printf("Average Score: %f", scorer.getAverageScore());
  }
}
