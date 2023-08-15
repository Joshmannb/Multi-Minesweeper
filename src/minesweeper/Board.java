/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.util.Arrays;

/**
 * TODO: Specification
 */
public class Board {

    // TODO: Abstraction function, rep invariant, rep exposure, thread safety

    // TODO: Specify, test, and implement in problem 2

    final private char[][] visualBoard;
    final private boolean[][] bombBoard;
    final private int width, height;
    static final private char UNTOUCHED = '-';
    static final private char FLAGGED = 'F';
    static final private char NOBOMBAROUND = ' ';

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.visualBoard = new char[height - 1][width - 1];
        this.bombBoard = new boolean[height - 1][width - 1];

        for (var row : this.visualBoard) {
            Arrays.fill(row, '-');
        }

        // TODO: bombs in this.bombBoard hasn't been decied.
    }

    public synchronized char[][] look() {
        return this.visualBoard;
    }

    public synchronized boolean dig(int x, int y) {
        int[] newCoordinates = transform(x, y);
        x = newCoordinates[0];
        y = newCoordinates[1];
        boolean res = false;
        
        if (!isUntouched(x, y)) { return false; }

        if (this.bombBoard[x][y]) {
            this.bombBoard[x][y] = false;
            updateNeighbour(x, y);
            res = true;
        }

        int count = checkNeighbour(x, y);
        this.visualBoard[x][y] = (count == 0) ? UNTOUCHED : Character.forDigit(count, 10);
        
        if (count == 0) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    int newX = x + i, newY = y + j;
                    if ((i == 0 && j == 0) || !checkBoundry(newX, newY)) { continue; }
                    if (checkNeighbour(newX, newY) == 0) { dig(newX, newY); }
                }
            }
        }

        return res;
    }

    public synchronized boolean flag(int x, int y) {
        int[] newCoordinates = transform(x, y);
        x = newCoordinates[0];
        y = newCoordinates[1];

        if (checkBoundry(x, y) && isUntouched(x, y)) {
            this.visualBoard[x][y] = FLAGGED;
            return true;
        }

        return false;
    }

    public synchronized boolean deflag(int x, int y) {
        int[] newCoordinates = transform(x, y);
        x = newCoordinates[0];
        y = newCoordinates[1];

        if (checkBoundry(x, y) && isFlagged(x, y)) {
            this.visualBoard[x][y] = UNTOUCHED;
            return true;
        }

        return false;
    }

    private synchronized int[] transform(int x, int y) {
        int[] res = { y, x };

        return res;
    }

    private boolean checkBoundry(int x, int y) {
        return x >= 0 && y >= 0 && x < this.height && y < this.width;
    }

    private synchronized boolean isUntouched(int x, int y) {
        return this.visualBoard[x][y] == UNTOUCHED;
    }

    private synchronized boolean isFlagged(int x, int y) {
        return this.visualBoard[x][y] == FLAGGED;
    }

    private synchronized boolean isDug(int x, int y) {
        return this.visualBoard[x][y] != '-' && this.visualBoard[x][y] != 'F';
    }

    private synchronized int checkNeighbour(int x, int y) {
        int res = 0;

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (!checkBoundry(x, y) && i == 0 && j == 0) {
                    continue;
                } else {
                    int newX = x + i;
                    int newY = y + j;
                    if (this.bombBoard[newX][newY]) {
                        res++;
                    }
                }
            }
        }

        return res;
    }

    private synchronized void updateNeighbour(int x, int y) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                int newX = x + i, newY = y + j;
                if (!checkBoundry(newX, newY) || !isDug(newX, newY) || (i == 0 && j == 0)) { continue; }
                int count = (int)this.visualBoard[newX][newY] - 1;
                if (count == 0) {
                    this.visualBoard[newX][newY] = NOBOMBAROUND;
                } else {
                    this.visualBoard[newX][newY] = Character.forDigit(count, 10);
                }
            }
        }
    }

    public static void main(String[] args) {
        Board test = new Board(3, 5);
        System.out.println(test.visualBoard.length);
        System.out.println(test.visualBoard[0].length);
    }
}
