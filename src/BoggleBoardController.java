import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.List;

public class BoggleBoardController {

    Integer colIndex;
    Integer rowIndex;
    Integer points = 0;
    Integer[][] availableLetters = new Integer[4][4];
    List<Node> clickedLetters = new ArrayList<>();
    Pair<Integer, Integer> lastClicked = new Pair<>(null, null);
    ArrayList<String> usedWords = new ArrayList<>();
    ArrayList<String> dict = new ArrayList<>();
    String currentWord = "";
    boolean newDict = true;
    //boolean saveWord is to prevent printing "word cleared" if we're not
    // just clearing a word because the clear function is reused by other code
    boolean saveWord = false;

    //AI state variables
    Integer maxPoints = 0;
    ArrayList<String> AIusedWords = new ArrayList<>();

    //Settings
    boolean typeWords = false;
    boolean changeSetting = false;
    List<String> dice = Settings.diceNew;
    InputStream dictFile = getClass().getResourceAsStream("/dictionaries/bogwords.txt");
    String colorAIWords = Settings.woodColorAIWords;
    String colorClicked = Settings.woodColorClicked;
    String colorCanClick = Settings.woodColorCanClick;
    String colorCantClick = Settings.woodColorCantClick;
    String colorEmptySquare = Settings.woodColorEmptySquare;
    String colorNotification = Settings.woodNotifications;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private BorderPane borderPane;

    @FXML
    private GridPane gridPane;

    @FXML
    private Label pointsLabel;

    @FXML
    private Label notificationLabel;

    @FXML
    private VBox wordHistoryVBox;

    @FXML
    private Rectangle background;

    @FXML
    private ToggleGroup radioDictionary;

    @FXML
    private RadioMenuItem dictFrench;

    @FXML
    private RadioMenuItem dictCustom;

    @FXML
    private RadioMenuItem dictGerman;

    @FXML
    private RadioMenuItem dictEnglish;

    @FXML
    private RadioMenuItem dictEnable;

    @FXML
    private RadioMenuItem dictScrabble;

    @FXML
    private RadioMenuItem dictSpanish;

    @FXML
    private RadioMenuItem dictItalian;

    @FXML
    private RadioMenuItem dictDutch;

    @FXML
    public ToggleGroup radioTheme;

    @FXML
    public RadioMenuItem themeWood;

    @FXML
    public RadioMenuItem themeHacker;

    @FXML
    private ToggleGroup radioDice;

    @FXML
    private RadioMenuItem radioNew1;

    @FXML
    private RadioMenuItem radioClassic1;

    @FXML
    private CheckMenuItem radioTypeWords;

    @FXML
    private TextField textWordGuess;


