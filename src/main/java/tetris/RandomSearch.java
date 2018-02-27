package tetris;

public class RandomSearch {
  public int pickMove(State s, int[][] legalMoves) {
    double rand = Math.random();
    return (int) Math.floor(rand * legalMoves.length);
  }

  public static void main(String[] args) {
    State s = new State();
    new TFrame(s);
    RandomSearch p = new RandomSearch();
    while(!s.hasLost()) {
      s.makeMove(p.pickMove(s,s.legalMoves()));
      s.draw();
      s.drawNext(0,0);
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("You have completed "+s.getRowsCleared()+" rows.");
  }
}
