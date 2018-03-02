package tetris;

import tetris.feature.*;
import tetris.heuristic.Heuristic;

import tetris.player.Player;

import java.util.ArrayList;

public class PlayerSkeleton {
  public static void main(String[] args) {
    State state = new State();
    new TFrame(state);
    PlayerSkeleton p = new PlayerSkeleton();
    ArrayList<Feature> features = new ArrayList<Feature>();
    features.add(new RowsClearedFeature());
    features.add(new TotalHeightFeature());
    features.add(new HoleFeature());
    features.add(new UnevenFeature());

    Heuristic heuristic = new Heuristic(features);
    Player player = new Player(heuristic);

    while(!state.hasLost()) {
      int move = player.getMove(state);
      state.makeMove(move);
      state.draw();
      state.drawNext(0,0);
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    System.out.println("You have completed "+state.getRowsCleared()+" rows.");
  }
}
