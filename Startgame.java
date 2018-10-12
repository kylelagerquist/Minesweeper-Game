import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import javalib.funworld.World;
import java.awt.Color;
import javalib.worldimages.*;


// TestBigBang to play 

//////////////////////////////////////
// EXTRA CREDIT COMPLETED:

// 1) TRACKS BOMBS REMAINING
// 2) RESTART THE GAME
// 3) ALLOW PLAYER TO SELECT ROWS, COLUMNS, BOMBS
// 4) ALLOW PLAYER TO SELECT ANY COMBO OF ROWS, COLUMNS, BOMBS
// 5) GAME MODE FOR MORE THAN ONE MINE PER CELL
// 6) START SCREEN
// 7) FIRST CELL USER CLICKS IS NEVER BOMB 

//////////////////////////////////////



// At any given time the game is either in the:
//    - Start game phase
//    - During game phase (GamePlay)
//    - End of game phase (clicked on bomb or only bombs remaining)
abstract class AGameMode extends World {
  int numberOfRows; // how many rows in the game
  int numberOfColumns; // how many columns in the game
  int numberOfBombs; // how many bombs in the game
  Random rand; // random number generator

  // constructor for a randomly generated input
  AGameMode(int numberOfRows, int numberOfColumns, int numberOfBombs) {
    this(numberOfRows, numberOfColumns, numberOfBombs, new Random());
  }

  // constructor for user to select random seed
  AGameMode(int numberOfRows, int numberOfColumns, int numberOfBombs, Random r) {
    this.numberOfRows = numberOfRows;
    this.numberOfColumns = numberOfColumns;
    this.numberOfBombs = numberOfBombs;
    this.rand = r;
  }

  // creates an invisible box to check if a posn lies within it
  public boolean checkCoords(int leftX, int rightX, int topY, int bottomY, Posn pos) {
    return (pos.x > leftX && pos.x < rightX) && (pos.y > topY && pos.y < bottomY);
  }
}

// beginning of the game where user customizes the game they are about to play
// decide on how many columns, rows, and bombs
// decide on easy mode (1 bomb per cell) or hard mode (multiple bombs per cell)
class StartGame extends AGameMode {
  String currentInput; // where the user is currently typing

  StartGame(int numberOfRows, int numberOfColumns, int numberOfBombs) {
    super(numberOfRows, numberOfColumns, numberOfBombs);
    this.currentInput = "";
  }

  // draws the starting screen of the game
  public javalib.funworld.WorldScene makeScene() {

    // all neccessary images that need to be drawn
    WorldImage startBackground = new RectangleImage(1200, 700, "solid", Color.GRAY);
    WorldImage titleOfGame = new TextImage("MINESWEEPER", 100, Color.RED);
    WorldImage developedBy = new TextImage("Developers", 60, Color.GREEN);
    WorldImage lagerquist = new TextImage("Kyle Lagerquist", 40, Color.GREEN); 
    WorldImage divincenzo = new TextImage("Christian Divincenzo", 40, Color.GREEN); 
    WorldImage rowsDescription = this.makeDescriptionImage("Number of Rows");
    WorldImage columnsDescription = this.makeDescriptionImage("Number of Columns");
    WorldImage bombsDescription = this.makeDescriptionImage("Number of Bombs");
    WorldImage rowsNumber = this.makeNumberImage(Integer.toString(this.numberOfRows));
    WorldImage columnsNumber = this.makeNumberImage(Integer.toString(this.numberOfColumns));
    WorldImage bombsNumber = this.makeNumberImage(Integer.toString(this.numberOfBombs));
    WorldImage maxDescription = new TextImage("Min: 2, Max: 40", 20, Color.GREEN);
    WorldImage maxBombDescription = new TextImage("Max:" + this.maxBombs(), 20, Color.GREEN);
    WorldImage start = new TextImage("START GAME: EASY", 30, Color.WHITE);
    WorldImage startHard = new TextImage("START GAME: HARD", 30, Color.WHITE);
    WorldImage startAuto = new TextImage("START GAME: AUTO", 30, Color.WHITE);
    WorldImage startGameBackground = new RectangleImage(300, 60, "solid", Color.RED);

    // aligning the game description
    titleOfGame = new AboveImage(titleOfGame, developedBy, lagerquist, divincenzo);

    // aligning the customization options for the game
    rowsDescription = new BesideImage(rowsDescription, rowsNumber);
    columnsDescription = new BesideImage(columnsDescription, columnsNumber);
    bombsDescription = new BesideImage(bombsDescription, bombsNumber);
    WorldImage customization = new AboveAlignImage(AlignModeX.LEFT, rowsDescription, maxDescription,
        columnsDescription, maxDescription, bombsDescription, maxBombDescription);

    // aligning start button
    start = new OverlayImage(start, startGameBackground);
    startHard = new OverlayImage(startHard, startGameBackground);
    startAuto = new OverlayImage(startAuto, startGameBackground);

    // overlaying everything onto the final scene
    startBackground = new OverlayImage(titleOfGame.movePinhole(0, 175), startBackground);
    startBackground = new OverlayImage(customization.movePinhole(0, -175), startBackground);
    startBackground = new OverlayImage(startAuto.movePinhole(-400, -85), startBackground);
    startBackground = new OverlayImage(start.movePinhole(-400, -175), startBackground);
    startBackground = new OverlayImage(startHard.movePinhole(-400, -265), startBackground);

    return this.getEmptyScene().placeImageXY(startBackground, 600, 350);
  }

