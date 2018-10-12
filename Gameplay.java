import java.util.ArrayList;
import java.util.Arrays;
import tester.*;
import javalib.funworld.World;
import javalib.funworld.WorldScene;
import java.awt.Color;
import javalib.worldimages.*;



// need a way to automatically build a list of cells and then link all those cells together
class GamePlay extends AGameMode {
  ArrayList<Cell> gameCells; // list of all the cells in the game
  boolean multipleBombsPerCell; // whether the user is in easy or hard mode
  boolean gameOver;

  GamePlay(int numberOfRows, int numberOfColumns, int numberOfBombs, ArrayList<Cell> gameCells,
      boolean multipleBombsPerCell) {
    super(numberOfRows, numberOfColumns, numberOfBombs);
    this.gameCells = gameCells;
    this.multipleBombsPerCell = multipleBombsPerCell;
    this.gameOver = false;
  }

  // draws all the images during the game play of minesweeper
  public WorldScene makeScene() {
    WorldImage gameBackground = new RectangleImage(1200, 650, "solid", Color.GRAY);
    WorldImage titleOfGame = new TextImage("MINESWEEPER", 40, Color.RED);
    WorldImage titleOfGameBackGround = new RectangleImage(1200, 50, "solid", Color.GRAY);
    WorldImage bombsRemaining = new RectangleImage(200, 100, "solid", Color.RED);
    WorldImage bombsWords = new TextImage("BOMBS REMAINING", 20, Color.WHITE);
    WorldImage bombsRemainingNumber = new TextImage(this.countBombsRemaining(), 40, Color.WHITE);
    WorldImage gameBoard = this.drawGameBoard();
    WorldImage gameBoardBackground = new RectangleImage(this.numberOfColumns * 15 + 7,
        this.numberOfRows * 15 + 7, "solid", Color.RED);
    WorldImage newGame = new TextImage("NEW GAME", 30, Color.WHITE);
    WorldImage newGameBackground = new RectangleImage(200, 60, "solid", Color.GREEN);
    WorldImage youLose = new TextImage("YOU LOSE", 100, Color.MAGENTA);
    WorldImage youWin = new TextImage("YOU WIN", 100, Color.MAGENTA);

    // start a new game button
    newGame = new OverlayImage(newGame, newGameBackground).movePinhole(-450, 0);

    // the actual minesweeper game board
    gameBoard = new OverlayImage(gameBoard, gameBoardBackground);

    // counter for bombs remaining
    bombsWords = new AboveImage(bombsWords, bombsRemainingNumber);
    bombsRemaining = new OverlayImage(bombsWords, bombsRemaining).movePinhole(450, 0);

    // title of the game
    titleOfGame = new OverlayImage(titleOfGame, titleOfGameBackGround);

    // overlays all the images on the game background 
    gameBackground = new AboveImage(titleOfGame, gameBackground);
    gameBackground = new OverlayImage(bombsRemaining, gameBackground);
    gameBackground = new OverlayImage(newGame, gameBackground);
    gameBackground = new OverlayImage(gameBoard, gameBackground);

    // if the game isn't over then places normal screen
    if (!this.gameOver) {
      return this.getEmptyScene().placeImageXY(gameBackground, 600, 350);
    }
    // if the user won then displays the win message
    else if (this.win()) {
      gameBackground = new OverlayImage(youWin, gameBackground);
      return this.getEmptyScene().placeImageXY(gameBackground, 600, 350);
    }
    // if the user lost then displays the loss message
    else {
      gameBackground = new OverlayImage(youLose, gameBackground);
      return this.getEmptyScene().placeImageXY(gameBackground, 600, 350);
    }
  }

  // counts the bombs remaining in the game
  public String countBombsRemaining() {
    int counter = 0;

    for (Cell c : this.gameCells) {
      counter = counter + c.numberOfFlags;
    }
    // if the user has not clicked yet then no bombs have been planted, but still
    // all of the bombs are remaining
    if (!this.userHasClicked()) {
      return Integer.toString(this.numberOfBombs);
    }
    // subtract the number of flags that the user has placed from the total number of bombs
    else {
      return Integer.toString(this.numberOfBombs - counter);
    }
  }