    //this is the AI function
    void findWords(String[][] board) {
        boolean[][] visited = new boolean[4][4];
        String newWord = "";
        for (int i=0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                findWordsUtil(board, visited, i, j, newWord);
            }
        }
//        maxPoints += points;
    }

    void findWordsUtil(String[][] board, boolean[][] visited, int i, int j, String newWord) {
        boolean isPossible = false;
        visited[i][j] = true;
        newWord = newWord + board[i][j];

        //if word is in dictionary, print it
        for (String line: dict) {
            if (newWord.equalsIgnoreCase(line)) {
                boolean wordFound = false;
                for (String usedWord: AIusedWords) {
                    if (newWord.equalsIgnoreCase(usedWord)) {
                        wordFound = true;
                    }
                }
                for (String usedWord: usedWords) {
                    if (newWord.equalsIgnoreCase(usedWord)) {
                        wordFound = true;
                    }
                }
                if (!wordFound) {
                    AIusedWords.add(newWord);
                }
            }
            else if (line.length() >= newWord.length()){
                String testLine = line.substring(0, newWord.length());
                if (newWord.equalsIgnoreCase(testLine)) {
                    isPossible = true;
                }
            }
        }

        if (!isPossible) {
            newWord = newWord.substring(0, newWord.length()-1);
            visited[i][j] = false;
            return;
        }

        //checks all surrounding letters
        for (int row = i-1; row<=i+1 && row<4; row++) {
            for (int col = j-1; col<=j+1 && col<4; col++) {
                if (row>=0 && col>=0 && !visited[row][col]) {
                    findWordsUtil(board, visited, row, col, newWord);
                }
            }
        }

        //Intellij says this isn't used, but it's used by the for loop in "findWords()"
        newWord = newWord.substring(0, newWord.length()-1);
        visited[i][j] = false;
    }

    void printSolver() {
        for (String newWord: AIusedWords) {
            boolean wordFound = false;
            for (String usedWord : usedWords) {
                if (newWord.equalsIgnoreCase(usedWord)) {
                    wordFound = true;
                }
            }
            if (!wordFound) {
                HBox wordHistory = new HBox();
                Label wordLabel = new Label(newWord);
                Label wordPointsLabel = new Label();
                wordHistory.setMinSize(181, Region.USE_COMPUTED_SIZE);
                wordHistory.setPrefSize(194, 20);
                wordHistory.setMaxSize(Region.USE_PREF_SIZE, Region.USE_COMPUTED_SIZE);
                wordLabel.setTranslateX(5);
                wordLabel.setMinSize(155, Region.USE_COMPUTED_SIZE);
                wordLabel.setPrefSize(155, 20);
                wordLabel.setMaxSize(155, Region.USE_COMPUTED_SIZE);
                wordLabel.setStyle(colorAIWords);
                wordPointsLabel.setMinSize(20, Region.USE_COMPUTED_SIZE);
                wordPointsLabel.setPrefSize(32, 20);
                wordPointsLabel.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                wordPointsLabel.setStyle(colorAIWords);

                switch (newWord.length()) {
                    case 0:
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        maxPoints++;
                        wordPointsLabel.setText("+1");
                        break;
                    case 5:
                        maxPoints += 2;
                        wordPointsLabel.setText("+2");
                        break;
                    case 6:
                        maxPoints += 3;
                        wordPointsLabel.setText("+3");
                        break;
                    case 7:
                        maxPoints += 5;
                        wordPointsLabel.setText("+5");
                        break;
                    default:
                        maxPoints += 11;
                        wordPointsLabel.setText("+11");
                        break;
                }

                wordHistory.getChildren().addAll(wordLabel, wordPointsLabel);
                wordHistoryVBox.getChildren().add(0, wordHistory);
            }
        }
    }

    void handleFindWords() {
        String[][] boggleBoard = new String[4][4];
        int i = 0;
        int j = 0;
        for (Node node: gridPane.getChildren()) {
            Label letter = (Label)node;
            boggleBoard[i][j] = letter.getText();
            if (i < 4) {
                if (j < 3) {
                    j++;
                } else {
                    j = 0;
                    i++;
                }
            }
        }
        findWords(boggleBoard);
    }

    @FXML
    void handleAI(ActionEvent event) {
        //this is just the button, it will run the AI function initially
        notificationLabel.setText("Solving, please wait. This may take a moment.");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Platform.runLater(() -> {
            if (!typeWords) {
                handleFindWords();
            }
            printSolver();
            maxPoints += points;
            notificationLabel.setText("All words found. Maximum possible score: " + maxPoints + "     Your score: " + points);
        });
    }

    @FXML
    void handleChangeTheme(ActionEvent event) {
        RadioMenuItem button = (RadioMenuItem)radioTheme.getSelectedToggle();
        switch (button.getId()) {
            case "themeWood":
                colorAIWords = Settings.woodColorAIWords;
                colorClicked = Settings.woodColorClicked;
                colorCanClick = Settings.woodColorCanClick;
                colorCantClick = Settings.woodColorCantClick;
                colorEmptySquare = Settings.woodColorEmptySquare;
                colorNotification = Settings.woodNotifications;
                background.setEffect(new ImageInput(new Image("/wood.jpg")));
                break;
            case "themeHacker":
                colorAIWords = Settings.hackerColorAIWords;
                colorClicked = Settings.hackerColorClicked;
                colorCanClick = Settings.hackerColorCanClick;
                colorCantClick = Settings.hackerColorCantClick;
                colorEmptySquare = Settings.hackerColorEmptySquare;
                colorNotification = Settings.hackerNotifications;
                background.setEffect(null);
                break;
        }
        handleClearWord(event);
        notificationLabel.setStyle(colorNotification);
    }

    @FXML
    void handleChangeDice(ActionEvent event) {
        RadioMenuItem button = (RadioMenuItem)radioDice.getSelectedToggle();
        switch (button.getId()) {
            case "diceNew1":
                dice = Settings.diceNew;
                break;
            case "diceClassic1":
                dice = Settings.diceClassic;
                break;
        }
        handleNewGame(event);
    }

    void addToDict() {
//        try(BufferedReader br = new BufferedReader(new FileReader(dictFile))) {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(dictFile))) {
            for(String line; (line = br.readLine()) != null; ) {
                dict.add(line);
            }
        }
        catch (IOException ex){}
        newDict = false;
    }

    private boolean isValidLetter() {
        //is it possible to clean this up any more?
        if (availableLetters[rowIndex][colIndex] == 1) {
            if (lastClicked.getKey() != null && lastClicked.getValue() != null) {
                if (!(rowIndex == lastClicked.getKey()
                        && colIndex == lastClicked.getValue())
                        && (((rowIndex == (lastClicked.getKey() - 1))
                            || (rowIndex == lastClicked.getKey())
                            || (rowIndex == (lastClicked.getKey() + 1)))
                          && ((colIndex == (lastClicked.getValue() - 1))
                              || (colIndex == lastClicked.getValue())
                              || (colIndex == (lastClicked.getValue() + 1))))) {
                    return true;
                }
            }
            else {
                return true;
            }
        }
        return false;
    }

    private void mouseEvent(MouseEvent e, Node source) {
        colIndex = GridPane.getColumnIndex(source);
        rowIndex = GridPane.getRowIndex(source);
        if(colIndex == null) {
            colIndex = 0;
        }
        if(rowIndex == null) {
            rowIndex = 0;
        }
    }

    @FXML
    void handleOnMouseClicked(MouseEvent event) {
        Node source = (Node)event.getSource();
        mouseEvent(event, source);
        if (isValidLetter()) {
            clickedLetters.add(source);
            availableLetters[rowIndex][colIndex] = 0;
            lastClicked = new Pair<>(rowIndex, colIndex);
            //blue clicked color
            source.setStyle(colorClicked);
            currentWord = "";
            for (Node node: clickedLetters) {
                Label label = (Label)node;
                currentWord += label.getText();
            }
            notificationLabel.setText('"' + currentWord + '"');
        }
        else {
            notificationLabel.setText("You can't click this square.");
        }
    }

    @FXML
    void handleOnMouseEnter(MouseEvent event) {
        Node source = (Node) event.getSource();
        mouseEvent(event, source);
        if (isValidLetter()) {
            source.setStyle(colorCanClick);
        }
        else {
            for (Node node: clickedLetters) {
                if (source == node) {
                    return;
                }
            }
            source.setStyle(colorCantClick);
        }
    }

    @FXML
    void handleOnMouseExit(MouseEvent event) {
        Node source = (Node) event.getSource();
        mouseEvent(event, source);
        if (availableLetters[rowIndex][colIndex] == 1) {
            source.setStyle(colorEmptySquare);
        }
    }

    @FXML
    void handleClearWord(ActionEvent event) {
        for(int k = 0; k < 4; k++) {
            for(int j = 0; j < 4; j++) {
                availableLetters[k][j] = 1;
            }
        }
        clickedLetters.clear();
        lastClicked = new Pair<>(null, null);
        for (Node node: gridPane.getChildren()) {
            node.setStyle(colorEmptySquare);
        }
        if (!(saveWord)) {
            notificationLabel.setText("Word cleared.");
        }
        saveWord = false;
        currentWord = "";
    }

    //save word button
    @FXML
    void handleSaveWord(ActionEvent event) {
//        String word = "";
//        for (Node node: clickedLetters) {
//            Label label = (Label)node;
//            word += label.getText();
//        }
        boolean wordFound = false;

        ArrayList<String> testWords = dict;
        if(typeWords) {
            testWords = AIusedWords;
        }
        for (String line: testWords) {
            if (currentWord.equalsIgnoreCase(line)) {
                for (String newWord: usedWords) {
                    if (currentWord.equalsIgnoreCase(newWord)) {
                        wordFound = true;
                        notificationLabel.setText('"' + currentWord + '"' + " has already been used.");
                        //TODO
                        //flash all letters red
                    }
                }
                if (!wordFound) {
                    HBox wordHistory = new HBox();
                    wordHistory.setMinSize(181, Region.USE_COMPUTED_SIZE);
                    wordHistory.setPrefSize(194,20);
                    wordHistory.setMaxSize(Region.USE_PREF_SIZE,Region.USE_COMPUTED_SIZE);
                    Label wordLabel = new Label(currentWord);
                    wordLabel.setTranslateX(5);
                    wordLabel.setMinSize(155,Region.USE_COMPUTED_SIZE);
                    wordLabel.setPrefSize(155,20);
                    wordLabel.setMaxSize(155,Region.USE_COMPUTED_SIZE);
                    Label wordPointsLabel = new Label();
                    wordPointsLabel.setMinSize(20,Region.USE_COMPUTED_SIZE);
                    wordPointsLabel.setPrefSize(32,20);
                    wordPointsLabel.setMaxSize(Region.USE_COMPUTED_SIZE,Region.USE_COMPUTED_SIZE);
                    switch (currentWord.length()) {
                        case 0:
                            break;
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                            points += 1;
                            wordPointsLabel.setText("+1");
                            break;
                        case 5:
                            points += 2;
                            wordPointsLabel.setText("+2");
                            break;
                        case 6:
                            points += 3;
                            wordPointsLabel.setText("+3");
                            break;
                        case 7:
                            points += 5;
                            wordPointsLabel.setText("+5");
                            break;
                        default:
                            points += 11;
                            wordPointsLabel.setText("+11");
                            break;
                    }
                    usedWords.add(currentWord);
                    wordHistory.getChildren().addAll(wordLabel,wordPointsLabel);
                    wordHistoryVBox.getChildren().add(0, wordHistory);
                    notificationLabel.setText('"' + currentWord + '"' + " saved, " + wordPointsLabel.getText() + " points!");
                    //TODO
                    //flash all letters green
                }
                break;
            }
            else {
                notificationLabel.setText('"' + currentWord + '"' + " is not a valid word.");
                //TODO
                //flash all letters red
            }
        }
        pointsLabel.setText("Points: " + points);
        saveWord = true;
        handleClearWord(event);
    }

    @FXML
    void handleNewGame(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("New Game");
        alert.setHeaderText(null);
        alert.setContentText("Warning, this action will start a new game. All progress will be lost.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            initialize();
            saveWord = true;
            handleClearWord(event);
            wordHistoryVBox.getChildren().clear();
            notificationLabel.setStyle(colorNotification);
            notificationLabel.setText("New game! Game reset!");
            if (changeSetting) {
                if (typeWords) {
                    textWordGuess.setVisible(true);
                } else {
                    textWordGuess.setVisible(false);
                }
            }
        }
        else if (changeSetting) {
            if (typeWords) {
                typeWords = false;
                radioTypeWords.setSelected(false);
            }
            else {
                typeWords = true;
                radioTypeWords.setSelected(true);
            }
        }
        changeSetting = false;
    }

    @FXML
    public void handleChangeDictionary(ActionEvent event) {
        RadioMenuItem button = (RadioMenuItem)radioDictionary.getSelectedToggle();
        switch (button.getText()) {
            case "English":
                dictFile = getClass().getResourceAsStream("/dictionaries/bogwords.txt");
                break;
            case "Enable (English)":
                dictFile = getClass().getResourceAsStream("/dictionaries/enable1.txt");
                break;
            case "Scrabble (English)":
                dictFile = getClass().getResourceAsStream("/dictionaries/ospd.txt");
                break;
            case "Spanish":
                dictFile = getClass().getResourceAsStream("/dictionaries/espanol.txt");
                break;
            case "French":
                dictFile = getClass().getResourceAsStream("/dictionaries/francais.txt");
                break;
            case "Italian":
                dictFile = getClass().getResourceAsStream("/dictionaries/italiano.txt");
                break;
            case "Dutch":
                dictFile = getClass().getResourceAsStream("/dictionaries/nederlands3.txt");
                break;
            case "German":
                dictFile = getClass().getResourceAsStream("/dictionaries/deutsch.txt");
                break;
            case "Custom...":
                FileChooser fc = new FileChooser();
                File temp = fc.showOpenDialog(null);
                String path = temp.getAbsolutePath();
                try {
                    dictFile = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
        newDict = true;
        //TODO
        //alert user that this will start a new game.
        handleNewGame(event);
        notificationLabel.setText("New game! Current dictionary: " + button.getText());
    }

    @FXML
    public void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setContentText("Java Boggle v1.3\n\nWritten April 2016 by Jordan Wells and Sam Murray.\nLast updated 27 April 2016");
        alert.showAndWait();
    }

    @FXML
    public void handleTypeWords(ActionEvent event) {
        changeSetting = true;
        if (radioTypeWords.isSelected()) {
            typeWords = true;
        }
        else {
            typeWords = false;
        }
        handleNewGame(event);
    }

    @FXML
    public void handleGuessWord(ActionEvent event) {
//        input_search.textProperty().addListener((ov, oldValue, newValue) -> {
            textWordGuess.setText(textWordGuess.getText().toUpperCase());
//        });
        currentWord = textWordGuess.getText();
        handleSaveWord(event);
        textWordGuess.setText("");
    }

    @FXML
    private void handleClose(ActionEvent event) {
        System.exit(0);
    }

    //the "jumble" simulates actual boggle, using real die letters
    //  and not repeating dice. additionally, the letter from
    //  each die is random
    @FXML
    void initialize() {
        Random rand = new Random();
        for(int k = 0; k < 4; k++) {
            for(int j = 0; j < 4; j++) {
                availableLetters[k][j] = 1;
            }
        }
        points = 0;
        maxPoints = 0;
        pointsLabel.setText("Points: " + points);

        //this prevents duplicate die numbers
        //list1 is a list of die numbers
        //list2 is the list of which die each grid cell will be
        //TODO
        // Morrison said something about sets not allowing duplicates?
        List<Integer> list1 = new ArrayList<Integer>(){{
            add(0); add(1); add(2); add(3);
            add(4); add(5); add(6); add(7);
            add(8); add(9); add(10); add(11);
            add(12); add(13); add(14); add(15);
        }};
        List<Integer> list2 = new ArrayList<>();
        for (int k = 16; k > 0; k--) {
            int die = rand.nextInt(k);
            list2.add(list1.get(die));
            list1.remove(die);
        }

        //Jumble
        //this shakes the box to jumble the letters
        int k = 0;
        for (Node node: gridPane.getChildren()) {
            Label label = (Label)node;
            label.setText("" + (dice.get(list2.get(k))).charAt(rand.nextInt(6)));
            if (label.getText().equals("Q")) {
                label.setText("Qu");
            }
            k++;
        }

        usedWords.clear();
        AIusedWords.clear();
        if(newDict) {
            dict.clear();
            addToDict();
        }

        //TODO
        if (typeWords) {
            handleFindWords();
        }
    }
}