  // creates a description of the number of something
  public WorldImage makeDescriptionImage(String description) {
    return new OverlayImage(new TextImage(description, 30, Color.RED), 
        new RectangleImage(300, 50, "solid", Color.BLUE));
  }

  // draws the current number the user is typing
  public WorldImage makeNumberImage(String description) {
    return new OverlayImage(new TextImage(description, 30, Color.GREEN), 
        new RectangleImage(76, 50, "solid", Color.ORANGE));
  }

  // Calculates the maximum amount of bombs for the given number of rows and columns
  public String maxBombs() {
    if (this.numberOfColumns * this.numberOfRows == 0) {
      return "";
    }
    else {
      return Integer.toString(this.numberOfColumns * this.numberOfRows - 1);
    }
  }

  // builds a list of cells that are not linked together
  // length of list is equal to rows multiplied by columns
  public ArrayList<Cell> buildCellsList() {
    ArrayList<Cell> cellList = new ArrayList<Cell>();

    for (int i = 0;
        i < this.numberOfColumns * this.numberOfRows;
        i = i + 1) {
      cellList.add(new Cell());
    }
    return cellList;
  }

  // handles the users' clicks
  public World onMouseClicked(Posn posn) {
    // start easy game
    if (this.checkCoords(900, 1100, 495, 555, posn)) {
      return this.readyToStart(false, false);
    }
    // start easy game, autoSolve
    else if (this.checkCoords(900, 1100, 585, 645, posn)) {
      return this.readyToStart(true, true);
    }
    // start hard game
    else if (this.checkCoords(900, 1100, 585, 645, posn)) {
      return this.readyToStart(true, false);
    }
    // type in rows input
    else if (this.checkCoords(712, 788, 435, 485, posn)) {
      this.currentInput = "rows";
      return this;
    }
    // type in columns input 
    else if (this.checkCoords(712, 788, 505, 555, posn)) {
      this.currentInput = "columns";
      return this;
    }
    // type in bombs input
    else if (this.checkCoords(712, 788, 575, 625, posn)) {
      this.currentInput = "bombs";
      return this;
    }
    // user clicks in area where there is nothing to interact with
    else {
      this.currentInput = "";
      return this;
    }
  }

  // checks if the correct data parameters are met in order to start the game
  public World readyToStart(boolean multipleBombs, boolean auto) {
    if (this.numberOfRows < 1
        || this.numberOfRows > 40
        || this.numberOfColumns < 1
        || this.numberOfColumns > 40
        || this.numberOfBombs > this.numberOfColumns * this.numberOfRows - 1) {
      return this;
    }
    // starts game in auto mode
    else if (!multipleBombs && auto) {
      return new AutoSolve(this.numberOfRows, this.numberOfColumns, this.numberOfBombs, 
          this.buildCellsList(), false);
    }
    // starts game in easy mode
    else if (!multipleBombs) {
      return new GamePlay(this.numberOfRows, this.numberOfColumns, this.numberOfBombs, 
          this.buildCellsList(), false);
    }
    // starts game in hard mode
    else {
      return new GamePlay(this.numberOfRows, this.numberOfColumns, this.numberOfBombs, 
          this.buildCellsList(), true);
    }
  }

