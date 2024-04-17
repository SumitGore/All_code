package test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TicTacToe extends JFrame implements ActionListener {

    private char[][] board = new char[3][3];
    private char currentPlayer = 'X';
    private boolean gameOver = false;

    private JButton[][] buttons = new JButton[3][3];

    public TicTacToe() {
        super("Tic Tac Toe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 3));

        // Initialize buttons and board
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].addActionListener(this);
                add(buttons[i][j]);
                board[i][j] = ' ';
            }
        }

        setVisible(true);
        setSize(300, 300);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            return; // Ignore clicks if game is over
        }

        JButton clickedButton = (JButton) e.getSource();
        int row = 0, col = 0;

        // Find clicked button's position on the board
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j] == clickedButton) {
                    row = i;
                    col = j;
                    break;
                }
            }
        }

        // Check if the cell is already occupied
        if (board[row][col] != ' ') {
            JOptionPane.showMessageDialog(this, "Cell already occupied!");
            return;
        }

        // Mark the cell and update board
        buttons[row][col].setText(String.valueOf(currentPlayer));
        board[row][col] = currentPlayer;

        // Check for winner
        if (checkWinner(row, col, currentPlayer)) {
            gameOver = true;
            JOptionPane.showMessageDialog(this, currentPlayer + " wins!");
        } else if (isBoardFull()) {
            gameOver = true;
            JOptionPane.showMessageDialog(this, "It's a tie!");
        }

        // Switch turns
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    private boolean checkWinner(int row, int col, char player) {
        // Check row, column, and diagonals
        return (board[row][0] == player && board[row][1] == player && board[row][2] == player) ||
               (board[0][col] == player && board[1][col] == player && board[2][col] == player) ||
               (board[0][0] == player && board[1][1] == player && board[2][2] == player) ||
               (board[0][2] == player && board[1][1] == player && board[2][0] == player);
    }

    private boolean isBoardFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {
        new TicTacToe();
    }
}

