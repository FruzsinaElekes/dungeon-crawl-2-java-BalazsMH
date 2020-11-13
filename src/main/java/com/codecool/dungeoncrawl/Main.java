package com.codecool.dungeoncrawl;

import com.codecool.dungeoncrawl.logic.*;
import com.codecool.dungeoncrawl.logic.actors.pokemon.Pokemon;
import com.codecool.dungeoncrawl.logic.items.Inventory;
import com.codecool.dungeoncrawl.logic.items.Key;
import com.codecool.dungeoncrawl.logic.MapGenerator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Main extends Application {
    private static Stage pStage;
    boolean m = MapGenerator.generateMap(1);
    GameMap map = MapLoader.loadMap("Level1");
    List<List<Integer>> mapWallsLevel1 = MapLoader.getWalls();
    boolean mapReady = MapGenerator.generateMap(2);
    //TODO: figure out why it doesn't allow simply calling Mapgenerator with a void return value
    GameMap map2 = MapLoader.loadMap("Level2");
    List<List<Integer>> mapWallsLevel2 = MapLoader.getWalls();

    Pokemon bulbasaur = map.getPokemonList().get(2);

    MapChanger mapChanger = new MapChanger(map, map2);

    Canvas canvas = new Canvas(
            map.getWidth() * Tiles.DEFAULT_TILE_WIDTH,
            map.getHeight() * Tiles.DEFAULT_TILE_WIDTH);
    GraphicsContext context = canvas.getGraphicsContext2D();


    Label nameLabel = new Label();
    Label currentInfo = new Label();
    Label currentLevel = new Label();
    StringBuilder text = new StringBuilder();
    String[] developers = new String[]{"Fruzsi", "Dani", "Peti", "Balázs"};


    public Inventory inventory = new Inventory();


    public static void main(String[] args) {
        launch(args);
    }

    private Scene mainMenu(Stage primaryStage, Scene game) {
        VBox mainPane = new VBox();
        mainPane.setPrefSize(1287/1.5,797/1.5);
        Background background = new Background(new BackgroundImage(new Image("/main_menu.png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO,
                                                              BackgroundSize.AUTO,
                                                                false, false, true, true)));
        mainPane.setBackground(background);
        TextField nameInput = new TextField();
        nameInput.setFont(Font.loadFont("file:Pokemon_Classic.ttf", 14));
        nameInput.setMaxSize(220,220);
        nameInput.setPromptText("Enter your name ");
        Button submitButton = new Button("Play!");
        submitButton.setFont(Font.loadFont("file:Pokemon_Classic.ttf", 14));

        mainPane.getChildren().addAll(nameInput, submitButton);
        mainPane.setAlignment(Pos.CENTER);
        Scene mainMenu = new Scene(mainPane);
        mainPane.requestFocus();

        submitButton.setOnMouseClicked((event)-> this.onSubmitPressed(primaryStage, game, nameInput));
        return mainMenu;
    }

    private Scene game() {
        setLabels();

        VBox rightPane = createRightPane();
        VBox levelBox = createLevelBox();
        VBox bottom = createBottomBox();

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(canvas);
        borderPane.setBottom(bottom);
        borderPane.setRight(rightPane);
        borderPane.setTop(levelBox);
        //TODO: eliminate unnecessary space between the top of the canvas and the borderpane center top

        Scene scene = new Scene(borderPane);
        scene.setOnKeyPressed(this::onKeyPressed);
        return scene;
    }

    @Override
    public void start(Stage primaryStage) {
        pStage = primaryStage;
        primaryStage.setTitle("JavaMon");
        primaryStage.getIcons().add(new Image("file:logo.png"));

        //Create Game
        Scene game = game();

        //Main menu
        Scene mainMenu = mainMenu(primaryStage, game);


        primaryStage.setScene(mainMenu);
        refresh();
        primaryStage.show();
    }

    public static Stage getpStage() {
        return pStage;
    }

    private void onSubmitPressed(Stage primaryStage, Scene gameScene, TextField nameInput) {
        String enteredName = nameInput.getText();
        map.getPlayer().setUserName(enteredName);
        if (Arrays.asList(developers).contains(enteredName)) {
            map.getPlayer().setSuperUser(true);
        }
        nameLabel.setText(map.getPlayer().getUserName());
        primaryStage.setScene(gameScene);
    }

    private void onKeyPressed(KeyEvent keyEvent) {
        text.setLength(0);
        KeyCode keyPressed = keyEvent.getCode();
        switch (keyPressed) {
            case UP:
                map.getPlayer().setFacing("up");
                map.getPlayer().move(0, -1);
                refresh();
                break;
            case DOWN:
                map.getPlayer().setFacing("down");
                map.getPlayer().move(0, 1);
                refresh();
                break;
            case LEFT:
                map.getPlayer().setFacing("left");
                map.getPlayer().move(-1, 0);
                refresh();
                break;
            case RIGHT:
                map.getPlayer().setFacing("right");
                map.getPlayer().move(1,0);
                refresh();
                break;
            case R:
                map.getRocketGrunt().releasePokemon(map);
                refresh();
                break;
            case T:
                map.getPlayer().throwPokeBall(inventory, text, getPokemonInRange(), map);
                refresh();
                checkIfGameEnds();
                break;
            case E:
                if (map.getPlayer().getCell().getItem() instanceof Key){
                    inventory.addKey(map.getPlayer().getCell());
                    map.getPlayer().getCell().setItem(null);
                } else {
                    map.getPlayer().pickupItem(inventory, text);
                }
                refresh();
                break;
            case O:
                if (inventory.hasKey() && map.getPlayer().getCell().getType() == CellType.DOOR){
                    map.getPlayer().getCell().getDoor().setOpen();
                    map = mapChanger.changeMap(map);
                    refresh();
                }
                break;
            case A:
                inventory.changeActivePokemon();
                break;
            case F:
                map.getPlayer().fightPokemon(inventory, text, getPokemonInRange(), map);
                refresh();
                checkIfGameEnds();
                break;
            case H:
                inventory.heal();
                refresh();
                break;
        }
    }

    private void refresh() {
        context.setFill(new ImagePattern(Tiles.getFloorTile(), 0, 0, 960, 960, false));
        context.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        moveAllPokemon();
        //moveBulbasaur();
        refreshInfoWindow();
        refreshLevelInfo();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Cell cell = map.getCell(x, y);
                if (cell.getActor() != null) {
                    Tiles.drawTile(context, cell.getActor(), x, y);
                } else if (cell.getItem() != null){
                    Tiles.drawTile(context, cell.getItem(), x, y);
                } else if(cell.getPokemon() != null){
                    Tiles.drawTile(context, cell.getPokemon(), x, y);
                } else if(cell.getDoor() != null) {
                    Tiles.drawTile(context, cell.getDoor(), x, y);
                } else {
                    Tiles.drawTile(context, cell, x, y);
                }
            }
        }
    }

    private void moveAllPokemon() {
        int level = mapChanger.getLevel();
        List<List<Integer>> mapWalls = (level == 1) ? mapWallsLevel1 : mapWallsLevel2;
        List<Pokemon> pokemonList= map.getPokemonList();
        List playerCoordinates = map.returnPlayerCoordinates();
        for (Pokemon pokemon : pokemonList) {
            int x = pokemon.getX();
            int y = pokemon.getY();
            pokemon.attackMove(mapWalls, playerCoordinates, x, y);
        }
    }


    public void checkIfGameEnds(){
        if (inventory.getActivePokemon() == null){
            gameEndWindow(EndCondition.LOSE);
        } else if (map2.getRocketGrunt().getRocketPokemonList().size() == 0 && map2.getRocketGrunt().getRocketPokemonOnBoard().size() == 0){
            gameEndWindow(EndCondition.WIN);
        }
    }

    protected void gameEndWindow(EndCondition endCondition) {
        Stage endPopup = new Stage();
        endPopup.initModality(Modality.WINDOW_MODAL);
        endPopup.initOwner(getpStage());
        VBox endContent = new VBox();
        Scene endScene = new Scene(endContent);
        Text winText = new Text("Congratulations! You won!");
        Text loseText = new Text("You lost. Try again!");
        Text displayedText = endCondition == EndCondition.WIN? winText : loseText;
        Button closeWindow = new Button("Quit game");
        closeWindow.setFont(Font.loadFont("file:Pokemon_Classic.ttf", 14));
        displayedText.setFont(Font.loadFont("file:Pokemon_Classic.ttf", 22));
        endContent.setAlignment(Pos.CENTER);

        closeWindow.setOnAction((event)-> {
            try {
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        endContent.getChildren().addAll(displayedText, closeWindow);
        endContent.setPrefSize(800.0/2,761.0/2);
        Background background = new Background(new BackgroundImage(
                new Image(endCondition == EndCondition.LOSE? "/lose.png": "/win.png"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(BackgroundSize.AUTO,
                BackgroundSize.AUTO,
                false, false, true, true)));

        endPopup.setScene(endScene);
        endContent.setBackground(background);
        endPopup.show();
    }



    private void refreshLevelInfo() {
        currentLevel.setText(map.getLevel());
    }

    private void refreshInfoWindow() {
        Cell standingOn = map.getPlayer().getCell();
        if (standingOn.getDoor() != null){
            text.append("\nOpen door by 'O'\n");
        } else if (standingOn.getItem() != null){
            text.append(String.format("\nPick up %s by 'E'!\n", standingOn.getItem().getTileName()));
        }
        if (getPokemonInRange().isPresent()) {
            text.append("\n\nPokemon in range:\n");
            getPokemonInRange().get().forEach(p -> text.append("\n" + p.toString()));
        }
        currentInfo.setText(text.toString());
    }

    private Optional<List<Pokemon>> getPokemonInRange() {
        Optional<List<Pokemon>> toReturn = Optional.empty();
        List<Pokemon> pokemonInRange = new ArrayList<Pokemon>();
        int playerX = map.getPlayer().getCell().getX();
        int playerY = map.getPlayer().getCell().getY();
        List<Pokemon> pokemonList= map.getPokemonList();
        pokemonList.forEach(p -> {
            if (Math.abs(p.getCell().getX() - playerX) + Math.abs(p.getCell().getY() - playerY) <= 3){
                pokemonInRange.add(p);
                currentInfo.setText(p.toString());
            }
        });
        if (pokemonInRange.size() > 0) toReturn = Optional.of(pokemonInRange);
        return toReturn;
    }


    private VBox createRightPane() {
        GridPane ui = new GridPane();
        ui.setPrefWidth(300);
        ui.setPadding(new Insets(10));
        nameLabel.setText(map.getPlayer().getUserName());
        ui.add(nameLabel, 0, 0);

        VBox infoBox = createInfoBox();
        VBox rightPane = new VBox(ui, infoBox);
        rightPane.setSpacing(20.00);
        return rightPane;
    }

    private VBox createInfoBox(){

        currentInfo.setWrapText(true);
        currentInfo.setPrefWidth(300);
        TextFlow textFlow = new TextFlow();
        textFlow.setPrefWidth(200);
        textFlow.getChildren().add(currentInfo);

        Image infoImage = new Image(String.valueOf(ClassLoader.getSystemResource("info.png")));
        Label infoTitle = new Label();
        infoTitle.setGraphic(new ImageView(infoImage));

        VBox infoBox = new VBox(infoTitle, textFlow);
        infoBox.setStyle("-fx-padding: 10px;");
        infoBox.setPrefHeight(600);
        infoBox.setPrefWidth(200);

        infoBox.setSpacing(20);
        return infoBox;
    }

    private VBox createBottomBox() {
        Text movementInfo = new Text("Hint:\nUse the arrow keys to move the character on the map\n" +
                "Press 'A' to change active pokemon and 'H' to heal it\n" +
                "Press 'F' to fight and 'T' to catch pokemon\n" +
                "Pick things up by 'E'\n" +
                "Engage Rocket Grunt by 'R'\n");
        movementInfo.setFont(Font.loadFont("file:Pokemon_Classic.ttf", 12));
        movementInfo.setTextAlignment(TextAlignment.CENTER);
        movementInfo.setLineSpacing(1.5);

        VBox bottom = new VBox(movementInfo);
        bottom.setAlignment(Pos.CENTER);
        return bottom;
    }

    private VBox createLevelBox() {
        VBox levelBox = new VBox(currentLevel);
        levelBox.setAlignment(Pos.CENTER);
        levelBox.setPadding(new Insets(5));
        levelBox.setMaxHeight(10);
        return levelBox;
    }

    private void setLabels() {
        currentLevel.setText(map.getLevel());
        currentLevel.setFont(Font.loadFont("file:Pokemon_Classic.ttf", 18));
        nameLabel.setFont(Font.loadFont("file:Pokemon_Classic.ttf", 18));
        currentInfo.setFont(Font.loadFont("file:Pokemon_Classic.ttf",14));
        currentInfo.setWrapText(true);
    }
}
