import java.io.*;
import java.io.Serializable;
import java.lang.Math;
import java.lang.System.*;
import java.util.*;
import javafx.animation.*;
import javafx.animation.PathTransition.OrientationType;
import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.CheckBox;

/**
 * This is the outermost, main class which encloses everything else as well as extends
 * Application.
 * @author      Hanit, Abhimanyu (16040, 16003)
 * @version     1.7
 * @since       1.0
 */
public class ChainReaction extends Application implements Serializable {
    //Constants
    /**
    * The horizontal length of tile
    */
    final int horizontal    = 45;
    /**
    * The vertical length of tile
    */
    final int vertical      = 45;
    /**
    * The fps of animation
    */
    final double fps        = 60;
    /**
    * The radius of orbs
    */
    final double radius     = 10;
    /**
    * The horizontal length of menu
    */
    final int sizeMenuX     = 400;
    /**
    * The vertical length of menu
    */
    final int sizeMenuY     = 400;
    /**
    * The maximum vertical and horizontal dimension of the grid
    */
    final int maxGridSize   = 15;
    /**
    * The size of all buttons
    */
    final int buttonSize    = 30;
    /**
    * The initial colors of orbs of different players
    */
    transient final Color[] colorList = new Color[] {
        Color.RED,  Color.BLUE,  Color.CHOCOLATE, Color.GREEN, 
        Color.GREY, Color.WHITE, Color.GOLD,      Color.MAROON
    };
    /**
    * The maximum number of players
    */
    final int maxPlayers = colorList.length;
    /**
    * The stage on which the application is displayed
    */
    transient private Stage stage;

    /**
     * This class represents a player, with each having his own instance.
     * @author      Hanit, Abhimanyu (16040, 16003)
     * @version     1.7
     * @since       1.0
     */
    public class Player implements Serializable {
        /**
        * The name of player
        */
        private String name;
        /**
        * The color assigned to player
        */
        private double[] color;
        /**
        * Tells if player is AI controlled
        */
        private Boolean isAI;
        /**
         * The constructor to the class
         * @param name The player's name
         * @param color The player's color
         */
        public Player(String name, Color color) {
            this.name  = name;
            this.color = new double[3];
            this.color[0] = color.getRed();
            this.color[1] = color.getGreen();
            this.color[2] = color.getBlue();
            this.isAI = false;
        }
        /**
         * Getter method for name
         * @return the player's name
         */
        public String getName() {
            return name;
        }
        /**
         * Setter method for name
         * @param name The player's name to be set
         */
        public void setName(String name) {
            this.name = name;
        }
        /**
         * Getter method for color
         * @return the player's color
         */
        public Color getColor() {
            return Color.color(color[0], color[1], color[2]);
        }
        /**
         * Getter method for AI
         * @return the player's AI state
         */
        public Boolean getAI() {
            return isAI;
        }
        /**
         * Setter method for isAI
         * @param ai the player's AI state to be set
         */
        public void setAI(Boolean ai) {
            this.isAI = ai;
        }
        /**
         * Setter method for color
         * @param c the color to be set
         */
        public void setColor(Color c) {
            color[0] = c.getRed();
            color[1] = c.getGreen();
            color[2] = c.getBlue();
        }
        /**
         * AI logic to make move, calls Board.makeMove()
         * @param board the board object on which move is to be made
         */
        public void makeMove(Board board) {
            if (this != board.getPlayer()[board.currentPlayer]) {
                return;
            }
            int n = board.numRows, m = board.numColumns;
            Random rand = new Random();
            while (true) {
                int i = rand.nextInt(n);
                int j = rand.nextInt(m);
                try {
                    board.makeMove(i, j);
                    break;
                } catch (InvalidMoveException e) {}
            }
        }
    }

