import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BoggleBoardController {

    Integer colIndex;
    Integer rowIndex;
    Integer points = 0;
    Integer[][] availableLetters = new Integer[4][4];
    List<Node> clickedLetters = new ArrayList<>();
    Pair<Integer, Integer> lastClicked = new Pair<>(null, null);
    List<String> dice = new ArrayList<String>(16) {{
        add("AAEEGN"); add("ABBJOO"); add("ACHOPS");
        add("AFFKPS"); add("AOOTTW"); add("CIMOTU");
        add("DEILRX"); add("DELRVY"); add("DISTTY");
        add("EEGHNW"); add("EEINSU"); add("EHRTVW");
        add("EIOSST"); add("ELRTTY"); add("HIMNUQ");
        add("HLNNRZ");
    }};
    ArrayList<String> usedWords = new ArrayList<>();
    ArrayList<String> dict = new ArrayList<>();
    URL url = getClass().getResource("dictionaries/bogwords.txt");
    File dictFile = new File(url.getPath());
    boolean newDict = true;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private GridPane gridPane;

    @FXML
    private Label pointsLabel;

    @FXML
    private VBox wordHistoryVBox;


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

    private boolean isValidLetter(MouseEvent e, Node source) {
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

    @FXML
    void handleOnMouseClicked(MouseEvent event) {
        Node source = (Node)event.getSource();
        mouseEvent(event, source);
        if (isValidLetter(event, source)) {
            clickedLetters.add(source);
            availableLetters[rowIndex][colIndex] = 0;
            lastClicked = new Pair<>(rowIndex, colIndex);
            source.setStyle("-fx-background-color:rgba(40,80,255,1); -fx-opacity:0.9;");
        }
    }

    @FXML
    void handleOnMouseEnter(MouseEvent event) {
        Node source = (Node) event.getSource();
        mouseEvent(event, source);
        if (isValidLetter(event, source)) {
            source.setStyle("-fx-background-color:rgba(90,255,110,1); -fx-opacity:0.8;");
        }
        else {
            for (Node node: clickedLetters) {
                if (source == node) {
                    return;
                }
            }
            source.setStyle("-fx-background-color:rgba(255,90,90,1); -fx-opacity:0.8;");
        }
    }

    @FXML
    void handleOnMouseExit(MouseEvent event) {
        Node source = (Node) event.getSource();
        mouseEvent(event, source);
        if (availableLetters[rowIndex][colIndex] == 1) {
            source.setStyle("-fx-background-color: rgba(245,245,245,1); -fx-opacity: 0.8;");
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
            node.setStyle("-fx-background-color: rgba(245,245,245,1); -fx-opacity: 0.8;");
        }
    }

    //save word button
    @FXML
    void handleSaveWord(ActionEvent event) {
        String word = "";
        for (Node node: clickedLetters) {
            Label label = (Label)node;
            word += label.getText();
        }
        System.out.println(word);
        boolean wordFound = false;
        for (String line: dict) {
            if (word.equalsIgnoreCase(line)) {
                for (String newWord: usedWords) {
                    if (word.equalsIgnoreCase(newWord)) {
                        wordFound = true;
                    }
                }
                if (!wordFound) {
                    HBox wordHistory = new HBox();
                    wordHistory.setMinSize(181, Region.USE_COMPUTED_SIZE);
                    wordHistory.setPrefSize(194,20);
                    wordHistory.setMaxSize(Region.USE_PREF_SIZE,Region.USE_COMPUTED_SIZE);
                    Label wordLabel = new Label(word);
                    wordLabel.setTranslateX(5);
                    wordLabel.setMinSize(155,Region.USE_COMPUTED_SIZE);
                    wordLabel.setPrefSize(155,20);
                    wordLabel.setMaxSize(155,Region.USE_COMPUTED_SIZE);
                    Label wordPointsLabel = new Label();
                    wordPointsLabel.setMinSize(20,Region.USE_COMPUTED_SIZE);
                    wordPointsLabel.setPrefSize(32,20);
                    wordPointsLabel.setMaxSize(Region.USE_COMPUTED_SIZE,Region.USE_COMPUTED_SIZE);
                    switch (word.length()) {
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
                    usedWords.add(word);
                    wordHistory.getChildren().addAll(wordLabel,wordPointsLabel);
                    wordHistoryVBox.getChildren().add(0, wordHistory);
                }
            }
        }
        pointsLabel.setText("Points: " + points);
        handleClearWord(event);
    }

    @FXML
    void handleNewGame(ActionEvent event) {
        initialize();
        handleClearWord(event);
        wordHistoryVBox.getChildren().clear();
    }

    void addToDict() {
        try(BufferedReader br = new BufferedReader(new FileReader(dictFile))) {
            for(String line; (line = br.readLine()) != null; ) {
                dict.add(line);
            }
        }
        catch (IOException ex){}
        newDict = false;
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
        if(newDict) {
            dict.clear();
            addToDict();
        }
    }

    @FXML
    private void handleClose(ActionEvent event) {
        System.exit(0);
    }
}
