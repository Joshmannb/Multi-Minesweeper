package minesweeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Actual board of the game representing the internal state.
 */
public class Board {
    final private static String boomMessage = "BOOM!";
    final private List<String[]> visualBoard;
    final private boolean[][] bombBoard;
    final private int width, height;
    
    /**
     * Construct a new Board class with size width x height and randomized bomb distribution.
     * 
     * @param width width of the Board.
     * @param height height of the Board
     */
    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.visualBoard = new ArrayList<>();
        this.bombBoard = new boolean[height][width];
        
        for (int i = 0; i < this.height; i++) {
            String[] row = new String[this.width];
            for (int j = 0; j < this.width; j++) {
                row[j] = "-";
            }
            this.visualBoard.add(row);
        }
        
        for (var row : this.bombBoard) {
            for (int i = 0; i < row.length; i++) {
                if (new Random().nextFloat() <= 0.25) {
                    row[i] = true;
                }
            }
        }
    }
    
    /**
     * Construct a Board class with size and bomb distribution specified by File object file.
     * 
     * @param file File object specify the size and bomb distribution of the board.
     * @throws FileNotFoundException if invalid file is provided.
     * @throws IOException if the format of the file is wrong.
     */
    public Board(File file) throws FileNotFoundException, IOException {
        BufferedReader input = new BufferedReader(new FileReader(file));
        String[] size = input.readLine().split(" ");
        this.height = Integer.parseInt(size[1]);
        this.width = Integer.parseInt(size[0]);
        this.visualBoard = new ArrayList<>();
        this.bombBoard = new boolean[height][width];

        for (int i = 0; i < this.height; i++) {
            String[] row = new String[this.width];
            for (int j = 0; j < this.width; j++) {
                row[j] = "-";
            }
            this.visualBoard.add(row);
        }

        for (int i = 0; i < this.height; i++) {
            String[] row = input.readLine().split(" ");
            for (int j = 0; j < this.width; j++) {
                this.bombBoard[i][j] = Integer.parseInt(row[j]) == 0 ? false : true;
            }
        }

        input.close();
    }

    /**
     * Get width of the board.
     * 
     * @return width of the board
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Get height of the board.
     * 
     * @return height of the board
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the current state of the board.
     * 
     * @return current state of the board
     */
    public synchronized List<String[]> boardMessage() {
        return this.visualBoard;
    }

    /**
     * Get the boomMessage if user encounters a bomb.
     * 
     * @return boomMessage
     */
    public synchronized String boomMessage() {
        return Board.boomMessage;
    }

    /**
     * Dig the block at location (x, y) and update the neighbour blocks. If the block and the neighbour blocks contains no bombs, then dig neighbour blocks recursively.
     * 
     * @param x xth row
     * @param y yth column
     * @return boomMessage if the block contains a bomb else boardMessage.
     */
    public synchronized boolean dig(int x, int y) {
        if (!isUntouched(x, y) || !insideBoundry(x, y)) {
            return false;
        }

        if (this.bombBoard[x][y]) {
            this.bombBoard[x][y] = false;
            updateNeighbour(x, y);
            dig(x, y);
            return true;
        }

        int count = checkNeighbour(x, y);
        this.visualBoard.get(x)[y] = (count == 0) ? " " : String.valueOf(count);

        if (count == 0) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int newX = x + i, newY = y + j;
                    if ((i == 0 && j == 0) || !insideBoundry(newX, newY) || !isUntouched(newX, newY)
                            || bombBoard[newX][newY]) {
                        continue;
                    }
                    dig(newX, newY);
                }
            }
        }

        return false;
    }

    /**
     * Flag the block at location (x, y).
     * 
     * @param x xth row
     * @param y yth column
     */
    public synchronized void flag(int x, int y) {
        if (insideBoundry(x, y) && isUntouched(x, y)) {
            this.visualBoard.get(x)[y] = "F";
        }
    }

    /**
     * Deflag the block at location (x, y).
     * 
     * @param x
     * @param y
     */
    public synchronized void deflag(int x, int y) {
        if (insideBoundry(x, y) && isFlagged(x, y)) {
            this.visualBoard.get(x)[y] = "-";
        }
    }

    /**
     * Check if the location (x, y) is within board or not.
     * 
     * @param x xth row
     * @param y yth column
     * @return true if the location is within board else false
     */
    private synchronized boolean insideBoundry(int x, int y) {
        return x >= 0 && y >= 0 && x < this.height && y < this.width;
    }

    /**
     * Check if the block at location (x, y) is untouched or not.
     * 
     * @param x xth row
     * @param y yth column
     * @return true if the block hasn't been dug or flagged else false
     */
    private synchronized boolean isUntouched(int x, int y) {
        return this.visualBoard.get(x)[y] == "-";
    }

    /**
     * Check if the block at location (x, y) is flagged or not.
     * 
     * @param x xth row
     * @param y yth column
     * @return true if the block is flagged else false
     */
    private synchronized boolean isFlagged(int x, int y) {
        return this.visualBoard.get(x)[y] == "F";
    }

    /**
     * Check if the block at location (x, y) is dug or not.
     * 
     * @param x xth row
     * @param y yth column
     * @return true if the block is dug else false
     */
    private synchronized boolean isDug(int x, int y) {
        return this.visualBoard.get(x)[y] != "-" && this.visualBoard.get(x)[y] != "F";
    }

    /**
     * Calculate how many neighbours contain bombs around location (x, y).
     * 
     * @param x xth row
     * @param y yth column
     * @return number of neighbours contain bombs, " " if there is none.
     */
    private synchronized int checkNeighbour(int x, int y) {
        int res = 0;

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!insideBoundry(x + i, y + j) || (i == 0 && j == 0)) {
                    continue;
                } else {
                    if (this.bombBoard[x + i][y + j]) {
                        res++;
                    }
                }
            }
        }

        return res;
    }

    /**
     * Update neighbour blocks if it's dug.
     * 
     * @param x xth row
     * @param y yth column
     */
    private synchronized void updateNeighbour(int x, int y) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int newX = x + i, newY = y + j;
                if (!insideBoundry(newX, newY) || !isDug(newX, newY) || (i == 0 && j == 0)) {
                    continue;
                }
                int count = Integer.parseInt(this.visualBoard.get(newX)[newY]) - 1;
                if (count == 0) {
                    this.visualBoard.get(newX)[newY] = " ";
                } else {
                    this.visualBoard.get(newX)[newY] = String.valueOf(count);
                }
            }
        }
    }
}
