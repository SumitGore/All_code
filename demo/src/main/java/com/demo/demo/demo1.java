package com.demo.demo;

import java.util.Arrays;

public class demo1 {

    public static void main(String[] args) {
        // Define the dimensions of the matrix
        int rows = 3;
        int columns = 3;

        // Create a 2D matrix and initialize it with values
        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };

        // Print the original matrix
        System.out.println("Original Matrix:");
        printMatrix(matrix);

        // Specify the indices to remove
        int rowToRemove = 1;
        int colToRemove = 2;

        // Remove the element at the specified row and column
        matrix = removeElement(matrix, rowToRemove, colToRemove);

        // Print the matrix after removing the element
        System.out.println("\nMatrix after removing element at (" + rowToRemove + ", " + colToRemove + "):");
        printMatrix(matrix);

        // Iterate through the matrix and print non-removed values
        System.out.println("\nNon-Removed Values:");
        iterateNonRemovedValues(matrix);
    }

    // Helper method to remove an element at a specific row and column index
    private static int[][] removeElement(int[][] matrix, int rowIndex, int colIndex) {
        if (rowIndex >= 0 && rowIndex < matrix.length && colIndex >= 0 && colIndex < matrix[0].length) {
            matrix[rowIndex][colIndex] = 0; // Set to 0 or any other marker value
            return matrix;
        } else {
            System.out.println("Invalid row or column index");
            return matrix;
        }
    }

    // Helper method to print non-removed values in the matrix
    private static void iterateNonRemovedValues(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                if (value != 0) {
                    System.out.print(value + " ");
                }
            }
        }
    }

    // Helper method to print the matrix
    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }
}
