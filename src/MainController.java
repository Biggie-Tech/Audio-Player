import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import org.farng.mp3.MP3File;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by D on 26/07/2017.
 */
public class MainController {


    private Main parent;
    private Parent parentScene;
    private int songIndex = 0;
    private static Duration songTime;
    private static Duration songDuration;
    private boolean isClickChanging = false;
    private boolean busy;
    private Song song;

    //Button Controls
    private Button buttonPlay;
    private Button buttonNext;
    private Button buttonPrevious;
    private Button buttonExtractAudioFeatures;
    private Button buttonFindLyrics;
    private Button buttonClassifyLyrics;
    private Button buttonClassifyAudio;

    //MenuBar Controls
    private MenuBar menuBar;
    private Menu menuFile;
    private MenuItem menuItemOpenFile;
    private MenuItem menuItemOpenFolder;
    private Menu menuEdit;
    //private MenuItem menuItemAddToPlaylist;

    //private MenuItem menuItemDelete;
    private Menu menuView;
    private Menu menuHelp;
    //private MenuItem menuItemAbout;

    //Slider Controls
    private Slider sliderVolume;
    private Slider sliderSongSeek;
    private ProgressBar progressBarSong;
    private ProgressIndicator progressIndicatorProgress;

    //Table
    private TableView<Song> centreTable;
    private ListView<String> listViewPlaylists;
    private ObservableList<Song> mainSongList;
    private ObservableList<String> playlistNames;
    private ObservableList<ObservableList> playlists;
    private ObservableList<Song> playlistAngryLyrics;
    private ObservableList<Song> playlistCalmLyrics;
    private ObservableList<Song> playlistHappyLyrics;
    private ObservableList<Song> playlistSadLyrics;
    private ObservableList<Song> playlistAngryAudio;
    private ObservableList<Song> playlistCalmAudio;
    private ObservableList<Song> playlistHappyAudio;
    private ObservableList<Song> playlistSadAudio;

    //labels
    private Label labelArtist;
    private Label labelTitle;
    private Label labelAlbum;
    private Label labelSongDuration;
    private Label labelProgress;
    private ImageView imageViewAlbumArt;

    /*ClassifierHandler cc;
    DatabaseHandler dc;*/

    private PlayerHandler playerHandler;
    private DatabaseHandler databaseHandler;
    private ClassifierHandler classifierHandler;
    private FeatureFinder featureFinder;


    public MainController(Main main) {
        playerHandler = new PlayerHandler();
        databaseHandler = DatabaseHandler.getInstance();
        parent = main;
        parentScene = parent.getScene().getRoot();
        setGuiComponents();
        setButtonEvents();
        setMenuBarEvents();
        buildCentreTable();
        setSliderEvents();
        buildEmotionPlaylists();
        buildListViewPlaylists();
        busy = false;
    }


    public void setGuiComponents() {
        //Assign Button Controls
        buttonPlay = (Button) parentScene.lookup("#buttonPlay");
        buttonNext = (Button) parentScene.lookup("#buttonNext");
        buttonPrevious = (Button) parentScene.lookup("#buttonPrevious");
        buttonExtractAudioFeatures = (Button) parentScene.lookup("#buttonExtractAudioFeatures");
        buttonFindLyrics = (Button) parentScene.lookup("#buttonFindLyrics");
        buttonClassifyLyrics = (Button) parentScene.lookup("#buttonClassifyLyrics");
        buttonClassifyAudio = (Button) parentScene.lookup("#buttonClassifyAudio");
        labelTitle = (Label) parentScene.lookup("#labelTitle");
        labelTitle.setVisible(false);
        labelArtist = (Label) parentScene.lookup("#labelArtist");
        labelArtist.setVisible(false);
        labelAlbum = (Label) parentScene.lookup("#labelAlbum");
        labelAlbum.setVisible(false);
        labelSongDuration = (Label) parentScene.lookup("#labeSongDuration") ;
        labelProgress = (Label) parentScene.lookup("#labelProgress");
        labelProgress.setVisible(false);
        imageViewAlbumArt = (ImageView) parentScene.lookup("#imageViewAlbumArt");


        //Assign Menu Controls
        menuBar = (MenuBar) parentScene.lookup("#menuBar");

        menuFile = new Menu("File");
        menuItemOpenFile = new MenuItem("Open File");
        menuItemOpenFolder = new MenuItem("Open Folder");

        menuEdit = new Menu("Edit");
        menuView = new Menu("View");
        menuHelp = new Menu("Help");
        menuFile.getItems().addAll(menuItemOpenFile, menuItemOpenFolder);
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuHelp);