    /**
     * This class represents a single cell of the game board. It has all information about
     * the current state of the particular cell, as well as the animation components of orbs
     * inside the cell.
     * @author      Hanit, Abhimanyu (16040, 16003)
     * @version     1.7
     * @since       1.0
     */
    public class Cell implements Serializable {
        /**
        * The row number assigned to cell
        */
        private int row;
        /**
        * The rcolumn number assigned to cell
        */
        private int column;
        /**
        * The number of orbs in the cell
        */
        private int numOrbs;
        /**
        * The max number of orbs that the cell can contain before exploding
        */
        private int maxOrbs;
        /**
        * The player that has orbs in the cell
        */
        private int player;
        /**
        * The color of orbs in the cell
        */
        transient private Color color;
        /**
        * The group of orbs in the cell
        */
        transient private Group circles;
        /**
        * The animation of orbs in the cell
        */
        transient private Timeline animation;
        /**
        * The angle of circle with respect to cell centre
        */
        private double theta;
        /**
         * Constructor for Cell object
         * @param row the cell's row on the board
         * @param column the cell's column on the board
         * @param maxOrbs the maxOrbs for the cell
         * @param pane the AnchorPane of the board
         */
        public Cell(int row, int column, int maxOrbs, AnchorPane pane) {
            this.row     = row;
            this.column  = column;
            this.maxOrbs = maxOrbs;
            numOrbs      = 0;
            player       = -1;
            color        = null;
            theta        = 0;

            circles = new Group();
            setAnimation();
            pane.getChildren().add(circles);
        }
        /**
         * load method for restoring the Cell object to a previous state
         * <p>
         * This method is needed because the Animation and GUI components are not
         * serializable, and hence need to be manually restored upon deserializing
         * a previous game state
         * @param color the color of the orbs in this cell
         * @param pane the AnchorPane of the Board
         */
        public void load(Color color, AnchorPane pane) {
            this.color = color;
            circles = new Group();
            setAnimation();
            pane.getChildren().add(circles);
            redraw();
        }
        /**
         * Adds an orb to the cell
         * <p>
         * Whenever a move is made on this cell, this function adds an orb to this
         * cell. This involves either increasing the number of orbs by 1 and redrawing
         * the animation of cell rotation, or if orbs exceed the maximum limit, setting
         * the number of orbs to 1 and notifying the caller of the detonation
         * @param player the player who made the move on this cell
         * @param color the color of the player who made the move
         * @return returns true if the cell detonated
         */
        public Boolean addOrb(int player, Color color) {
            this.player = player;
            this.color  = color;
            ++numOrbs;
            if (numOrbs > maxOrbs) {
                numOrbs = 0;
                this.player = -1;
                this.color  = null;
                theta       = 0;
                redraw();
                return true;
            }
            redraw();
            return false;
        }
        /**
         * Getter method for number of orbs
         * @return number of orbs
         */
        public int getOrbs() {
            return numOrbs;
        }
        /**
         * Getter method for player
         * @return player
         */
        public int getPlayer() {
            return player;
        }
        /**
         * Getter method for the color of orbs in this cell
         * @return the color of orbs in this cell
         */
        public Color getColor() {
            return color;
        }
        /**
         * Creates a Timeline object which handles the rotation of orbs in this cell.
         * <p>
         * This method does a lot of things, since it handles the rotation of orbs
         * inside the cell. There are different speeds, distances and angles
         * involved for the rotation depending on the number of orbs.
         */
        private void setAnimation() {
            animation = new Timeline(
                new KeyFrame(Duration.seconds(1 / fps), 
                new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent actionEvent) {
                        circles.getChildren().clear();
                        int num = numOrbs;
                        if (num == 0) {
                            return;
                        }
                        Point<Double> center = getRectangleCenter(row, column);
                        double X             = center.getX();
                        double Y             = center.getY();
                        double rotationSpeed = (new double[]{5, 1, 1})[num - 1];
                        double distanceX     = radius / (new double[] {30, 2, 2 / Math.sqrt(3)})[num - 1];
                        double distanceY     = radius / (new double[] {30, 2, 2})[num - 1];
                        theta += rotationSpeed;
                        if (theta >= 360) {
                            theta -= 360;
                        }
                        for (int i = 0; i < num; ++i) {
                            double thetaRadians = Math.toRadians(theta + (360 * i) / num);
                            Circle c = new Circle(X - Math.cos(thetaRadians) * distanceX, 
                                                  Y + Math.sin(thetaRadians) * distanceY,
                                                  radius, color);
                            c.setMouseTransparent(true);
                            c.setId(Double.toString(theta));
                            circles.getChildren().add(c);
                        }
                    }
                })
            );
            animation.setCycleCount(Animation.INDEFINITE);
        }
        /**
         * Redraws the animation or orbs in the cell
         */
        public void redraw() {
            animation.pause();
            animation.play();
        }
    }
    
    /**
     * This class is the main class which represents the Board and the current game being
     * played on the board. It has the entire state information of the game, GUI components
     * of the game window and the list of players. Its methods control the game as well
     * as the player's interaction with the game window when the game is in session.
     * 
     * It also has methods for saving/loading/undo/restart/exit, and controls the logic of
     * the game. The animation where cells detonate and orbs move from one cell to other is
     * also handled by the Board class.
     * @author      Hanit, Abhimanyu (16040, 16003)
     * @version     1.7
     * @since       1.0
     */
    public class Board implements Serializable {
        /**
        * The player with current turn
        */
        private int currentPlayer;
        /**
        * The number of players left in the game
        */
        private int playersLeft;
        /**
        * The number of moves made
        */
        private int movesMade;
        /**
        * The number of rows in the board
        */
        private int numRows;
        /**
        * The number of columns in the board
        */
        private int numColumns;
        /**
        * The total number of players in the game
        */
        private int numPlayers;
        /**
        * The array of players containing all the players
        */
        private Player[] player;
        /**
        * The array that tells who made the first move
        */
        private Boolean[] madeFirstMove;
        /**
        * The array that tells which player is still in game
        */
        private Boolean[] isInGame;
        /**
        * The array that contains number of cells contained by players
        */
        private int[] controlledCells;
        /**
        * The array of cells
        */
        private Cell[][] cell;
        /**
        * The queue for bfs
        */
        private Queue<Point<Integer>> queue;
        /**
        * The color of the player with the turn
        */
        transient private Color currentColor;
        /**
        * The anchor pane that holds the game display
        */
        transient private AnchorPane gameDisplay;
        /**
        * The scene that holds gameDisplay
        */
        transient private Scene scene;
        /**
        * The button to exit the game
        */
        transient private Button exitButton;
        /**
        * The button to undo the turn
        */
        transient private Button undoButton;
        /**
        * The button to restart the game
        */
        transient private Button restartButton;
        /**
         * Constructor for board
         */
        public Board() {
            player          = new Player[maxPlayers];
            madeFirstMove   = new Boolean[maxPlayers];
            isInGame        = new Boolean[maxPlayers];
            controlledCells = new int[maxPlayers];
            for (int i = 0; i < maxPlayers; ++i) {
                player[i]          = new Player("Player " + (i + 1), colorList[i]);
                madeFirstMove[i]   = false;
                isInGame[i]        = true;
                controlledCells[i] = 0;
            }
        }
        /**
         * Getter method for player
         * @return player
         */
        public Player[] getPlayer() {
            return player;
        }
        /**
         * Getter method for the number of orbs in a particular cell
         * @param row the cell's row
         * @param column the cell's column
         * @return the number of orbs in a particular cell
         */
        private int getOrbs(int row, int column) {
            return cell[row][column].getOrbs();
        }
        /**
         * Getter method for color of orbs in a particular cell
         * @param row the cell's row
         * @param column the cell's column
         * @return color of orbs in a particular cell
         */
        private Color getColor(int row, int column) {
            return cell[row][column].getColor();
        } 
        /**
         * Saves the board to a file
         * <p>
         * This method writes (serializes) the board object to a save file
         * @param fileName name of the file to save to
         */
        public void save(String fileName) {
            try {
                 FileOutputStream fileOut = new FileOutputStream(fileName);
                 ObjectOutputStream out   = new ObjectOutputStream(fileOut);
                 out.writeObject(this);
                 out.close();
                 fileOut.close();
            } catch(IOException i) {
                 i.printStackTrace();
            }
        }
        /**
         * Loads the board from a file
         * <p>
         * This method loads (deserializes) the board object from a file and
         * copies it to the current object. It then also manually initializes
         * all transient components of Board
         * @param fileName name of the file to load from
         */
        public void load(String fileName) {
            Board board;
            try {
                 FileInputStream fileIn = new FileInputStream(fileName);
                 ObjectInputStream in = new ObjectInputStream(fileIn);
                 board = ((Board) in.readObject());
                 currentPlayer   = board.currentPlayer;
                 playersLeft     = board.playersLeft;
                 movesMade       = board.movesMade;
                 numRows         = board.numRows; 
                 numColumns      = board.numColumns;
                 numPlayers      = board.numPlayers;
                 player          = board.player;
                 madeFirstMove   = board.madeFirstMove;
                 isInGame        = board.isInGame;
                 controlledCells = board.controlledCells;
                 cell            = board.cell;
                 in.close();
                 fileIn.close();
            } catch(IOException i) {
                 i.printStackTrace();
            } catch(ClassNotFoundException c) {
                 System.out.println("Board class not found");
                 c.printStackTrace();
            }
            deleteFile("undoSave");
            setAnimation();
            currentColor = player[currentPlayer].getColor();
            queue        = new LinkedList<Point<Integer>>();
            for (int i = 0; i < numRows; ++i) {
                for (int j = 0; j < numColumns; ++j) {
                    int p = cell[i][j].getPlayer();
                    Color c = null;
                    if (p != -1) {
                        c = player[p].getColor();
                    }
                    cell[i][j].load(c, gameDisplay);
                }
            }
            play();
        }
        /**
         * Undos the previous move
         * <p>
         * This method restores the board state to the state before the latest move
         * It uses the load function for this on the undoSave file created after
         * every move.
         */
        public void undo() {
            try {
                 FileInputStream fileIn = new FileInputStream("undoSave");
                 ObjectInputStream in = new ObjectInputStream(fileIn);
                 FileOutputStream fileOut = new FileOutputStream("undoSaveTemp");
                 ObjectOutputStream out   = new ObjectOutputStream(fileOut);
                 out.writeObject(in.readObject());
                 out.close();
                 fileOut.close();
                 in.close();
                 fileIn.close();
            } catch(Exception i) {
                 i.printStackTrace();
            }
            load("undoSave");
            deleteFile("mainSave");
            if (movesMade > 0) {
                new File("undoSaveTemp").renameTo(new File("mainSave"));
            } else {
               deleteFile("undoSaveTemp");
            }
        }
        /**
         * Initializes the board to a new game with the settings provided
         * as parameters
         * @param numRows number of rows
         * @param numColumns number of columns
         * @param numPlayers number of players
         */
        public void initBoard(int numRows, int numColumns, int numPlayers) {
            this.numRows    = numRows;
            this.numColumns = numColumns;
            this.numPlayers = numPlayers;
            playersLeft     = numPlayers;
            currentPlayer   = 0;
            movesMade       = 0;
            cell            = new Cell[numRows][numColumns];
            queue           = new LinkedList<Point<Integer>>();
            setAnimation();
            for(int i = 0; i < numRows; i++) {
                for(int j = 0; j < numColumns; j++) {
                    int maxOrbs = 3;
                    if (i == 0 || i == numRows - 1) {
                        --maxOrbs;
                    }
                    if (j == 0 || j == numColumns - 1) {
                        --maxOrbs;
                    }
                    cell[i][j] = new Cell(i, j, maxOrbs, gameDisplay);
                }
            }
        }
        /**
         * Sets animation and GUI components
         */
        public void setAnimation() {
            gameDisplay = new AnchorPane();
            scene       = new Scene(gameDisplay);
            setExitButton();
            setUndoButton();
            setRestartButton();
            for(int i = 0; i < numRows; i++) {
                for(int j = 0; j < numColumns; j++) {
                    Rectangle rect = new Rectangle(horizontal * i, vertical * j, horizontal, vertical);
                    setRectangle(rect, i, j);
                    gameDisplay.getChildren().add(rect);
                }
            }
            HBox buttonLocation = new HBox(1);
            gameDisplay.getChildren().add(buttonLocation);
            File tmpDir = new File("undoSave");
            undoButton.setDisable(!tmpDir.exists());
            buttonLocation.getChildren().addAll(exitButton, undoButton, restartButton);
            gameDisplay.setBottomAnchor(buttonLocation, 1.0);
        }
        /**
         * This swiches the scene to the game scene, used to start the game
         */
        public void play() {
            stage.setScene(scene);
            stage.sizeToScene();
            double stageHeight = stage.getHeight();
            double stageWidth = stage.getWidth();
            stage.setHeight(stageHeight + buttonSize);
        }
        /**
         * This method is called when the player makes a move
         * <p>
         * It updates the state of the game as well as temporary save files
         * It calls bfs for the logic of the move
         * @param row the row where move was made
         * @param column the column where move was made
         * @throws InvalidMoveException if move was invalid
         */
        private void makeMove(int row, int column) throws InvalidMoveException {
            Cell currentCell = cell[row][column];
            currentColor     = player[currentPlayer].getColor();
            if (currentCell.getOrbs() > 0 && currentCell.getPlayer() != currentPlayer) {
                throw new InvalidMoveException("Invalid move");
            } else if (row < 0 || row >= numRows) {
                throw new InvalidMoveException("Invalid move");
            } else if (column < 0 || column >= numColumns) {
                throw new InvalidMoveException("Invalid move");
            }
            save("undoSave");
            madeFirstMove[currentPlayer] = true;
            queue.add(new Point<Integer>(row, column));
            File tmpDir = new File("undoSave");
            undoButton.setDisable(!tmpDir.exists());
            bfs();
        }
        /**
         * Controls the logic of a move
         * <p>
         * Performs a breadth first search for updating the game state whenever
         * a move is made, since moves explode in all 4 directions. Also calls
         * appropriate animations when a cell detonates.
         */
        private void bfs() {
            ArrayList<Point<Double>> from = new ArrayList<Point<Double>>();
            ArrayList<Point<Double>> to   = new ArrayList<Point<Double>>();
            Queue<Point<Integer>> tempQ   = new LinkedList<Point<Integer>>();
            while (!queue.isEmpty()) {
                if (playersLeft == 1) {
                    endGame();
                    return;
                }
                Point<Integer> cur = queue.poll();
                int X              = cur.getX(), Y = cur.getY();
                int orbPlayer      = cell[X][Y].getPlayer();
                if (orbPlayer != currentPlayer) {
                    ++controlledCells[currentPlayer];
                    if (orbPlayer != -1) {
                        --controlledCells[orbPlayer];
                        if (controlledCells[orbPlayer] == 0) {
                            isInGame[orbPlayer] = false;
                            --playersLeft;
                        }
                    }
                }
                Boolean detonated = cell[X][Y].addOrb(currentPlayer, currentColor);
                if (detonated) {
                    --controlledCells[currentPlayer];
                    for (int i = X - 1; i <= X + 1; ++i) {
                        for (int j = Y - 1; j <= Y + 1; ++j) {
                            if (i == X && j == Y) {
                                continue;
                            } else if (i != X && j != Y) {
                                continue;
                            } else if (i < 0 || i >= numRows) {
                                continue;
                            } else if (j < 0 || j >= numColumns) {
                                continue;
                            } else {
                                tempQ.add(new Point<Integer>(i, j));
                                from.add(getRectangleCenter(X, Y));
                                to.add(getRectangleCenter(i, j));
                            }
                        }
                    }
                }
            }
            if (!tempQ.isEmpty()) {
                queue = tempQ;
                moveCircles(from, to);
            } else {
                if (playersLeft == 1) {
                    endGame();
                    return;
                }
                do {
                    currentPlayer = (currentPlayer + 1) % numPlayers;
                } while (!isInGame[currentPlayer]);
                ++movesMade;
                save("mainSave");
                if (player[currentPlayer].getAI()) {
                    // try {
                    //     Thread.sleep(500);
                    // } catch (InterruptedException e) {
                    //     e.printStackTrace();
                    // }
                    // for (int t = 0; t < 10; ++t) {
                    //     for (int i = 0; i < numRows; ++i) {
                    //         for (int j = 0; j < numColumns; ++j) {
                    //             cell[i][j].redraw();
                    //             try {
                    //                 Thread.sleep(1);
                    //             } catch (InterruptedException e) {
                    //                 e.printStackTrace();
                    //             }
                    //         }
                    //     }
                    // }
                    player[currentPlayer].makeMove(this);
                }
            }
        }
        /**
         * Called when the game ends to delete save files and declare the winner
         */
        private void endGame() {
            int winner = -1;
            for (int i = 0; i < numPlayers; ++i) {
                if (isInGame[i]) {
                    winner = i;
                    break;
                }
            }
            deleteFile("mainSave");
            deleteFile("undoSave");
            showPopup("Congratulations!", player[winner].getName() + " wins.");
            gameDisplay.getChildren().clear();
            menu();
        }
        /**
         * Animates circles moving from one cell to another
         * @param source The list of starting positions for the circles
         * @param dest This list of final positions of each circle
         */
        private void moveCircles(ArrayList<Point<Double>> source, ArrayList<Point<Double>> dest) {
            int n = source.size();
            for (int i = 0; i < n; ++i) {
                double x1 = source.get(i).getX(), y1 = source.get(i).getY();
                double x2 = dest.get(i).getX(),   y2 = dest.get(i).getY();
                Circle circle = new Circle(x1, y1, radius, currentColor);
                gameDisplay.getChildren().add(circle);
                circle.setCenterX(x1);
                circle.setCenterY(y1);
                circle.setMouseTransparent(true);
                Path path = new Path();
                path.getElements().add(new MoveTo(x1, y1));
                path.getElements().add(new LineTo(x2, y2));
                PathTransition animation = new PathTransition();
                animation.setNode(circle);
                animation.setPath(path);
                animation.setOrientation(OrientationType.ORTHOGONAL_TO_TANGENT);
                animation.setInterpolator(Interpolator.LINEAR);
                animation.setDuration(new Duration(500));
                animation.setCycleCount(1);
                if (i < n - 1) {
                    animation.setOnFinished(e -> gameDisplay.getChildren().remove(circle));
                } else {
                    animation.setOnFinished(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            gameDisplay.getChildren().remove(circle);
                            bfs();
                        }
                    });
                }
                animation.play();
            }
        }
        /**
         * Initializes the rectangle object at (i, j)
         * @param rect the Rectangle object
         * @param i the row
         * @param j the column
         */
        private void setRectangle(Rectangle rect, int i, int j) {
            rect.setStroke(Color.RED);
            rect.setFill(Color.BLACK);
            rect.setOnMousePressed(new EventHandler<MouseEvent>() {
                public void handle(MouseEvent e) {
                    try {
                        makeMove(i, j);
                    } catch (InvalidMoveException ex) {
                        // showPopup("Invalid Move", "Sorry, you cannot place an orb there");
                    }
                }
            });
        }
        /**
         * Initializes the Exit button and its handler
         */
        private void setExitButton() {
            exitButton = new Button("Exit");
            exitButton.setPrefHeight(buttonSize);
            exitButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    menu();
                }
            });
        }
        /**
         * Initializes the Restart button and its handler
         */
        private void setRestartButton() {
            restartButton = new Button("Restart");
            restartButton.setPrefHeight(buttonSize);
            restartButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    deleteFile("mainSave");
                    deleteFile("undoSave");
                    Board.this.initBoard(numRows, numColumns, numPlayers);
                    Board.this.play();
                }
            });
        }
        /**
         * Initializes the Undo button and its handler
         */
        private void setUndoButton() {
            undoButton = new Button("Undo");
            undoButton.setPrefHeight(buttonSize);
            undoButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent e) {
                    undo();
                }
            });
        }
    }

    /**
     * This class is used for Point objects of the same (but any) type. It is useful in
     * various places since the game is played on a 2D grid and coordinates of the form
     * (i, j) are everywhere.
     *
     * The fact that it is generic means we can use it for cell coordinates (integers)
     * as well as the positions of the orbs on the Pane (real numbers)
     * @author      Hanit, Abhimanyu (16040, 16003)
     * @version     1.7
     * @since       1.0
     */
    public class Point<T> implements Serializable {
        private T x, y;
        /**
         * Constructor for Point
         * @param x value of x
         * @param y value of y
         */
        public Point(T x, T y) {
            this.x = x;
            this.y = y;
        }
        /**
         * Getter method for X
         * @return X
         */
        public T getX() {
            return x;
        }
        /**
         * Getter method for Y
         * @return Y
         */
        public T getY() {
            return y;
        }
    }

    /**
     * This class is for Exceptions where an invalid move is made by the player.
     * @author      Hanit, Abhimanyu (16040, 16003)
     * @version     1.7
     * @since       1.0
     */
    public class InvalidMoveException extends Exception {
        public InvalidMoveException(String message) {
            super(message);
        }
    }
    /**
     * Displays the settings page from the main menu
     * <p>
     * This method creates a new scene that has settings options and switches the stage to that scene
     * <p>
     * Contains players names and color pickers so that players can change their colours
     * @param board The Board object that contains the array of players
     * @param menuScene The Scene that has main menu so that user can exit out of settings
    */
    public void settings(Board board, Scene menuScene) {
        AnchorPane settings = new AnchorPane();
        Scene settingsScene = new Scene(settings);
        TextField[] textFieldR = new TextField[maxPlayers];
        TextField[] textFieldG = new TextField[maxPlayers];
        TextField[] textFieldB = new TextField[maxPlayers];
        stage.setScene(settingsScene);
        stage.setTitle("Settings");
        stage.setHeight(sizeMenuY + 100);
        stage.setWidth(sizeMenuX + 100);
        Player[] player = board.getPlayer();
        Label nameLabel = new Label("Settings");
        settings.getChildren().add(nameLabel);
        nameLabel.setTranslateY(10);
        nameLabel.setTranslateX(220);
        CheckBox checkAI[] = new CheckBox[maxPlayers];
        ColorPicker[] colorPicker = new ColorPicker[maxPlayers];

        Label playerNameLabel = new Label("Name");
        playerNameLabel.setTranslateX(80);
        playerNameLabel.setTranslateY(40);
        Label isAILabel = new Label("AI controlled");
        isAILabel.setTranslateX(370);
        isAILabel.setTranslateY(40);
        Label colorLabel = new Label("Color");
        colorLabel.setTranslateX(220);
        colorLabel.setTranslateY(40);
        settings.getChildren().addAll(playerNameLabel, isAILabel, colorLabel);

        TextField[] playerName = new TextField[maxPlayers];

        for(int i = 0; i < maxPlayers; i++) {
            playerName[i] = new TextField(player[i].getName());
            // Label temp = new Label(player[i].getName());
            settings.getChildren().add(playerName[i]);
            playerName[i].setTranslateY(80 + i * 30);
            playerName[i].setTranslateX(60);

            colorPicker[i] = new ColorPicker(player[i].getColor());
            colorPicker[i].setTranslateY(80 + i * 30);
            colorPicker[i].setTranslateX(200);
            settings.getChildren().add(colorPicker[i]);

            checkAI[i] = new CheckBox();
            checkAI[i].setSelected(player[i].getAI());
            checkAI[i].setTranslateY(80 + i * 30);
            checkAI[i].setTranslateX(390);
            settings.getChildren().add(checkAI[i]);

        }

        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(100);
        saveButton.setTranslateX(190);
        saveButton.setTranslateY(400);
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Boolean isOk = true;
                for(int i = 0; i < maxPlayers; i++) {
                    player[i].setAI(checkAI[i].isSelected());
                    player[i].setName(playerName[i].getText());
                    for(int j = i + 1; j < maxPlayers; j++) {

                        if(colorPicker[i].getValue().equals(colorPicker[j].getValue())){
                            isOk = false;
                        }
                    }
                    player[i].setColor(colorPicker[i].getValue());
                }
                if(isOk == false) {
                    showPopup("Error", "Two or more RGB values are same");
                }
                else {
                    stage.setHeight(sizeMenuY);
                    stage.setWidth(sizeMenuX);
                    stage.setScene(menuScene);
                }

            }
        });
        settings.getChildren().add(saveButton);
    }

    /**
     * Displays the main menu on the screen at the start of the game
     * <p>
     * This method creates a new scene and switches the stage to that scene
     * <p>
     * Contains drop downs to select size of game, select number of players,
     * settings and play button
    */
 
    public void menu() {
        AnchorPane mainMenu = new AnchorPane();
        Scene menuScene     = new Scene(mainMenu);
        Board board = new Board();

        // reddit.com/r/eyebleach
        stage.setTitle("Chain Reaction");
        stage.setHeight(sizeMenuY);
        stage.setWidth(sizeMenuX);
        stage.setScene(menuScene);
        ChoiceBox<Integer> selectRow     = new ChoiceBox<Integer>();
        ChoiceBox<Integer> selectColumn  = new ChoiceBox<Integer>();
        ChoiceBox<Integer> selectPlayers = new ChoiceBox<Integer>();
        Label nameLabel   = new Label("Chain Reaction");
        Label rowLabel    = new Label("Rows");
        Label columnLabel = new Label("Columns");
        Label playerLabel = new Label("Players");
        nameLabel.setTranslateX(150);
        nameLabel.setTranslateY(10);
        rowLabel.setTranslateX(120);
        rowLabel.setTranslateY(130);
        selectRow.setTranslateX(240);
        selectRow.setTranslateY(130);
        columnLabel.setTranslateX(120);
        columnLabel.setTranslateY(180);
        selectColumn.setTranslateX(240);
        selectColumn.setTranslateY(180);
        playerLabel.setTranslateX(120);
        playerLabel.setTranslateY(80);
        selectPlayers.setTranslateX(240);
        selectPlayers.setTranslateY(80);
        for (int i = 2; i <= maxGridSize; ++i) {
            selectRow.getItems().add(i);
            selectColumn.getItems().add(i);
        }
        selectRow.setValue(9);
        selectColumn.setValue(6);
        for (int i = 2; i <= maxPlayers; ++i) {
            selectPlayers.getItems().add(i);
        }
        selectPlayers.setValue(2);

        Button playButton = new Button("Play");
        playButton.setTranslateX(150);
        playButton.setTranslateY(250);
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                int numPlayers = selectPlayers.getValue();
                int numRows    = selectColumn.getValue();
                int numColumns = selectRow.getValue();
                if (numPlayers > numRows * numColumns) {
                    showPopup("Error! Too many Players", "Number of players selected are\nmore than the grid size");
                    return;
                }
                deleteFile("undoSave");
                deleteFile("mainSave");
                board.initBoard(selectColumn.getValue(), selectRow.getValue(), selectPlayers.getValue());
                board.play();
            }
        });
        Image imageGear = new Image(getClass().getResourceAsStream("Resources/Gear.png"));
        ImageView imageView = new ImageView(imageGear);
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        imageView.setPreserveRatio(true);
        Button settingsButton = new Button("", imageView);
        settingsButton.setTranslateX(340);
        settingsButton.setTranslateY(330);
        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                settings(board, menuScene);
            }
        });
        Button resumeButton = new Button("Resume");
        resumeButton.setTranslateX(150);
        resumeButton.setTranslateY(290);
        resumeButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                board.load("mainSave");
            }
        });
        File tmpDir = new File("mainSave");
        if(tmpDir.exists()){
            mainMenu.getChildren().addAll(resumeButton);
        }
        playButton.setPrefWidth(100);
        resumeButton.setPrefWidth(100);
        selectPlayers.setPrefWidth(50);
        selectRow.setPrefWidth(50);
        selectColumn.setPrefWidth(50);

        // Yeah, get those children
        mainMenu.getChildren().addAll(playButton, selectRow,   selectColumn, selectPlayers,
                                      rowLabel,   columnLabel, playerLabel,  nameLabel, settingsButton);
    }
    /**
     * Displays a popup on the screen
     * <p>
     * This method creates a new window with a title and a message
     * and displays the window when function in called
     * @param title The string that becomes the title of the window
     * @param message The message that becomes the error message that the popup displals
    */

    public void showPopup(String title, String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(200);
        window.setMinHeight(200);

        Label label = new Label();
        label.setText(message);
        Button close = new Button("Close");
        close.setOnAction(e -> window.close());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, close);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.show();
    }
    /**
     * Returns center of rectangle at given row and column
     * <p>
     * Returns a Point object containing the center of rectangle
     * @param row The row where rectangle is located
     * @param column The column where rectangle is located
     * @return Point object (row, column) containing center of rectange
    */

    public Point<Double> getRectangleCenter(int row, int column) {
        double X = horizontal * (row + 0.5);
        double Y = vertical * (column + 0.5);
        return new Point<Double>(X, Y);
    }
    /**
     * Deletes file
     * <p>
     * Deletes the file whose name is entered as argument
     * @param fileName The name of the file to be deleted
    */

    public void deleteFile(String fileName) {
        try {
            new File(fileName).delete();
        } catch(Exception e) {
        }
    }
    /**
     * The default start function of application
     * <p>
     * Starts the appliccation
     * @param stage The stage where the application has to be started from
    */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setResizable(false);
        menu();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
