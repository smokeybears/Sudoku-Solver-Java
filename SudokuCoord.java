/////////////////////////////////////////////////////////////////////////////////
// CS 430 - Artificial Intelligence
// Project 4 - Sudoku Solver w/ Variable Ordering and Forward Checking
// File: SudokuCoord.java
//
// Description: This class represents a Sudoku coordinate (square), which acts
// as a variable for our constraint satisfaction problem (CSP)
/////////////////////////////////////////////////////////////////////////////////
public class SudokuCoord
{
	public int row;
	public int col;
	
	SudokuCoord()
	{
		row = 0;
		col = 0;
	}
	
	SudokuCoord(int r, int c)
	{
		row = r;
		col = c;
	}
}
