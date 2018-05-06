package it.polimi.ingsw;

import java.util.List;

public class PlayerWindow {

    private WindowPattern windowPattern;
    //if the array is empty the value of the cell is a Die with number equal to 0
    private Cell[][] diceGrid;

    public PlayerWindow() {
        diceGrid = new Cell[WindowPattern.ROWS][WindowPattern.COLUMNS];
        for (int i = 0; i < WindowPattern.ROWS; i++)
            for (int j = 0; j < WindowPattern.COLUMNS; j++)
                diceGrid[i][j] = new Cell();

    }


    /**
     * Return a copy of the cell containing the die at the given coordinates
     *
     * @param row
     * @param column
     * @return Copy of the cell
     */
    public Cell getCellAt(int row, int column) {
        Cell cell = new Cell();
        if (diceGrid[row][column].isEmpty()) {
            return cell;
        } else {
            Die die = new Die(diceGrid[row][column].getDie().getColor());
            die.setNumber(diceGrid[row][column].getDie().getNumber());
            cell.setDie(die);
            return cell;
        }
    }

    /**
     * Add die in the cell at the given coordinates
     *
     * @param die
     * @param row
     * @param column
     * @return True if the die was added, false if the cell wasn't empty
     */
    public boolean addDie(Die die, int row, int column) {
        if (!diceGrid[row][column].isEmpty()) {
            return diceGrid[row][column].setDie(die);
        } else {
            return false;
        }

    }

    /**
     * Move die from the old coordinate to the new coordinate
     *
     * @param oldRow
     * @param oldColumn
     * @param newRow
     * @param newColumn
     * @return True if the die was moved, otherwise false
     */
    public boolean moveDie(int oldRow, int oldColumn, int newRow, int newColumn) {
        if (diceGrid[newRow][newColumn].setDie(diceGrid[oldRow][oldColumn].getDie())) {
            //provo ad aggiungerlo se tutto bene dico true e rimuovo quello vecchio
            return diceGrid[oldRow][oldColumn].removeDie();
        } else {
            return false;
        }
    }


    public WindowPattern getWindowPattern() {
        return windowPattern;
    }

    public boolean checkPlacement(Die die, int row, int column) {
        if (row < 0 || row >= WindowPattern.ROWS)
            return false;
        if (column < 0 || column >= WindowPattern.COLUMNS)
            return false;
        return windowPattern.getContraints()[row][column].checkConstraint(die);
    }
}
