package it.polimi.ingsw.ui;

import it.polimi.ingsw.model.*;
import it.polimi.ingsw.network.client.ClientHandler;
import it.polimi.ingsw.ui.controller.IntroController;
import it.polimi.ingsw.ui.controller.MainController;
import it.polimi.ingsw.ui.controller.WindowPatternController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class GUI extends Application implements UI {
    public static final double CHOOSER_TILE_SIZE = 70.0;
    public static final double BASE_TILE_SIZE = 30.0;
    public static final double TILE_RELATIVE_SIZE = CHOOSER_TILE_SIZE / 1920.0;
    public static final double BOARD_RELATIVE_HEIGHT = 600.0 / 1080.0;
    public static final double BOARD_RELATIVE_WIDTH = 350.0 / 1920.0;
    public static final double ROUND_CORNER_RADIUS = 10.0 / CHOOSER_TILE_SIZE * BASE_TILE_SIZE;
    public static final double DIE_RELATIVE_SPACER = 5.0 / CHOOSER_TILE_SIZE;
    public static final double TOKEN_RELATIVE_SIZE = 7.0 / CHOOSER_TILE_SIZE;
    public static final double DIE_RESCALE_FACTOR = 0.5;
    private Stage stage;
    private WindowPattern selected;
    private IntroController introController;
    private MainController mainController;
    private ClientHandler handler;
    private ProxyModel model;
    private ChangeListener<Number> listener1;
    private ChangeListener<Number> listener2;
    private ChangeListener<Number> sizeListener;
    private double initialWidth;
    private double initialHeight;
    private Die selectedDie;
    private Timeline timer;
    private IntegerProperty secondsRemaining;

    /**
     * Get the current turn remaining seconds
     *
     * @return an int corresponding to the amount of seconds before the turn ends
     */
    public int getSecondsRemaining() {
        return secondsRemaining.get();
    }

    public IntegerProperty secondsRemainingProperty() {
        return secondsRemaining;
    }

    /**
     * Proxy method to show a message in the GUI
     *
     * @param s
     * @see MainController#showMessage(String)
     */
    private void showMessage(String s) {
        Platform.runLater(() -> mainController.showMessage(s));
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("views/intro.fxml"));
        try {
            initialHeight = 0;
            initialWidth = 0;
            secondsRemaining = new SimpleIntegerProperty();
            stage = primaryStage;
            handler = new ClientHandler(this);
            primaryStage.setTitle("Sagrada - Connection");
            Parent root = loader.load();
            introController = loader.getController();
            introController.setSelfStage(primaryStage);
            introController.setGui(this);
            introController.initListeners();
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Couldn't load GUI");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }

    @Override
    public void failedLogin() {
        Platform.runLater(() -> {
            introController.failedLogin();
        });
    }

    @Override
    public void showLogin() {
        // Intentionally left blank
    }

    @Override
    public void showPatternCardsChooser(PatternCard one, PatternCard two) {
        Platform.runLater(() -> {
            stage.setTitle("Sagrada - Please choose your pattern card");
            Button confirm = new Button("Confirm");
            confirm.setDisable(true);
            VBox main = new VBox();
            main.setAlignment(Pos.CENTER);
            HBox box = new HBox();
            box.setSpacing(10);
            WindowPattern[] patterns = {one.getFront(), one.getBack(), two.getFront(), two.getBack()};
            Parent[] containers = new Parent[patterns.length];
            WindowPatternController[] controllers = new WindowPatternController[patterns.length];
            int i = 0;
            for (WindowPattern p : patterns) {
                FXMLLoader patternLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/windowpattern.fxml"));
                try {
                    containers[i] = patternLoader.load();
                    containers[i].getStylesheets().add(getClass().getClassLoader().getResource("windowpattern.css").toExternalForm());
                    controllers[i] = patternLoader.getController();
                    controllers[i].setWindowPattern(p, CHOOSER_TILE_SIZE);
                    box.getChildren().add(containers[i]);
                    int finalI = i;
                    box.getChildren().get(i).setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            confirm.setDisable(false);
                            for (Parent c : containers) {
                                c.getStyleClass().remove("chosen");
                            }
                            ((VBox) event.getSource()).getStyleClass().add("chosen");
                            selected = patterns[finalI];
                        }
                    });
                    i++;
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Couldn't load pattern card GUI");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    Platform.exit();
                }
            }
            main.getChildren().add(box);
            main.getChildren().add(confirm);
            confirm.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (selected != null) {
                        confirm.setDisable(true);
                        handler.setChosenPatternCard(selected);

                    }
                }
            });
            VBox.setMargin(confirm, new Insets(20, 0, 20, 0));
            VBox.setMargin(main, new Insets(20));
            stage.setScene(new Scene(main));
            stage.sizeToScene();
            stage.centerOnScreen();
        });
    }

    @Override
    public void showLoggedInUsers() {
        Platform.runLater(() -> {
            stage.setTitle("Sagrada - Welcome to the game room");
            model = handler.getModel();
            introController.showLoggedInUsers();
        });
    }

    @Override
    public ClientHandler getClientHandler() {
        return handler;
    }

    @Override
    public ProxyModel getModel() {
        return model;
    }

    @Override
    public void setModelAfterReconnecting(ProxyModel model) {
        Platform.runLater(() -> {
            this.model = model;
        });
    }

    @Override
    public void initUI() {
        launch();
    }

    @Override
    public void startGame() {
        // Intentionally left blank
    }

    @Override
    public void update() {
        Platform.runLater(() -> {
            mainController.update();
        });
    }

    @Override
    public void myTurnStarted() {
        Platform.runLater(() -> {
            mainController.enableActions(true);
        });
    }

    @Override
    public void myTurnEnded() {
        Platform.runLater(() -> {
            mainController.enableActions(false);
            mainController.cleanUI();
        });
    }

    @Override
    public void nextMove() {
        Platform.runLater(() -> {
            showMessage("You can do something else!");
            update();
        });
    }

    @Override
    public void toolAvailable(boolean isAvailable) {
        Platform.runLater(() -> {
            if (!isAvailable) {
                showMessage("You can't use that now!");
            } else {
                showMessage("You can use that Tool Card.  Go on!");
            }
        });
    }

    @Override
    public void playerDisconnected(Player p) {
        Platform.runLater(() -> {
            showMessage(String.format("%s has disconnected!", p.getName()));
        });
    }

    @Override
    public void initBoard() {
        Platform.runLater(() -> {
            stage.setTitle("Sagrada");
            FXMLLoader boardLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/main.fxml"));
            try {
                Parent root = boardLoader.load();
                root.getStylesheets().add(getClass().getClassLoader().getResource("die.css").toExternalForm());
                root.getStylesheets().add(getClass().getClassLoader().getResource("board.css").toExternalForm());
                mainController = boardLoader.getController();
                mainController.setGUI(this);
                mainController.initBoards();
                // Init listeners
                model.getDraftPool().addListener((ListChangeListener<Die>) c -> {
                    while (c.next()) {
                        update();
                    }
                });
                listener1 = (observable, oldValue, newValue) -> {
                    //showMessage(String.format("Round %d ended. round %d is starting!", oldValue.intValue(), newValue.intValue()));
                };
                listener2 = (observable, oldValue, newValue) -> {
                    startTimer();
                    update();
                    if (model.isMyTurn()) {
                        showMessage(String.format("It's turn %d of round %d. You're playing!", model.getCurrentTurn() + 1, model.getCurrentRound()));
                    } else {
                        showMessage(String.format("It's turn %d of round %d. %s is playing!", model.getCurrentTurn() + 1, model.getCurrentRound(), model.getCurrentPlayer().getName()));
                    }
                };
                model.currentRoundProperty().addListener(listener1);
                model.currentTurnProperty().addListener(listener2);
                stage.setScene(new Scene(root));
                stage.sizeToScene();
                stage.centerOnScreen();
                sizeListener = new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        double initialArea = initialWidth * initialHeight;
                        if (initialArea == 0.0) {
                            return;
                        }
                        if (stage.getWidth() * stage.getHeight() < initialArea) {
                            stage.setWidth(initialWidth);
                            stage.setHeight(initialHeight);
                        }
                        mainController.resizeAll();
                    }
                };
                stage.widthProperty().addListener(sizeListener);
                stage.heightProperty().addListener(sizeListener);
                mainController.repositionBoards();
                mainController.update();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Error while loading the main game GUI");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                Platform.exit();
            }
        });
    }

    /**
     * This method stops the current timer and resets it to the initial value supplied from server via the {@link ProxyModel}.
     * The real timer is actually in the server, this is just a mere representation for the client and does not interfere
     * with the game itself
     */
    private void startTimer() {
        Platform.runLater(() -> {
            if (timer != null) {
                timer.stop();
            }
            secondsRemaining.set(model.getTimeout());
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> secondsRemaining.set(getSecondsRemaining() - 1)));
            timer.setCycleCount(model.getTimeout());
            timer.play();
        });
    }

    @Override
    public boolean isGUI() {
        return true;
    }

    @Override
    public void wrongMove() {
        Platform.runLater(() -> {
            showMessage("Your move was incorrect!");
        });
    }

    @Override
    public void chooseDieFromWindowPattern() {
        Platform.runLater(() -> {
            mainController.toolChooseDieFromWindowPattern();
        });
    }

    @Override
    public void chooseDieFromDraftPool() {
        Platform.runLater(() -> {
            mainController.toolChooseDieFromDraftPool();
        });
    }

    @Override
    public void chooseDieFromRoundTrack() {
        Platform.runLater(() -> {
            mainController.toolChooseDieFromRoundTrack();
        });
    }

    @Override
    public void chooseIfDecrease() {
        Platform.runLater(() -> {
            mainController.toolChooseIfDecrease();
        });
    }

    @Override
    public void chooseIfPlaceDie(int number) {
        Platform.runLater(() -> {
            mainController.toolChooseIfPlaceDie();
        });

    }

    @Override
    public void chooseToMoveOneDie() {
        Platform.runLater(() -> {
            mainController.toolChooseToMoveOneDie();
        });

    }

    @Override
    public void setValue(SagradaColor color) {
        Platform.runLater(() -> {
            mainController.toolSetValue(color);
        });
    }

    @Override
    public void setNewCoordinates() {
        Platform.runLater(() -> {
            mainController.toolSetNewCoordinates();
        });
    }

    @Override
    public void chooseTwoDice() {
        Platform.runLater(() -> {
            mainController.toolChooseTwoDice();
        });
    }

    @Override
    public void chooseTwoCoordinates() {
        Platform.runLater(() -> {
            mainController.toolChooseTwoCoordinates();
        });
    }

    @Override
    public void toolEnded(boolean success) {
        Platform.runLater(() -> {
            if (success) {
                showMessage("The tool card succeeded!");
            } else {
                showMessage("The tool card failed.");
            }
        });
    }

    /**
     * Retrieves the primary screen height
     *
     * @return the primary screen height, as a double
     */
    public double getHeight() {
        return Screen.getPrimary().getVisualBounds().getHeight();
    }

    /**
     * Retrieves the primary screen width
     *
     * @return the primary screen width, as a double
     */
    public double getWidth() {
        return Screen.getPrimary().getVisualBounds().getWidth();
    }

    /**
     * Method to ask the server to pass the turn: it is just a connection method between the controller and the network
     * layer
     */
    public void endTurn() {
        Platform.runLater(() -> {
            handler.passTurn();
        });
    }

    /**
     * Method to set the currently selected die before sending it to the network layer asking for a move
     *
     * @param i the index of the chosen die in the draft pool
     */
    public void selectDie(int i) {
        Platform.runLater(() -> {
            selectedDie = model.getDraftPool().get(i);
        });
    }

    /**
     * Method to ask the server to place a die on the player's window, via the network layer
     *
     * @param i the row of the die's destination
     * @param j the column of the row's destination
     */
    public void tryDiePlacement(int i, int j) {
        Platform.runLater(() -> {
            if (selectedDie != null) {
                handler.handlePlacement(selectedDie, i, j);
                selectedDie = null;
            }
        });
    }

    /**
     * Method to save the initial size of the game window, so that we can ensure the window cannot be resized to an
     * inferior size
     */
    public void setInitialSize() {
        Platform.runLater(() -> {
            stage.sizeToScene();
            initialWidth = stage.getWidth();
            initialHeight = stage.getHeight();
        });
    }
}
