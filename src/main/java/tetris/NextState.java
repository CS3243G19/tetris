package tetris;

public class NextState {
    private int[][] field = new int[State.ROWS][State.COLS];
    private int[] top = new int[State.COLS];
    private static int rowsCleared = 0;

    private int[][][] pBottom = State.getpBottom();
    private int[][] pHeight = State.getpHeight();
    private int[][] pWidth = State.getpWidth();
    private int[][][] pTop = State.getpTop();

    private int nextPiece;
    private int turn;
    private int cleared;

    boolean lost = false;

    public int[][] getField() {
        return field;
    }

    public int[] getTop() {
        return top;
    }

    public int getRowsCleared() {
        return cleared;
    }

    public boolean hasLost() {
        return lost;
    }

    public void setNextPiece(int nextPiece) {
      this.nextPiece = nextPiece;
    }

    public int getNextPiece() {
      return nextPiece;
    }

    public int getTurnNumber() {
      return turn;
    }

    //constructor
    public NextState(State s) {
        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                field[i][j] = s.getField()[i][j];
            }
        }

        for (int i = 0; i < State.COLS; i++) {
            top[i] = s.getTop()[i];
        }
        nextPiece = s.getNextPiece();
        turn = s.getTurnNumber();
        cleared = s.getRowsCleared();
    }

    public NextState(NextState s) {
        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                field[i][j] = s.getField()[i][j];
            }
        }

        for (int i = 0; i < State.COLS; i++) {
            top[i] = s.getTop()[i];
        }
        nextPiece = s.getNextPiece();
        turn = s.getTurnNumber();
        cleared = s.getRowsCleared();
    }

    //make a move based on the move index - its order in the legalMoves list
    public void makeMove(int move, int[][] legalMoves) {
        makeMove(legalMoves[move]);
    }

    //make a move based on an array of orient and slot
    public void makeMove(int[] move) {
        makeMove(move[State.ORIENT],move[State.SLOT]);
    }

    //returns false if you lose - true otherwise
    public void makeMove(int orient, int slot) {
        //height if the first column makes contact
        int height = top[slot] - pBottom[nextPiece][orient][0];
        //for each column beyond the first in the piece
        for (int c = 1; c < pWidth[nextPiece][orient]; c++) {
            if (slot + c >= 10) {
                System.out.println("SLOT and C are awry");
            }
            height = Math.max(height, top[slot + c] - pBottom[nextPiece][orient][c]);
        }

        //check if game ended
        if (height + pHeight[nextPiece][orient] >= State.ROWS) {
            lost = true;
            return;
        }


        //for each column in the piece - fill in the appropriate blocks
        for (int i = 0; i < pWidth[nextPiece][orient]; i++) {

            //from bottom to top of brick
            for (int h = height + pBottom[nextPiece][orient][i]; h < height + pTop[nextPiece][orient][i]; h++) {
                field[h][i + slot] = turn;
            }
        }

        //adjust top
        for (int c = 0; c < pWidth[nextPiece][orient]; c++) {
            top[slot + c] = height + pTop[nextPiece][orient][c];
        }

        int rowsCleared = 0;

        //check for full rows - starting at the top
        for (int r = height + pHeight[nextPiece][orient] - 1; r >= height; r--) {
            //check all columns in the row
            boolean full = true;
            for (int c = 0; c < State.COLS; c++) {
                if (field[r][c] == 0) {
                    full = false;
                    break;
                }
            }
            //if the row was full - remove it and slide above stuff down
            if (full) {
                rowsCleared++;
                cleared++;
                //for each column
                for (int c = 0; c < State.COLS; c++) {

                    //slide down all bricks
                    for (int i = r; i < top[c]; i++) {
                        field[i][c] = field[i + 1][c];
                    }
                    //lower the top
                    top[c]--;
                    while (top[c] >= 1 && field[top[c] - 1][c] == 0) top[c]--;
                }
            }
        }
    }
}
