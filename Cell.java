import java.util.ArrayList;
import java.awt.Color;
import javalib.worldimages.*;


// represents a single cell on the game board
class Cell {
  int numberOfBombs; // whether or not this cell contains a bomb
  boolean hasBeenClicked; // whether or not this bomb has been clicked and shown
  int numberOfFlags; // number of flags in current cell
  boolean notClickedBomb; // represents a cell that is a bomb and has not been clicked
  ArrayList<Cell> neighboringCells; // list of all the cells neighboring this current cell

  Cell() {
    this.numberOfBombs = 0;
    this.hasBeenClicked = false;
    this.numberOfFlags = 0;
    this.neighboringCells = new ArrayList<Cell>();
    this.notClickedBomb = false;
  }

  Cell(int numberOfBombs, boolean hasBeenClicked, int numberOfFlags, 
      boolean notClickedBomb, ArrayList<Cell> neighboringCells) {
    this.numberOfBombs = numberOfBombs;
    this.hasBeenClicked = hasBeenClicked;
    this.numberOfFlags = numberOfFlags;
    this.notClickedBomb = notClickedBomb;
    this.neighboringCells = neighboringCells;
  }

  // determines whether this cell contains a bomb or not
  public boolean isBomb() {
    return this.numberOfBombs > 0;
  }

  // determines whether this cell has been shown
  public boolean isShown() {
    return this.hasBeenClicked;
  }

  // updates all the cells that are bombs and have not been clicked
  public void updateNonClickedBombs() {
    this.notClickedBomb = true;
    return;
  }

  // checks to see whether the given cell is not already in the list of neighbors
  // if it is not then it updates the list of neighbors
  // also ensure whether this cell is in the given cell's list of neighbors
  void updateNeighbors(Cell neighbor) {
    if (!this.neighboringCells.contains(neighbor)) {
      this.neighboringCells.add(neighbor);
    }
    if (!neighbor.neighboringCells.contains(this)) {
      neighbor.neighboringCells.add(this);
    }
    return;
  }

  // updates the current cell to represent that it has been clicked and contents should be shown
  public void show() {
    this.hasBeenClicked = true;
    return;
  }

  // draws the individual cell
  public WorldImage drawCell() {
    WorldImage hiddenCellFill = new RectangleImage(14, 14, "solid", Color.DARK_GRAY);
    WorldImage shownCellFill = new RectangleImage(14, 14, "solid", Color.LIGHT_GRAY);
    WorldImage bomb = new CircleImage(4, "solid", Color.RED);
    WorldImage cellBorder = new RectangleImage(15, 15, "solid", Color.BLACK);
    WorldImage numberOfFlags = new TextImage(Integer.toString(this.numberOfFlags), 8, Color.ORANGE);

    // cell that has been clicked and has no neighboring bombs
    if (this.notClickedBomb) {
      return new OverlayImage(bomb, new OverlayImage(hiddenCellFill, cellBorder));
    }
    // cell that has not been clicked and has been flagged
    else if (!this.hasBeenClicked && this.numberOfFlags > 0) {
      return new OverlayImage(numberOfFlags, new OverlayImage(hiddenCellFill, cellBorder));
    }
    // cell that has not been clicked
    else if (!this.hasBeenClicked) {
      return new OverlayImage(hiddenCellFill, cellBorder);
    }
    // cell that has been clicked and has a bomb
    else if (this.numberOfBombs > 0) {
      return new OverlayImage(bomb, new OverlayImage(shownCellFill, cellBorder));
    }
    // cell that has been clicked and has bombs neighboring it
    else if (this.countNeighboringBombs() > 0) {
      return new OverlayImage(new TextImage(Integer.toString(this.countNeighboringBombs()), 
          12, Color.RED), 
          new OverlayImage(shownCellFill, cellBorder));
    }
    else {
      return new OverlayImage(shownCellFill, cellBorder);
    }
  }

  // counts all of the bombs in the cells neighboring the current cell
  public int countNeighboringBombs() {
    int counter = 0;

    for (int i = 0;
        i < this.neighboringCells.size();
        i = i + 1) {

      Cell cell = this.neighboringCells.get(i);

      if (cell.isBomb()) {
        counter = counter + cell.numberOfBombs;
      }
    }
    return counter;
  }


  // adds a bomb to the current cell
  public void makeBomb() {
    this.numberOfBombs = this.numberOfBombs + 1;
    return;
  }

  // adds a flag to current cell only if it has not been clicked
  public void addFlag() {
    if (this.isShown()) {
      return;
    }
    else {
      this.numberOfFlags = this.numberOfFlags + 1;
      return;
    }
  }

  // shows the current cell and then checks if there are no adjacent bombs
  // if there are not then flood fills that cell
  public void floodFill() {
    this.show();

    if (this.countNeighboringBombs() == 0) {
      this.floodFillHelper();
    }
  }

  // goes through all the cells neighboring a cell to check if they need to 
  // be shown also because of flood fill
  // if the cell is already shown then we do not need to check it
  public void floodFillHelper() {

    for (int i = 0;
        i < this.neighboringCells.size();
        i = i + 1) {
      Cell cell =  this.neighboringCells.get(i);

      if (!cell.isShown()) {
        cell.floodFill();
      }
    }
  }
  
}