        //Assign Slider Controls
        sliderVolume = (Slider) parentScene.lookup("#sliderVolume");
        sliderSongSeek = (Slider) parentScene.lookup("#sliderSongSeek");
        progressBarSong = (ProgressBar) parentScene.lookup("#progressBarSong");
        progressIndicatorProgress = (ProgressIndicator) parentScene.lookup("#progressIndicatorProgress");
        progressIndicatorProgress.setVisible(false);

        //Assign centreTable and left listView
        centreTable = (TableView<Song>) parentScene.lookup("#centreTable");
        listViewPlaylists = (ListView<String>) parentScene.lookup("#listViewPlaylists");
    }

    private void setButtonEvents() {

        buttonPlay.setOnAction(e -> {
            buttonPlay.setText(playerHandler.pauseOrResume());
        });

        buttonNext.setOnAction(e -> {
            songIndex++;
            if (songIndex > centreTable.getItems().size() - 1) songIndex = 0; {
                centreTable.getSelectionModel().select(songIndex);
                song = centreTable.getItems().get(songIndex);
                playerHandler.play(song);
                setCurrentTrackTime();
                updateSongLabels(song);
                displayAlbumArt(song);

            }
        });

        buttonPrevious.setOnAction(e -> {
            songIndex--;
            if (songIndex < 0) songIndex = centreTable.getItems().size() - 1; {
                centreTable.getSelectionModel().select(songIndex);
                song = centreTable.getItems().get(songIndex);
                playerHandler.play(song);
                updateSongLabels(song);
                setCurrentTrackTime();
                displayAlbumArt(song);
            }
        });

        buttonExtractAudioFeatures.setOnAction(actionEvent -> {
            if (!busy) {
                //databaseHandler = DatabaseHandler.getInstance();
                featureFinder = new FeatureFinder();
                showProgress();
                extractAudioFeaturesTask();
            } else {
                displayBusyError();
            }
        });

        buttonFindLyrics.setOnAction(e -> {
            if (!busy) {
                //databaseHandler = DatabaseHandler.getInstance();
                featureFinder = new FeatureFinder();
                showProgress();
                findLyricsTask();
            } else {
                displayBusyError();
            }
        });

        buttonClassifyLyrics.setOnAction(e -> {
            if (!busy) {
                classifierHandler = ClassifierHandler.getInstance();
                //databaseHandler = DatabaseHandler.getInstance();
                showProgress();
                classifyLyricsTask();
            }else {
                displayBusyError();
            }
        });

        buttonClassifyAudio.setOnAction(e -> {
            if (!busy) {
                classifierHandler = ClassifierHandler.getInstance();
                //databaseHandler = DatabaseHandler.getInstance();
                showProgress();
                classifyAudioTask();
            }else {
                displayBusyError();
            }
        });
    }

    private void buildEmotionPlaylists() {
        playlistAngryLyrics = FXCollections.observableArrayList();
        playlistCalmLyrics = FXCollections.observableArrayList();
        playlistHappyLyrics = FXCollections.observableArrayList();
        playlistSadLyrics = FXCollections.observableArrayList();
        playlistAngryAudio = FXCollections.observableArrayList();
        playlistCalmAudio = FXCollections.observableArrayList();
        playlistHappyAudio = FXCollections.observableArrayList();
        playlistSadAudio = FXCollections.observableArrayList();
        playlistNames = FXCollections.observableArrayList();
        playlists = FXCollections.observableArrayList();
        for (Song s : mainSongList) {
            switch (s.getEmotionAudio()) {
                case "Angry":
                    playlistAngryAudio.add(s);
                    break;
                case "Happy":
                    playlistHappyAudio.add(s);
                    break;
                case "Sad":
                    playlistSadAudio.add(s);
                    break;
                case "Calm":
                    playlistCalmAudio.add(s);
                    break;
            }
            switch (s.getEmotionLyric()) {
                case "Angry":
                    playlistAngryLyrics.add(s);
                    break;
                case "Happy":
                    playlistHappyLyrics.add(s);
                    break;
                case "Sad":
                    playlistSadLyrics.add(s);
                    break;
                case "Calm":
                    playlistCalmLyrics.add(s);
                    break;
            }
        }

        playlistNames.addAll("All Songs", "Angry Lyrics", "Angry Audio", "Calm Lyrics",
                "Calm Audio", "Happy Lyrics", "Happy Audio", "Sad Lyrics", "Sad Audio");
        playlists.addAll(playlistAngryLyrics, playlistCalmLyrics, playlistHappyLyrics, playlistSadLyrics,
                playlistAngryAudio, playlistCalmAudio, playlistHappyAudio, playlistSadAudio);
    }

    private void extractAudioFeaturesTask() {
        busy = true;
        Task task = new Task() {
            int i;
            @Override
            protected Object call() throws Exception {
                    for (i = 0; i < centreTable.getItems().size(); i++) {
                        Song s = centreTable.getItems().get(i);
                        Platform.runLater(() -> {
                            labelProgress.setText("Analysing: (" + (i + 1) + "/" + centreTable.getItems().size() + ")");
                        });
                        if (!databaseHandler.featureExists(s.getId(), "audio")) {
                            ArrayList<Double> arrayList = featureFinder.extractAudioFeatures(new File(s.getPath()));
                            databaseHandler.insertAudioFeature(s, arrayList);
                        }
                    }
                return null;
            }
        };
        progressIndicatorProgress.setProgress(task.getProgress());
        task.setOnSucceeded(event -> {
            hideProgress();

            busy = false;
        });
        new Thread(task).start();
    }

    private void findLyricsTask() {
        busy = true;
        Task task = new Task() {
            int i;
            @Override
            protected Object call() throws Exception {
                String lyrics;
                for (i = 0; i < centreTable.getItems().size(); i++) {
                    Song s = centreTable.getItems().get(i);
                    Platform.runLater(() -> {
                        labelProgress.setText("Searching: (" + (i+1) + "/" + centreTable.getItems().size() + ")");
                    });
                    if (!databaseHandler.featureExists(s.getId(), "lyric")) {
                        lyrics = featureFinder.findLyric(s.getTitle(), s.getArtist());
                        if (!lyrics.equals("")){
                            databaseHandler.insertLyrics(s, lyrics);
                        }
                    }
                }
                return null;
            }
        };
        progressIndicatorProgress.setProgress(task.getProgress());
        task.setOnSucceeded(event -> {
            busy = false;
            hideProgress();
        });
        new Thread(task).start();
    }

    private void classifyLyricsTask() {
        busy = true;
        Task task = new Task() {
            int i;
            @Override
            protected Object call() throws Exception {
                for (i = 0; i < centreTable.getItems().size(); i++) {
                    Song s = centreTable.getItems().get(i);
                    Platform.runLater(() -> {
                        labelProgress.setText("Classifying Lyrics: (" + (i+1) + "/" + centreTable.getItems().size() + ")");
                    });
                    if (databaseHandler.featureExists(s.getId(), "lyric")) {
                        if (s.getEmotionLyric().equals("?")) {
                            classifierHandler.classifySongLyric(s);
                            System.out.println(s.getEmotionLyric());
                            databaseHandler.updateLyricEmotion(s);
                        }
                    }
                }
                return null;
            }
        };
        progressIndicatorProgress.setProgress(task.getProgress());
        task.setOnSucceeded(event -> {
            busy = false;
            hideProgress();
            try {
                centreTable.setItems(databaseHandler.getAllSongs());
                buildEmotionPlaylists();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        new Thread(task).start();
    }

    private void classifyAudioTask() {
        busy = true;
        Task task = new Task() {
            int i;
            @Override
            protected Object call() throws Exception {
                for (i = 0; i < centreTable.getItems().size(); i++) {
                    Song s = centreTable.getItems().get(i);
                    Platform.runLater(() -> {
                        labelProgress.setText("Classifying Audio: (" + (i+1) + "/" + centreTable.getItems().size() + ")");
                    });
                    if (databaseHandler.featureExists(s.getId(), "audio")) {
                        if (s.getEmotionAudio().equals("?")) {
                            classifierHandler.classifySongAudio(s);
                            System.out.println(s.getEmotionAudio());
                            databaseHandler.updateAudioEmotion(s);
                        }
                    }
                }
                return null;
            }
        };
        progressIndicatorProgress.setProgress(task.getProgress());
        task.setOnSucceeded(event -> {
            busy = false;
            hideProgress();
            try {
                centreTable.setItems(databaseHandler.getAllSongs());
                buildListViewPlaylists();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        new Thread(task).start();
    }

    private void setMenuBarEvents() {
        menuItemOpenFile.setOnAction(actionEvent -> {
            File file = selectFile();
            song = new Song(file.getAbsolutePath());
            if (playerHandler.getPlayer() == null) {
                playerHandler.createPlayer(file);
            }
            else {
                playerHandler.getPlayer().dispose();
                playerHandler.createPlayer(file);
            }
            centreTable.getItems().add(song);
            songTime = playerHandler.getPlayer().getCurrentTime();
            songDuration = playerHandler.getPlayer().getTotalDuration();
            labelSongDuration.setText(formatTime(songTime, songDuration));
            setCurrentTrackTime();
            updateSongLabels(song);
        });

        //Menu/File/Open Folder Event
        menuItemOpenFolder.setOnAction(actionEvent -> {
            selectDirectory();
        });
    }

    private void setSliderEvents() {
        //set song slider event
        sliderSongSeek.valueProperty().addListener((observable, oldValue, newValue) -> {
            //sets the progress bar to match the slider
            progressBarSong.setProgress(newValue.doubleValue() / 100);
            //if the slider is moved, the song will forward or reverse to that point
            if (sliderSongSeek.isValueChanging()) {
                playerHandler.seek(sliderSongSeek.getValue());
            }
        });
        //set volume slider event
        sliderVolume.setMin(0);
        sliderVolume.setMax(1);
        sliderVolume.setValue(0.5);
        sliderVolume.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (playerHandler.getPlayer() == null) return;
            playerHandler.getPlayer().setVolume(newValue.doubleValue());
        });

        //sets the events for the seek bar so the song plays from where the slider is dragged/clicked
        sliderSongSeek.setOnMousePressed(event -> {
            playerHandler.seek(sliderSongSeek.getValue());
        });
        sliderSongSeek.setOnMouseClicked(event -> {
            playerHandler.seek(sliderSongSeek.getValue());
        });
        sliderSongSeek.setOnMouseDragged(event -> {
            playerHandler.seek(sliderSongSeek.getValue());
        });
        sliderSongSeek.setOnMouseDragReleased(event -> {
            playerHandler.seek(sliderSongSeek.getValue());
        });

    }

    private void buildCentreTable() {
        //databaseHandler = DatabaseHandler.getInstance();
        
        TableColumn<Song, String> tableTrackName = new TableColumn<>("Title");
        tableTrackName.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableTrackName.setMinWidth(50);

        TableColumn<Song, String> tableTrackArtist = new TableColumn<>("Artist");
        tableTrackArtist.setCellValueFactory(new PropertyValueFactory<>("artist"));
        tableTrackArtist.setMinWidth(50);

        TableColumn<Song, String> tableTrackAlbum = new TableColumn<>("Album");
        tableTrackAlbum.setCellValueFactory(new PropertyValueFactory<>("album"));
        tableTrackAlbum.setMinWidth(50);

        TableColumn<Song, String> tableTrackEmotionAudio = new TableColumn<>("Audio Emotion");
        tableTrackEmotionAudio.setCellValueFactory(new PropertyValueFactory<>("emotionAudio"));
        tableTrackEmotionAudio.setMinWidth(50);

        TableColumn<Song, String> tableTrackEmotionLyric = new TableColumn<>("Lyric Emotion");
        tableTrackEmotionLyric.setCellValueFactory(new PropertyValueFactory<>("emotionLyric"));
        tableTrackEmotionLyric.setMinWidth(50);

        TableColumn<Song, String> tableTrackPath = new TableColumn<>("Track Path");
        tableTrackPath.setCellValueFactory(new PropertyValueFactory<>("path"));
        tableTrackPath.setMinWidth(50);

        centreTable.setEditable(true);
        centreTable.getSelectionModel().setCellSelectionEnabled(true);

        try {
            mainSongList = FXCollections.observableArrayList();
            mainSongList = databaseHandler.getAllSongs();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        centreTable.setItems(mainSongList);
        centreTable.getColumns().addAll(tableTrackName, tableTrackArtist, tableTrackAlbum, tableTrackEmotionLyric, tableTrackEmotionAudio, tableTrackPath);
        //Changes the colour of the emotion column cells depending on the emoiton
        setEmotionColumnColour(tableTrackEmotionLyric);
        setEmotionColumnColour(tableTrackEmotionAudio);
        //creates a row row factory that plays the selected row Song when double-clicked
        centreTable.setRowFactory(event -> {
            TableRow<Song> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2 && (!row.isEmpty())) {
                    playerHandler.play(row.getItem());
                    songIndex = row.getIndex();
                    setCurrentTrackTime();
                    updateSongLabels(row.getItem());
                    displayAlbumArt(row.getItem());
                }
            });
            return row;
        });
    }

    private void buildListViewPlaylists() {
        listViewPlaylists.setItems(playlistNames);
        listViewPlaylists.setOnMouseClicked(e -> {
            String selectedPlaylist = listViewPlaylists.getSelectionModel().getSelectedItem();
            displaySelectedPlaylist(selectedPlaylist);
        });
    }

    private void displaySelectedPlaylist(String name) {
        switch (name) {
            case "All Songs":
                centreTable.setItems(mainSongList);
                break;
            case "Angry Lyrics":
                centreTable.setItems(playlistAngryLyrics);
                resetEmotionColumnColour();
                break;
            case "Angry Audio":
                centreTable.setItems(playlistAngryAudio);
                resetEmotionColumnColour();
                break;
            case "Calm Lyrics":
                centreTable.setItems(playlistCalmLyrics);
                resetEmotionColumnColour();
                break;
            case "Calm Audio":
                centreTable.setItems(playlistCalmAudio);
                resetEmotionColumnColour();
                break;
            case "Happy Lyrics":
                centreTable.setItems(playlistHappyLyrics);
                resetEmotionColumnColour();
                break;
            case "Happy Audio":
                centreTable.setItems(playlistHappyAudio);
                resetEmotionColumnColour();
                break;
            case "Sad Lyrics":
                centreTable.setItems(playlistSadLyrics);
                resetEmotionColumnColour();
                break;
            case "Sad Audio":
                centreTable.setItems(playlistSadAudio);
                resetEmotionColumnColour();
                break;
            default:
                break;
        }
    }

    private void setEmotionColumnColour(TableColumn column) {
        column.setCellFactory(new Callback<TableColumn, TableCell>() {
            public TableCell call(TableColumn param) {
                return new TableCell<Song, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!isEmpty()) {
                            switch (getItem().toString()) {
                                case "Angry":
                                    this.setStyle("-fx-background-color: red");
                                    break;
                                case "Happy":
                                    this.setStyle("-fx-background-color: springgreen");
                                    break;
                                case "Sad":
                                    this.setStyle("-fx-background-color: blue");
                                    break;
                                case "Calm":
                                    this.setStyle("-fx-background-color: moccasin");
                                    break;
                            }
                            setText(item);
                        }
                    }
                };
            }
        });
    }

    private void resetEmotionColumnColour() {
        setEmotionColumnColour(centreTable.getColumns().get(3));
        setEmotionColumnColour(centreTable.getColumns().get(4));
    }

    private File selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("AUDIO Files", "*.MP3", "*.Mp3", "*.mp3")
        );
        fileChooser.setTitle("Select Mp3 File");
        File file = fileChooser.showOpenDialog(parent.getStage());
        return file;
    }

    private void selectDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File file = directoryChooser.showDialog(parent.getStage());
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                searchDirectory(file);
                return null;
            }
        };
        new Thread(task).start();
    }

    private void searchDirectory(File f) {
        MP3File mp3File;
        if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                    try {
                        mp3File = new MP3File(file);
                        Song song = new Song(file.getCanonicalPath());
                        song.setEmotionAudio("?");
                        song.setEmotionLyric("?");
                        song.setTitle(mp3File.getID3v2Tag().getSongTitle());
                        song.setArtist(mp3File.getID3v2Tag().getLeadArtist());
                        song.setAlbum(mp3File.getID3v2Tag().getAlbumTitle());
                        databaseHandler.insertSong(song);
                        if (!checkDuplicateSong(song)) {
                            centreTable.getItems().add(song);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public boolean checkDuplicateSong(Song song) {
        for (Song s : centreTable.getItems()) {
            if (s.getPath().equals(song.getPath())) {
                return true;
            }
        }
        return false;
    }

    private void setCurrentTrackTime() {
        playerHandler.getPlayer().currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            updateSliderSongValues();
        });
    }

    private void updateSliderSongValues() {
        //creates a thread so that the song slider and moves with the song
        if(sliderSongSeek != null
                && labelSongDuration  != null){
            Platform.runLater(() -> {
                songTime = playerHandler.getPlayer().getCurrentTime();
                songDuration = playerHandler.getPlayer().getTotalDuration();
                labelSongDuration.setText(formatTime(songTime, songDuration));
                if (!sliderSongSeek.isDisabled() &&
                        songDuration != null &&
                        songDuration.greaterThan(Duration.ZERO) &&
                        !sliderSongSeek.isValueChanging() &&
                        !isClickChanging) {
                    sliderSongSeek.setValue(songTime.divide(songDuration).toMillis() * 100);
                }
            });
        }

    }

    private  String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        // if elapsedHours > 0 don't subtract "(durationHours * (60 * 60)"
        int elapsedSeconds = elapsedHours > 0 ? intElapsed - elapsedMinutes * 60 : intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

        if (duration!=null && duration.greaterThan(javafx.util.Duration.ZERO)) {

            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);

            if (durationHours > 0) {
                intDuration = intDuration - (durationHours * (60 * 60));
            }

            int durationMinutes = intDuration / 60;
            // if durationHours > 0 don't subtract "(durationHours * (60 * 60)"
            int durationSeconds = durationHours > 0 ? intDuration - durationMinutes * 60 : intDuration - durationHours * 60 * 60 - durationMinutes * 60;

            if (durationHours > 0) {

                return String.format("%d:%02d:%02d - %d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d - %02d:%02d",
                        elapsedMinutes, elapsedSeconds,
                        durationMinutes, durationSeconds);
            }
        } else if (duration!=null) {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d",
                        elapsedMinutes, elapsedSeconds);
            }
        }
        return null;
    }

    private void updateSongLabels(Song song) {
        labelTitle.setText(song.getTitle());
        labelTitle.setVisible(true);
        labelArtist.setText(song.getArtist());
        labelArtist.setVisible(true);
        labelAlbum.setText(song.getAlbum());
        labelAlbum.setVisible(true);

    }
    private void displayAlbumArt(Song song) {
        try {
            playerHandler.getPlayer().getMedia().getMetadata().addListener((MapChangeListener<? super String, ? super Object>) change -> {
                if (change.wasAdded()) {
                    if (change.getKey().toString().equals("image")) {
                        imageViewAlbumArt.setImage((Image)change.getValueAdded());
                    }
                }
            });
        }catch (Exception e) {
        }
    }

    private void displayBusyError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("BUSY!");
        alert.setContentText("Please wait until the background task has finished!");
        alert.showAndWait();
    }

    private void showProgress() {
        labelProgress.setVisible(true);
        progressIndicatorProgress.setVisible(true);
    }
    private void hideProgress() {
        labelProgress.setVisible(false);
        progressIndicatorProgress.setVisible(false);
    }
}