  // checks if the user has won by checking if the number of bombs is equal to all of the
  // cells that the user has not clicked
  public boolean win() {
    int counter = 0;

    for (Cell c : this.gameCells) {
      if (!c.isShown()) {
        counter += 1;
      }
    }
    return this.numberOfBombs == counter; 
  }

  public void updateAllNonClickedBombs() {
    for (Cell c : this.gameCells) {
      if (!c.isShown() && c.isBomb()) {
        c.updateNonClickedBombs();
      }
    }
  }

  // draws the actual game board of all the cells
  public WorldImage drawGameBoard() {
    WorldImage rowAcc = new EmptyImage(); // placing all cells beside each other in current row
    ArrayList<WorldImage> imageAcc = new ArrayList<WorldImage>();  // list of all rows
    int columnCounter = 0; // current columns

    for (int i = 0;
        i < this.gameCells.size();
        i = i + 1) {

      WorldImage cell = this.gameCells.get(i).drawCell();

      // checks if the cell is the last cell
      if (i == this.gameCells.size() - 1) {
        rowAcc = new BesideImage(rowAcc, cell);
        imageAcc.add(rowAcc);
      }
      // adds the current cell to the current row
      else if (columnCounter < this.numberOfColumns) {
        rowAcc = new BesideImage(rowAcc, cell);
        columnCounter = columnCounter + 1;
      }
      // completes the current row, adds to list of rows, and starts a new row
      else {
        imageAcc.add(rowAcc);
        rowAcc = cell;
        columnCounter = 1;
      }
    }
    return this.alignRows(imageAcc);
  }

  // helper method for drawing the game board that takes in a list of rows and overlays
  // all of the rows
  public WorldImage alignRows(ArrayList<WorldImage> imageAcc) {
    WorldImage gameBoard = new EmptyImage();

    for (WorldImage r : imageAcc) {
      gameBoard = new AboveImage(gameBoard, r);
    }
    return gameBoard;
  }

  // the purpose is to take a list of cells, representing a game board, and link all the 
  // neighboring cells interates through each cell in the given list of cells
  // if it is the first item in the list, then it links all the cells surrounding it, 
  // and the current cell, as if the game board was a 2 x 2 square
  // next it checks to make sure the given cell is not on the bottom row or in the last column
  // this is because these cells do not contain two cells below and one to the right to link
  //  _________      _________
  // |    |    |    |  __|__  |
  // |____|____|    |_|_\|/_|_|
  // |    |    |    | |_/|\_| |
  // |____|____|    |____|____|

  public void linkAllCells(ArrayList<Cell> listOfCells) {
    for (int i = 0;
        i < this.gameCells.size();
        i = i + 1) {

      if (i % this.numberOfColumns < this.numberOfColumns - 1 // checks if not in last column
          && (i < this.numberOfColumns * (this.numberOfRows - 1))) { // checks if not in last row 
        listOfCells.get(i).updateNeighbors(listOfCells.get(i + 1));
        listOfCells.get(i).updateNeighbors(listOfCells.get(i + this.numberOfColumns));
        listOfCells.get(i).updateNeighbors(listOfCells.get(i + this.numberOfColumns + 1));
        listOfCells.get(i + 1).updateNeighbors(listOfCells.get(i + this.numberOfColumns));
        listOfCells.get(i + 1).updateNeighbors(listOfCells.get(i + this.numberOfColumns + 1));
        listOfCells.get(i + this.numberOfColumns).updateNeighbors(listOfCells.get(
            i + this.numberOfColumns + 1));
      }
    }
    this.gameCells = listOfCells;
  }

  // places bombs in random cells on the game board
  public void placeBombs(ArrayList<Cell> listOfCells, int clickedCell) {
    // places bombs in only one cell
    if (!this.multipleBombsPerCell) {
      this.placeBombsSingleCell(listOfCells, clickedCell);
    }
    // possibly places multiple bombs in one cell
    else {
      this.placeBombsMultipleCells(listOfCells, clickedCell);
    }
  }