  // handles all of the users key input
  public World onKeyEvent(String key) {

    // list of possible numbers user can click
    ArrayList<String> listOfNumbers = new ArrayList<String>(Arrays.asList("0", "1", "2",
        "3", "4", "5", "6", "7", "8", "9"));
    // deletes last integer in rows input
    if (key.equals("\b") && this.currentInput.equals("rows")) {
      this.numberOfRows = this.deleteLast(this.numberOfRows);
      return this;
    }
    // deletes last integer in columns input
    else if (key.equals("\b") && this.currentInput.equals("columns")) {
      this.numberOfColumns = this.deleteLast(this.numberOfColumns);
      return this;
    }
    // deletes last integer in bombs input
    else if (key.equals("\b") && this.currentInput.equals("bombs")) {
      this.numberOfBombs = this.deleteLast(this.numberOfBombs);
      return this;
    }
    // user clicks a key that has no output
    else if (!listOfNumbers.contains(key)) {
      return this;
    }
    // adds integer to rows input
    else if (this.currentInput.equals("rows") && smallInteger(this.numberOfRows)) {
      this.numberOfRows = Integer.parseInt(Integer.toString(this.numberOfRows) + key);
      return this;
    }
    // adds integer to columns input
    else if (this.currentInput.equals("columns") && smallInteger(this.numberOfColumns)) {
      this.numberOfColumns = Integer.parseInt(Integer.toString(this.numberOfColumns) + key);
      return this;
    }
    // adds integer to bombs input
    else if (this.currentInput.equals("bombs") && smallInteger(this.numberOfBombs)) {
      this.numberOfBombs = Integer.parseInt(Integer.toString(this.numberOfBombs) + key);
      return this;
    }
    // user clicks a key that has no output
    else {
      return this;
    }
  }

  // checks if an integer has 4 or less digits
  public boolean smallInteger(int given) {
    return Integer.toString(given).length() < 4;
  }

  // deletes the last digit from an integer
  public int deleteLast(int given) {
    if (String.valueOf(given).length() > 1) {
      return Integer.parseInt(String.valueOf(given).substring(0, 
          String.valueOf(given).length() - 1));
    }
    else {
      return 0;
    }
  }
}

class TestBigBang {

  boolean testWorld(Tester t) {
    return new StartGame(20, 20, 40).bigBang(1200, 700);
  }
}

class ExampleWorld {
  int longint = 12345;
  int shortint = 123;
  int onedigit = 5;
  StartGame game2 = new StartGame(3, 3, 1);
  StartGame game1 = new StartGame(2, 2, 1);
  StartGame game3 = new StartGame(0, 0, 0);
  Posn posn1 = new Posn(50, 50);
  Posn posn2 = new Posn(0, 50);
  Posn posn3 = new Posn(1200, 1200);
  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  Cell cell5;
  Cell cell6;
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
  ArrayList<Cell> list2;
  WorldImage newGame = new TextImage("NEW GAME", 30, Color.WHITE);
  WorldImage newGameBackground = new RectangleImage(200, 60, "solid", Color.GREEN); 

