package minesweeper.server;

import java.io.*;
import java.net.Socket;
import java.util.List;

import minesweeper.Board;

/**
 * Handler to read and handle user input and produce corresponding output to the user.
 * 
 * @author Chen Jia-Hui
 */
public class MinesweeperHandler implements Runnable {
    private static int currentPlayers = 0;
    private Socket clientSocket;
    private Board board;
    private boolean debug;
    private String helpMessage = "\"look\": display the current board state." + "%n".formatted()
                                + "\"dig x y\": dig block at yth row and xth column if the block haven't been dug or flagged. The game ends if the block contains a mine, else displays the current board state." + "%n".formatted()
                                + "\"flag x y\": flag block at yth row and xth column if the block haven't been dug." + "%n".formatted()
                                + "\"deflag x y\": unflag blcok at yth row and xth column if it's flagged." + "%n".formatted()
                                + "\"help\": display user instructions." + "%n".formatted()
                                + "\"bye\": quit game and termiante connection.";

    /**
     * Construct a new MinesweeperHandler instance.
     * 
     * @param clientSocket the socket to communicate with the specific user.
     * @param board the board class used the represent the actual game.
     * @param debug indicates if the server in the debug state. The game won't terminate if the debug flag is set to true.
     */
    public MinesweeperHandler(Socket clientSocket, Board board, boolean debug) {
        this.clientSocket = clientSocket;
        this.board = board;
        this.debug = debug;
        MinesweeperHandler.currentPlayers += 1;
    }

    /**
     * Driver code when a new thread is initialized. Required by Runnable interface.
     */
    @Override
    public void run() {
        try {
            handleConnection(this.clientSocket);
        } catch (IOException ioe) {
            ioe.printStackTrace(); // but don't terminate serve()
        } finally {
            try {
                this.clientSocket.close();
                MinesweeperHandler.currentPlayers -= 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket socket where the client is connected
     * @throws IOException if the connection encounters an error or terminates
     *                     unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        try {
            out.println(
                    "Welcome to Minesweeper. Board: %d columns by %d rows. Players: %d including you. Type 'help' for help."
                            .formatted(board.getWidth(), board.getHeight(), MinesweeperHandler.currentPlayers));
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                if (output != "bye") {
                    out.println(output);
                } else {
                    out.println(output);
                    if (!debug) {
                        break;
                    }
                }
            }
        } finally {
            out.close();
            in.close();
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an
     * output message.
     * 
     * @param input message from client
     * @return message to client, or null if none
     */
    private String handleRequest(String input) {
        String regex = "(look)|(help)|(bye)|"
                + "(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|(deflag -?\\d+ -?\\d+)";
        if (!input.matches(regex)) {
            return this.helpMessage;
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            return handleBoardMessage();
        } else if (tokens[0].equals("help")) {
            return this.helpMessage;
        } else if (tokens[0].equals("bye")) {
            return "bye";
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                synchronized (board) {
                    boolean bomb = board.dig(y, x);
                    if (bomb) {
                        return board.boomMessage();
                    } else {
                        return handleBoardMessage();
                    }
                }

            } else if (tokens[0].equals("flag")) {
                synchronized (board) {
                    board.flag(y, x);
                    return handleBoardMessage();
                }
            } else if (tokens[0].equals("deflag")) {
                synchronized (board) {
                    board.deflag(y, x);
                    return handleBoardMessage();
                }
            }
        }
        throw new UnsupportedOperationException();
    }

    /**
     * Process board message and make it ready to be sent to the socket.
     * 
     * @return processed board message.
     */
    private String handleBoardMessage() {
        synchronized (board) {
            List<String[]> output = this.board.boardMessage();
            StringBuffer res = new StringBuffer();
            for (int i = 0; i < output.size() - 1; i++) {
                res.append(String.join(" ", output.get(i)) + "%n".formatted());
            }
            res.append(String.join(" ", output.get(output.size() - 1)));
            return res.toString();
        }
    }
}