  // only allows for one bomb to be placed per cell
  public void placeBombsSingleCell(ArrayList<Cell> listOfCells, int clickedCell) {
    ArrayList<Integer> cellsRemaining = this.buildListOfNumbers();
    // ensures that a bomb is never placed in first cell user clicks
    cellsRemaining.remove(clickedCell);

    for (int i = 0;
        i < this.numberOfBombs;
        i = i + 1) {

      int randomNum = this.rand.nextInt(cellsRemaining.size());
      int newBombIndex = cellsRemaining.get(randomNum);
      cellsRemaining.remove(cellsRemaining.get(randomNum));
      listOfCells.get(newBombIndex).makeBomb();
    }
    this.gameCells = listOfCells;
  }

  // allows for the possibility that multiple bombs are placed in a single cell
  public void placeBombsMultipleCells(ArrayList<Cell> listOfCells, int clickedCell) {
    ArrayList<Integer> cellsRemaining = this.buildListOfNumbers();
    // ensures that a bomb is never placed in first cell user clicks
    cellsRemaining.remove(clickedCell);

    for (int i = 0;
        i < this.numberOfBombs;
        i = i + 1) {

      int randomNum = this.rand.nextInt(cellsRemaining.size());
      int newBombIndex = cellsRemaining.get(randomNum);
      listOfCells.get(newBombIndex).makeBomb();
    }
    this.gameCells = listOfCells;
  }

  // builds a list of numbers representing indices of all the cells in the game
  public ArrayList<Integer> buildListOfNumbers() {
    ArrayList<Integer> listAcc = new ArrayList<Integer>();

    for (int i = 0;
        i < this.gameCells.size();
        i = i + 1) {
      listAcc.add(i);
    }
    return listAcc;
  }


  // handles all of the users left and right mouse clicks
  public World onMouseClicked(Posn pos, String buttonName) {
    int leftEdgeOfBoard = 600 - 15 * this.numberOfColumns / 2;
    int rightEdgeOfBoard = 600 + 15 * this.numberOfColumns / 2;
    int topOfBoard = 350 - 15 * this.numberOfRows / 2;
    int bottomOfBoard = 350 + 15 * this.numberOfRows / 2;

    // allows the user to start a new game
    if (this.checkCoords(950, 1150, 320, 380, pos)) {
      return new StartGame(this.numberOfRows, this.numberOfColumns, this.numberOfBombs);
    }
    // checks if the user clicks in an area where there is no interaction available
    if (!this.checkCoords(leftEdgeOfBoard, rightEdgeOfBoard, topOfBoard, bottomOfBoard, pos)) {
      return this;
    }

    // finds what cell the user clicked
    Cell clickedCell = this.gameCells.get(this.indexFinder(pos, leftEdgeOfBoard, topOfBoard));

    // does nothing if user clicks in area that doesn't permit interaction or game is over
    if (!this.checkCoords(leftEdgeOfBoard, rightEdgeOfBoard, topOfBoard, bottomOfBoard, pos)) {
      return this;
    }
    // adds a flag to a cell even if user has not yet clicked a cell
    else if (!this.userHasClicked() && buttonName.equals("RightButton")) {
      clickedCell.addFlag();
      return this;
    }
    // on users first click, it shows the cell the user clicked, links all of the cells in the
    // game, places bombs in some of the remaining cells, and flood fills surrounding cells
    else if (!this.userHasClicked()) {
      clickedCell.show();
      this.linkAllCells(this.gameCells);
      this.placeBombs(this.gameCells, this.indexFinder(pos, leftEdgeOfBoard, topOfBoard));
      clickedCell.floodFill();
      return this;
    }
    // flags a cell the user thinks is a bomb
    else if (buttonName.equals("RightButton")) {
      clickedCell.addFlag();
      return this;
    }
    // shows the cell the user clicked and flood fills surrounding cells
    else if (this.userHasClicked()) {
      // if the cell is a bomb then ends the game
      if (clickedCell.isBomb()) {
        this.updateAllNonClickedBombs();
        this.gameOver = true;
      }
      // if the cell is not shown then shows it, flood fills, and checks for win
      else if (!clickedCell.isShown()) {
        clickedCell.floodFill();
        this.checkForWin();
      }
      else {
        return this;
      }
      return this;
    }
    // if user does something else, program changes nothing
    else {
      return this;
    }
  }

  // checks if the user has won the game and if they have, it shows all the bombs,
  // and displays the win message
  public void checkForWin() {
    if (this.win()) {
      this.updateAllNonClickedBombs();
      this.gameOver = true;
    }
    return;
  }