  void InitGamePlay() {
    topleft = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topmiddle, middleleft, middle)));
    topmiddle = new Cell(1, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topleft, topright, middleleft, middle, middleright)));
    topright = new Cell(0, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topmiddle, middle, middleright)));
    middleleft = new Cell(1, false, 0, false,
        new ArrayList<Cell>(Arrays.asList(topleft, topmiddle, middle, bottomleft, bottommiddle)));
    middle = new Cell(0, true, 0, false,
        new ArrayList<Cell>(Arrays.asList(topleft, topmiddle, topright, 
            middleleft, middleright, bottomleft, bottommiddle, bottomright)));
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
    list2 = new ArrayList<Cell>(Arrays.asList(cell1, cell2, cell3, cell4));

  }

  boolean testrowFinder(Tester t) {
    this.InitGamePlay();
    GamePlay g1 = new GamePlay(2, 2, 1, list1, false);
    return t.checkExpect(g1.rowFinder(0, 5), 0)
        && t.checkExpect(g1.rowFinder(5, 5), 0);
  }

  boolean testIndexFinder(Tester t) {
    this.InitGamePlay();
    GamePlay g1 = new GamePlay(2, 2, 1, list1, false);
    return t.checkExpect(g1.indexFinder(new Posn(20, 20), 10, 0), 2);
  }

  boolean testbuildListOfNumbers(Tester t) {
    ArrayList<Integer> listAcc = new ArrayList<Integer>();

    for (int i = 0;
        i < 4;
        i = i + 1) {
      listAcc.add(i);
    }
    ArrayList<Integer> list4 = listAcc;
  
    this.InitGamePlay();
    GamePlay g1 = new GamePlay(2, 2, 1, list1, false);
    return t.checkExpect(g1.buildListOfNumbers(), list4);
  }
  
  boolean testCountBombsRemaining(Tester t) {
    this.InitGamePlay();
    GamePlay g1 = new GamePlay(2, 2, 1, list1, false);
    return t.checkExpect(g1.countBombsRemaining(), "1");
    
  }

  boolean testisBomb(Tester t) {
    this.InitGamePlay();
    return t.checkExpect(middleleft.isBomb(), true)
        && t.checkExpect(middleright.isBomb(), false)
        && t.checkExpect(bottomleft.isBomb(), false);
    
  }
  


  boolean testMaxBombs(Tester t) {
    return t.checkExpect(game1.maxBombs(), "3")
        && t.checkExpect(game2.maxBombs(), "8")
        && t.checkExpect(game3.maxBombs(), "");
  }


  boolean testOnKeyEvent(Tester t) {
    return t.checkExpect(game1.onKeyEvent("\b"), new StartGame(2, 2, 1))
        && t.checkExpect(game2.onKeyEvent("\b"), new StartGame(3, 3, 1))
        && t.checkExpect(game3.onKeyEvent("\b"), new StartGame(0, 0, 0));
  }

  boolean testCheckCoords(Tester t) {
    return t.checkExpect(game1.checkCoords(0, 100, 0, 100, posn1), true)
        && t.checkExpect(game1.checkCoords(0, 100, 0, 100, posn2), false)
        && t.checkExpect(game3.checkCoords(450, 750, 50, 400, posn3), false);
  }

  boolean testsmallInteger(Tester t) {
    return t.checkExpect(game1.smallInteger(shortint), true)
        && t.checkExpect(game1.smallInteger(longint), false);
  }

  boolean testDeleteLast(Tester t) {
    return t.checkExpect(game1.deleteLast(longint), 1234)
        && t.checkExpect(game1.deleteLast(onedigit), 0);
  }
  
  boolean testmakeDescriptionImage(Tester t) {
    return t.checkExpect(game1.makeDescriptionImage("Number of Rows"), 
        new OverlayImage(new TextImage("Number of Rows", 30, Color.RED), 
        new RectangleImage(300, 50, "solid", Color.BLUE)))
        && t.checkExpect(game2.makeDescriptionImage("Number of Bombs"), 
            new OverlayImage(new TextImage("Number of Bombs", 30, Color.RED), 
                new RectangleImage(300, 50, "solid", Color.BLUE)));
  }
  
  boolean testmakeNumberImage(Tester t) {
    return t.checkExpect(game1.makeNumberImage((Integer.toString(2))), 
        new OverlayImage(new TextImage("2", 30, Color.GREEN), 
        new RectangleImage(76, 50, "solid", Color.ORANGE)));
  }
  
  boolean testCountNeighboringBombs(Tester t) {
    this.InitGamePlay();
    return t.checkExpect(topleft.countNeighboringBombs(), 2)
        && t.checkExpect(topmiddle.countNeighboringBombs(), 1);
  }
  
  boolean testHasBeenClicked(Tester t) {
    this.InitGamePlay();
    return t.checkExpect(middle.hasBeenClicked, true)
        && t.checkExpect(topright.hasBeenClicked, false);
  }
  
  boolean testisshown(Tester t) {
    this.InitGamePlay();
    return t.checkExpect(middle.isShown(), true)
        && t.checkExpect(topright.isShown(), false);
  }
  
  
}