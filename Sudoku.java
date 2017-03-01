/////////////////////////////////////////////////////////////////////////////////
// CS 430 - Artificial Intelligence
// Project 4 - Sudoku Solver w/ Variable Ordering and Forward Checking
// File: Sudoku.java
//
// Group Member Names: Peter Cusack
// Due Date:
// 
//
// Description: A Backtracking program in Java to solve the Sudoku problem.
// Code derived from a C++ implementation at:
// http://www.geeksforgeeks.org/backtracking-set-7-suduku/
/////////////////////////////////////////////////////////////////////////////////

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class Sudoku
{
	// Constants
	final static int UNASSIGNED = 0; //UNASSIGNED is used for empty cells in sudoku grid	
	final static int N = 9;//N is used for size of Sudoku grid. Size will be NxN
	static int numBacktracks = 0;

	/////////////////////////////////////////////////////////////////////
	// Main function used to test solver.
	public static void main(String[] args) throws FileNotFoundException
	{
		// Reads in from TestCase.txt (sample sudoku puzzle).
		// 0 means unassigned cells - You can search the internet for more test cases.
		Scanner userInput = new Scanner(System.in);

		System.out.println("Please choose test Case by entering a number 1 -5");
		Scanner fileScan= new Scanner(new File("Case" + userInput.nextInt() + ".txt"));

		// Reads case into grid 2D int array
		int grid[][] = new int[9][9];
		for (int r = 0; r < 9; r++)
		{
			String row = fileScan.nextLine();
			String [] cols = row.split(",");
			for (int c = 0; c < cols.length; c++)
				grid[r][c] = Integer.parseInt(cols[c].trim());
		}
		
		// Prints out the unsolved sudoku puzzle (as is)
		System.out.println("Unsolved sudoku puzzle:");
		printGrid(grid);
		
		// Setup timer - Obtain the time before solving
		long stopTime = 0L;
		long startTime = System.currentTimeMillis();
		
		System.out.println("Enter a number 1-5 choosing from the fallowing options");
		System.out.println(" 1.) Default Static Ordering \n 2.) Your Original Static Ordering \n 3.) Random ordering \n 4.) Minimum Remaining Values Ordering \n 5.) Maximum Remaning Values Ordering");
		int orderingFunc = userInput.nextInt();
		
		System.out.println("Enter a number 1-3 choosing from the fallowing options \n 1.) None\n 2.) Forward checking\n 3.) Arc Consistency");
		boolean forwardCheck = false;
		boolean arc = false;
		switch(userInput.nextInt()){
			case 1: break;
			case 2: forwardCheck = true;
					break;
			case 3: arc = true;
					initlizeNodeDomains(grid);
					break;
			default: System.out.println("Sorry not a valid input");
					break;
		}
		// Attempts to solve and prints results
		if (SolveSudoku(grid, orderingFunc, forwardCheck, arc) == true)
		{
			// Get stop time once the algorithm has completed solving the puzzle
			stopTime = System.currentTimeMillis();
			System.out.println("Algorithmic runtime: " + (stopTime - startTime) + "ms");
			System.out.println("Number of backtracks: " + numBacktracks);
			
			// Sanity check to make sure the computed solution really IS solved
			if (!isSolved(grid))
			{
				System.err.println("An error has been detected in the solution.");
				System.exit(0);
			}
			System.out.println("\n\nSolved sudoku puzzle:");
			printGrid(grid);
		}
		else
			System.out.println("No solution exists");
	}

	/////////////////////////////////////////////////////////////////////
	// Write code here which returns true if the sudoku puzzle was solved
	// correctly, and false otherwise. In short, it should check that each
	// row, column, and 3x3 square of 9 cells maintain the ALLDIFF constraint.
	private static boolean isSolved(int[][] grid)
	{
		boolean safe = true;
		for (int row = 0; row < N; row++) {
			for (int cell = 0; cell < N; cell++ ) {
				if (isSafe(grid, row, cell, grid[row][cell])) {
					safe = false;
					return safe;
				}
			}
		}
		return safe;
	}

	/////////////////////////////////////////////////////////////////////
	// Takes a partially filled-in grid and attempts to assign values to
	// all unassigned locations in such a way to meet the requirements
	// for Sudoku solution (non-duplication across rows, columns, and boxes)
	/////////////////////////////////////////////////////////////////////
	private static final int [] DOMAIN = {1,3,4,5,6,7,8,9};
	// private static ArrayList<ArrayList<int []>> nodeDomains = new ArrayList<ArrayList<int []>>();
	private static int [][][] nodeDomains = new int[N][N][DOMAIN.length];

	static boolean SolveSudoku(int grid[][], int orderingFunc, boolean forwordCheckOption, boolean arc)
	{
		// Select next unassigned variable
		SudokuCoord variable;
		
		switch (orderingFunc) {
			case 1: variable = OrderingOpt1(grid);
					break;
			case 2: variable = OrderingOpt2(grid);
					break;
			case 3: variable = OrderingOpt3(grid);
					break;
			case 4: variable = OrderingOpt4(grid);
					break;
			case 5: variable = OrderingOpt5(grid);
					break;
			default: variable = OrderingOpt1(grid);
					break;
		}

		// If there is no unassigned location, we are done
		if (variable == null)
			return true; // success!

		int row = variable.row;
		int col = variable.col;

		// consider digits 1 to 9
		for (int num = 1; num <= 9; num++)
		{
			if (isSafe(grid, row, col, num) && (!forwordCheckOption || forwardCheck(grid, row, col, num)) && (!arc || arcConstistency(grid, row, col, num)))
			{
				// make tentative assignment
				// grid[row][col] = num;
				// System.out.println("____________________________");
				// printGrid(grid);
				// return, if success, yay!
				if (SolveSudoku(grid, orderingFunc, forwordCheckOption, arc))
					return true;

				// failure, un-assign & try again
				grid[row][col] = UNASSIGNED;   
			}
		}
		// Increment the number of backtracks
		numBacktracks++;
		return false; // This triggers backtracking
	}

	/////////////////////////////////////////////////////////////////////
	// Searches the grid to find an entry that is still unassigned. If
	// found, the reference parameters row, col will be set the location
	// that is unassigned, and true is returned. If no unassigned entries
	// remain, null is returned.
	/////////////////////////////////////////////////////////////////////
	static SudokuCoord OrderingOpt1(int grid[][])
	{
		for (int row = 0; row < N; row++)
			for (int col = 0; col < N; col++)
				if (grid[row][col] == UNASSIGNED)
					return new SudokuCoord(row, col);
		return null;
	}

	/////////////////////////////////////////////////////////////////////
	// TODO: Implement the following orderings, as specified in the
	// project description. You MAY feel free to add extra parameters if
	// needed (you shouldn't need to for the first two, but it may prove
	// helpful for the last two methods).
	/////////////////////////////////////////////////////////////////////
	// Just backwards
	static SudokuCoord OrderingOpt2(int grid[][])
	{
		for (int row = (N - 1); row >= 0; row--)
			for (int col = (N - 1); col >= 0; col--)
				if (grid[row][col] == UNASSIGNED)
					return new SudokuCoord(row, col);
		return null;
	}

	static SudokuCoord OrderingOpt3(int grid[][])
	{
		// Select random position scan forward then backward from there returning first unassigned positition
		double size = N; // have to cast to doubles
		int startingRow = (int)(Math.random() * size);
		for (int row = startingRow; row < N; row++)
			for (int col = 0; col < N; col++)
				if (grid[row][col] == UNASSIGNED)
					return new SudokuCoord(row, col);

		for (int row = (startingRow - 1); row >= 0; row--)
			for (int col = (N - 1); col >= 0; col--)
				if (grid[row][col] == UNASSIGNED)
					return new SudokuCoord(row, col);

		return null;
	}

	static SudokuCoord OrderingOpt4(int grid[][])
	{
		int [] currentMin = {0, 0, 100};
		int cellOptions = 0;
		for (int row = 0; row < N; row++)
			for (int cell = 0; cell < N; cell++)
				if (grid[row][cell] == UNASSIGNED && currentMin[2] >= (cellOptions = numberOfOptionsLeft(grid, row, cell)) ) {
					currentMin[0] = row;
					currentMin[1] =	cell;
					currentMin[2] = cellOptions;
				}
		if (100 == currentMin[2]) { return null; }
		return new SudokuCoord(currentMin[0], currentMin[1]);
	}


	static SudokuCoord OrderingOpt5(int grid[][])
	{
		int [] currentMax = {0, 0, -100};
		int cellOptions = 0;
		for (int row = 0; row < N; row++)
			for (int cell = 0; cell < N; cell++)
				if (grid[row][cell] == UNASSIGNED && currentMax[2] <= (cellOptions = numberOfOptionsLeft(grid, row, cell)) ) {
					currentMax[0] = row;
					currentMax[1] =	cell;
					currentMax[2] = cellOptions;
				}
		if (100 == currentMax[2]) { return null; }
		return new SudokuCoord(currentMax[0], currentMax[1]);
	}

	static boolean forwardCheck(int grid[][], int row, int col, int num) {
		grid[row][col] = num;
		for (int gridRow = 0; gridRow < N; gridRow++)
			for (int cell = 0; cell < N; cell++)
				if (grid[gridRow][cell] == UNASSIGNED && numberOfOptionsLeft(grid, gridRow, cell) == 0){
					grid[row][col] = UNASSIGNED;
					return false;
				}
		return true;
	}

	static int numberOfOptionsLeft(int grid[][], int row, int col) {
		int numberOfLegalNumbers = 0;
		for (int option = 1; option <= 9; option++)
			if (isSafe(grid, row, col, option))
				numberOfLegalNumbers++;
		return numberOfLegalNumbers;
	}

	static void initlizeNodeDomains(int [][] grid) {
		for (int row = 0; row < N; row++)
			for (int cell = 0; cell < N; cell++)
				System.arraycopy( DOMAIN, 0, nodeDomains[row][cell], 0, DOMAIN.length );;
	}

	static boolean arcConstistency(int [][] grid, int placementRow, int placementCell, int num) {
		grid[placementRow][placementCell] = num;
		for (int row = 0; row < N; row++)
			for (int cell = 0; cell < N; cell++)
				for (int option = 0; option < nodeDomains[row][cell].length; option++){
						pruneNode(grid, row, cell);
						System.out.println("__________");
						System.out.println(nodeDomains[row][cell].length);
						if ( nodeDomains[row][cell].length == 0)
							return false;
						arcConstistency(grid, row, cell, option);
						// grid[placementRow][placementCell] = UNASSIGNED;
				}
		return true;
	}

	static void removeElementFromArray(int row, int cell, int deleteIndex) {
		int [] newDomain = new int [nodeDomains[row][cell].length - 1];
		int removed = 0;
		// TODO: clean the catches up
		if (nodeDomains[row][cell].length <= 2) {
			System.out.println(" less than 2");
			if (nodeDomains[row][cell].length == 0) {
				System.out.println("--0--");
				newDomain = new int [0];
			}
			else if (deleteIndex == 1) {
				System.out.println("--1-0--");
				newDomain = new int []{nodeDomains[row][cell][0]};
			}
			else {
				System.out.println("--1-1--");
				newDomain = new int []{nodeDomains[row][cell][1]};
			}
		} 
		else {
			for (int option = 0; option < nodeDomains[row][cell].length; option++) {
				if (option == deleteIndex) { removed++; continue; }
				newDomain[option - removed] = nodeDomains[row][cell][option];
			}
		}
		nodeDomains[row][cell] = newDomain;
	}


	static boolean pruneNode(int [][] grid, int row, int cell) {
		boolean pruned = false;
		for(int option = 0; option < nodeDomains[row][cell].length; option++)
			if (!isSafe(grid, row, cell, nodeDomains[row][cell][option])) {
				removeElementFromArray(row, cell, option);
				pruned = true;
			}
		return pruned;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// in the specified row matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInRow(int grid[][], int row, int num)
	{
		for (int col = 0; col < N; col++)
			if (grid[row][col] == num)
				return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// in the specified column matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInCol(int grid[][], int col, int num)
	{
		for (int row = 0; row < N; row++)
			if (grid[row][col] == num)
				return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// within the specified 3x3 box matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInBox(int grid[][], int boxStartRow, int boxStartCol, int num)
	{
		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 3; col++)
				if (grid[row+boxStartRow][col+boxStartCol] == num)
					return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether it will be legal to assign
	// num to the given row, col location.
	/////////////////////////////////////////////////////////////////////
	static boolean isSafe(int grid[][], int row, int col, int num)
	{
		// Check if 'num' is not already placed in current row,
		// current column and current 3x3 box
		return !UsedInRow(grid, row, num) &&
				!UsedInCol(grid, col, num) &&
				!UsedInBox(grid, row - row%3 , col - col%3, num);
	}

	/////////////////////////////////////////////////////////////////////
	// A utility function to print grid
	/////////////////////////////////////////////////////////////////////
	static void printGrid(int grid[][])
	{
		for (int row = 0; row < N; row++)
		{
			for (int col = 0; col < N; col++)
			{
				if (grid[row][col] == 0)
					System.out.print("- ");
				else
					System.out.print(grid[row][col] + " ");
				
				if ((col+1) % 3 == 0)
					System.out.print(" ");
			}	    	   
			System.out.print("\n");
			if ((row+1) % 3 == 0)
				System.out.println();
		}
	}
}