  // checks if the user has clicked a cell yet
  public boolean userHasClicked() {
    int counter = 0;

    for (Cell c : this.gameCells) {
      if (c.isShown()) {
        counter += 1;
      }
    }
    return counter > 0;
  }

  // finds the index of the cell the user just clicked
  // essentially finds the index of the first cell in the row of the clicked cell
  // and then adds whatever column the clicked cell is in to give the index
  public int indexFinder(Posn posn, int leftBound, int topBound) {
    return this.rowFinder(topBound, posn.y) * this.numberOfColumns + 
        this.columnFinder(leftBound, posn.x);
  }

  // determines what column the clicked cell is in, first column is 0, last is columns - 1
  public int columnFinder(int leftBound, int xPosn) {
    int counter = 0;

    while (counter < this.numberOfColumns) {
      if (xPosn <= leftBound + 15 * (1 + counter)) {
        return counter;
      }
      else {
        counter = counter + 1; 
      }
    }
    return counter;
  }

  // determines what row the clicked cell is in, first row is 0, last is rows - 1
  public int rowFinder(int topBound, int yPosn) {
    int counter = 0;

    while (counter < this.numberOfRows) {
      if (yPosn <= topBound + 15 * (1 + counter)) {
        return counter;
      }
      else {
        counter = counter + 1; 
      }
    }
    return counter;
  }
}

class ExampleGame1 {

  Cell topleft;
  Cell topmiddle;
  Cell topright;
  Cell middleleft;
  Cell middle;
  Cell middleright;
  Cell bottomleft;
  Cell bottommiddle;
  Cell bottomright;
  ArrayList<Cell> list1;
  WorldImage newGame = new TextImage("NEW GAME", 30, Color.WHITE);
  WorldImage newGameBackground = new RectangleImage(200, 60, "solid", Color.GREEN); 
  WorldImage hiddenCellFill = new RectangleImage(14, 14, "solid", Color.DARK_GRAY);
  WorldImage shownCellFill = new RectangleImage(14, 14, "solid", Color.LIGHT_GRAY);
  WorldImage bomb = new CircleImage(4, "solid", Color.RED);
  WorldImage cellBorder = new RectangleImage(15, 15, "solid", Color.BLACK);
 
  
  void InitGamePlay() {
    topleft = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topmiddle, middleleft, middle)));
    topmiddle = new Cell(1, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topleft, topright, middleleft, middle, middleright)));
    topright = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topmiddle, middle, middleright)));
    middleleft = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topleft, topmiddle, middle, bottomleft, bottommiddle)));
    middle = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topleft, topmiddle, topright, middleleft, 
            middleright, bottomleft, bottommiddle, bottomright)));
    middleright = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topmiddle, topright, middle, bottommiddle, bottomright)));
    bottomleft = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(middleleft, middle, bottommiddle)));
    bottommiddle = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(middleleft, middle, middleright,
            bottomleft, bottomright)));
    bottomright = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(middle, middleright, bottommiddle)));


    list1 = new ArrayList<Cell>(Arrays.asList(topleft, topright, middleleft, middleright));  

  }

  boolean testrowFinder(Tester t) {
    this.InitGamePlay();
    GamePlay g1 = new GamePlay(2, 2, 1, list1, false);
    return t.checkExpect(g1.rowFinder(0, 5), true);
  }
  
 

  boolean testIndexFinder(Tester t) {
    this.InitGamePlay();
    GamePlay g1 = new GamePlay(2, 2, 1, list1, false);
    return t.checkExpect(g1.indexFinder(new Posn(20, 20), 10, 0), 10);
  }

  boolean testbuildListOfNumbers(Tester t) {
    this.InitGamePlay();
    GamePlay g1 = new GamePlay(2, 2, 1, list1, false);
    return t.checkExpect(g1.buildListOfNumbers(), new ArrayList<Integer>());
  }

  boolean testCountBombsRemaining(Tester t) {
    this.InitGamePlay();
    GamePlay g1 = new GamePlay(2, 2, 1, list1, false);
    return t.checkExpect(g1.countBombsRemaining(), "1");
  }

  boolean testisBomb(Tester t) {
    this.InitGamePlay();
    return t.checkExpect(middleleft.isBomb(), true)
        && t.checkExpect(middleright.isBomb(), false);
  }

}

