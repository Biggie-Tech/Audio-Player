import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import weka.core.Instances;
import weka.experiment.InstanceQuery;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by D on 27/07/2017.
 */
public class DatabaseHandler {

    private static volatile DatabaseHandler instance;
    private static final String JDBC_DRIVER = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:Songs.db";
    private static final String CREATE_TABLE_SONG = "CREATE TABLE IF NOT EXISTS `song` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`title` TEXT," +
            "`artist` TEXT," +
            "`album` TEXT," +
            "`emotion_audio` TEXT," +
            "`emotion_lyric` TEXT," +
            "`physical_path` TEXT UNIQUE)";
    private static final String CREATE_TABLE_PLAYLIST = "CREATE TABLE IF NOT EXISTS `playlist` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`name` TEXT)";
    private static final String CREATE_TABLE_SONGS_IN_PLAYLIST = "CREATE TABLE IF NOT EXISTS `songs_in_playlist` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`songId` INTEGER," +
            "`playlistId` INTEGER," +
            "FOREIGN KEY(`songId`) REFERENCES song ( id )," +
            "FOREIGN KEY(`playlistId`) REFERENCES playlist(id))";
    private static final String CREATE_TABLE_LYRIC = "CREATE TABLE IF NOT EXISTS `lyric` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`songId` INTEGER UNIQUE," +
            "`lyric` TEXT," +
            "`emotion` TEXT," +
            "FOREIGN KEY(`songId`) REFERENCES song ( id ))";
    private static final String CREATE_TABLE_AUDIO_FEATURE = "CREATE TABLE IF NOT EXISTS `audio_feature` (" +
            "`id` INTEGER PRIMARY KEY AUTOINCREMENT," +
            "`songId` INTEGER UNIQUE," +
            "`audio_feature_1` REAL,`audio_feature_2` REAL,`audio_feature_3` REAL,`audio_feature_4` REAL,`audio_feature_5` REAL,`audio_feature_6` REAL," +
            "`audio_feature_7` REAL,`audio_feature_8` REAL,`audio_feature_9` REAL,`audio_feature_10` REAL,`audio_feature_11` REAL,`audio_feature_12` REAL," +
            "`audio_feature_13` REAL,`audio_feature_14` REAL,`audio_feature_15` REAL,`audio_feature_16` REAL,`audio_feature_17` REAL,`audio_feature_18` REAL," +
            "`audio_feature_19` REAL,`audio_feature_20` REAL,`audio_feature_21` REAL,`audio_feature_22` REAL,`audio_feature_23` REAL,`audio_feature_24` REAL," +
            "`audio_feature_25` REAL,`audio_feature_26` REAL,`audio_feature_27` REAL,`audio_feature_28` REAL,`audio_feature_29` REAL,`audio_feature_30` REAL," +
            "`audio_feature_31` REAL,`audio_feature_32` REAL,`audio_feature_33` REAL,`audio_feature_34` REAL,`audio_feature_35` REAL,`audio_feature_36` REAL," +
            "`audio_feature_37` REAL,`audio_feature_38` REAL,`audio_feature_39` REAL,`audio_feature_40` REAL,`audio_feature_41` REAL,`audio_feature_42` REAL," +
            "`audio_feature_43` REAL,`audio_feature_44` REAL,`audio_feature_45` REAL,`audio_feature_46` REAL,`audio_feature_47` REAL,`audio_feature_48` REAL," +
            "`audio_feature_49` REAL,`audio_feature_50` REAL,`audio_feature_51` REAL,`audio_feature_52` REAL,`audio_feature_53` REAL,`audio_feature_54` REAL," +
            "`audio_feature_55` REAL,`audio_feature_56` REAL,`audio_feature_57` REAL,`audio_feature_58` REAL,`audio_feature_59` REAL,`audio_feature_60` REAL," +
            "`audio_feature_61` REAL,`audio_feature_62` REAL,`audio_feature_63` REAL,`audio_feature_64` REAL,`audio_feature_65` REAL,`audio_feature_66` REAL," +
            "`emotion` TEXT,\n" +
            "FOREIGN KEY(`songId`) REFERENCES song ( id )\n" +
            ")";
    private static final String AUDIO_FEATURE_NAMES = "audio_feature_1,audio_feature_2,audio_feature_3,audio_feature_4," +
            "audio_feature_5,audio_feature_6,audio_feature_7,audio_feature_8," +
            "audio_feature_9,audio_feature_10,audio_feature_11,audio_feature_12," +
            "audio_feature_13,audio_feature_14,audio_feature_15,audio_feature_16," +
            "audio_feature_17,audio_feature_18,audio_feature_19,audio_feature_20," +
            "audio_feature_21,audio_feature_22,audio_feature_23,audio_feature_24," +
            "audio_feature_25,audio_feature_26,audio_feature_27,audio_feature_28," +
            "audio_feature_29,audio_feature_30,audio_feature_31,audio_feature_32," +
            "audio_feature_33,audio_feature_34,audio_feature_35,audio_feature_36," +
            "audio_feature_37,audio_feature_38,audio_feature_39,audio_feature_40," +
            "audio_feature_41,audio_feature_42,audio_feature_43,audio_feature_44," +
            "audio_feature_45,audio_feature_46,audio_feature_47,audio_feature_48," +
            "audio_feature_49,audio_feature_50,audio_feature_51,audio_feature_52," +
            "audio_feature_53,audio_feature_54,audio_feature_55,audio_feature_56," +
            "audio_feature_57,audio_feature_58,audio_feature_59,audio_feature_60," +
            "audio_feature_61,audio_feature_62,audio_feature_63,audio_feature_64," +
            "audio_feature_65,audio_feature_66";
    private static final String[] STATEMENTES = {CREATE_TABLE_SONG, CREATE_TABLE_PLAYLIST, CREATE_TABLE_SONGS_IN_PLAYLIST, CREATE_TABLE_LYRIC, CREATE_TABLE_AUDIO_FEATURE, };
    private Connection connection;
    //private Statement statement;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private InstanceQuery instanceQuery;
    private Instances instances;

    private DatabaseHandler() {
        //createConnection();
        try {
            buildDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseHandler getInstance() {
        synchronized (DatabaseHandler.class) {
            instance = new DatabaseHandler();
            return instance;
        }
    }

    /*public boolean isDuplicateInPlaylist(Song song) {

        return false;
    }*/

    public void updateLyricEmotion(Song song) {
        try {
            createConnection();
            preparedStatement = connection.prepareStatement("UPDATE song SET emotion_lyric = ? WHERE id = ?");
            preparedStatement.setString(1, song.getEmotionLyric());
            preparedStatement.setInt(2, song.getId());
            preparedStatement.setQueryTimeout(30);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateAudioEmotion(Song song) {
        try {
            createConnection();
            preparedStatement = connection.prepareStatement("UPDATE song SET emotion_audio = ? WHERE id = ?");
            preparedStatement.setString(1, song.getEmotionAudio());
            preparedStatement.setInt(2, song.getId());
            preparedStatement.setQueryTimeout(30);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Instances getLyricInstance(Song song) {
        try {
            createConnection();
            instanceQuery = new InstanceQuery();
            instanceQuery.setDatabaseURL(DB_URL);
            instances = instanceQuery.retrieveInstances("SELECT lyric, emotion FROM lyric WHERE songId = " + song.getId());
            instances.setRelationName("Emotion");
            instances.setClassIndex(instances.numAttributes() - 1);
            instanceQuery.close();
            instanceQuery.disconnectFromDatabase();
            return instances;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public Instances getAudioInstance(Song song) {
        try {
            createConnection();
            instanceQuery = new InstanceQuery();
            instanceQuery.setDatabaseURL(DB_URL);
            instances = instanceQuery.retrieveInstances("SELECT "+ AUDIO_FEATURE_NAMES +", emotion FROM audio_feature WHERE songId = " + song.getId());
            instances.setRelationName("Emotion");
            instances.setClassIndex(instances.numAttributes() - 1);
            instanceQuery.close();
            instanceQuery.disconnectFromDatabase();
            return instances;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public void insertLyrics(Song song, String lyrics) {
        try {
            createConnection();
            preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO lyric (songId, lyric, emotion) VALUES (?,?,?)");
            preparedStatement.setInt(1, song.getId());
            preparedStatement.setString(2, lyrics);
            preparedStatement.setString(3, "?");
            preparedStatement.setQueryTimeout(30);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertAudioFeature(Song song, ArrayList<Double> audioFeatures) {
        try {
            createConnection();
            preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO audio_feature VALUES (null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            preparedStatement.setInt(1, song.getId());
            preparedStatement.setDouble(2, audioFeatures.get(0));
            preparedStatement.setDouble(3, audioFeatures.get(1));
            preparedStatement.setDouble(4, audioFeatures.get(2));
            preparedStatement.setDouble(5, audioFeatures.get(3));
            preparedStatement.setDouble(6, audioFeatures.get(4));
            preparedStatement.setDouble(7, audioFeatures.get(5));
            preparedStatement.setDouble(8, audioFeatures.get(6));
            preparedStatement.setDouble(9, audioFeatures.get(7));
            preparedStatement.setDouble(10, audioFeatures.get(8));
            preparedStatement.setDouble(11, audioFeatures.get(9));
            preparedStatement.setDouble(12, audioFeatures.get(10));
            preparedStatement.setDouble(13, audioFeatures.get(11));
            preparedStatement.setDouble(14, audioFeatures.get(12));
            preparedStatement.setDouble(15, audioFeatures.get(13));
            preparedStatement.setDouble(16, audioFeatures.get(14));
            preparedStatement.setDouble(17, audioFeatures.get(15));
            preparedStatement.setDouble(18, audioFeatures.get(16));
            preparedStatement.setDouble(19, audioFeatures.get(17));
            preparedStatement.setDouble(20, audioFeatures.get(18));
            preparedStatement.setDouble(21, audioFeatures.get(19));
            preparedStatement.setDouble(22, audioFeatures.get(20));
            preparedStatement.setDouble(23, audioFeatures.get(21));
            preparedStatement.setDouble(24, audioFeatures.get(22));
            preparedStatement.setDouble(25, audioFeatures.get(23));
            preparedStatement.setDouble(26, audioFeatures.get(24));
            preparedStatement.setDouble(27, audioFeatures.get(25));
            preparedStatement.setDouble(28, audioFeatures.get(26));
            preparedStatement.setDouble(29, audioFeatures.get(27));
            preparedStatement.setDouble(30, audioFeatures.get(28));
            preparedStatement.setDouble(31, audioFeatures.get(29));
            preparedStatement.setDouble(32, audioFeatures.get(30));
            preparedStatement.setDouble(33, audioFeatures.get(31));
            preparedStatement.setDouble(34, audioFeatures.get(32));
            preparedStatement.setDouble(35, audioFeatures.get(33));
            preparedStatement.setDouble(36, audioFeatures.get(34));
            preparedStatement.setDouble(37, audioFeatures.get(35));
            preparedStatement.setDouble(38, audioFeatures.get(36));
            preparedStatement.setDouble(39, audioFeatures.get(37));
            preparedStatement.setDouble(40, audioFeatures.get(38));
            preparedStatement.setDouble(41, audioFeatures.get(39));
            preparedStatement.setDouble(42, audioFeatures.get(40));
            preparedStatement.setDouble(43, audioFeatures.get(41));
            preparedStatement.setDouble(44, audioFeatures.get(42));
            preparedStatement.setDouble(45, audioFeatures.get(43));
            preparedStatement.setDouble(46, audioFeatures.get(44));
            preparedStatement.setDouble(47, audioFeatures.get(45));
            preparedStatement.setDouble(48, audioFeatures.get(46));
            preparedStatement.setDouble(49, audioFeatures.get(47));
            preparedStatement.setDouble(50, audioFeatures.get(48));
            preparedStatement.setDouble(51, audioFeatures.get(49));
            preparedStatement.setDouble(52, audioFeatures.get(50));
            preparedStatement.setDouble(53, audioFeatures.get(51));
            preparedStatement.setDouble(54, audioFeatures.get(52));
            preparedStatement.setDouble(55, audioFeatures.get(53));
            preparedStatement.setDouble(56, audioFeatures.get(54));
            preparedStatement.setDouble(57, audioFeatures.get(55));
            preparedStatement.setDouble(58, audioFeatures.get(56));
            preparedStatement.setDouble(59, audioFeatures.get(57));
            preparedStatement.setDouble(60, audioFeatures.get(58));
            preparedStatement.setDouble(61, audioFeatures.get(59));
            preparedStatement.setDouble(62, audioFeatures.get(60));
            preparedStatement.setDouble(63, audioFeatures.get(61));
            preparedStatement.setDouble(64, audioFeatures.get(62));
            preparedStatement.setDouble(65, audioFeatures.get(63));
            preparedStatement.setDouble(66, audioFeatures.get(64));
            preparedStatement.setDouble(67, audioFeatures.get(65));
            preparedStatement.setString(68, "?");
            preparedStatement.setQueryTimeout(30);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSong(Song song) {
            try {
                createConnection();
                preparedStatement = connection.prepareStatement("INSERT OR IGNORE INTO song (title, artist, album, emotion_audio, emotion_lyric, physical_path) VALUES (?,?,?,?,?,?)");
                preparedStatement.setString(1, checkInfo(song.getTitle()));
                preparedStatement.setString(2, checkInfo(song.getArtist()));
                preparedStatement.setString(3, checkInfo(song.getAlbum()));
                preparedStatement.setString(4, "?");
                preparedStatement.setString(5, "?");
                preparedStatement.setString(6, song.getPath());
                preparedStatement.setQueryTimeout(30);
                preparedStatement.executeUpdate();
                connection.close();
            } catch(Throwable t) {
                System.out.println("DatabaseHandler=======insertSong");
                t.printStackTrace();
            }
    }

    public ObservableList<Song> getAllSongs() throws SQLException{
        createConnection();
        preparedStatement = connection.prepareStatement("SELECT * FROM song");
        Song song;
        ObservableList<Song> songs = FXCollections.observableArrayList();
        resultSet = preparedStatement.executeQuery();
        int id;
        String title;
        String artist;
        String album;
        String emotionAudio;
        String emotionLyric;
        String physicalPath;
        while (resultSet.next()) {
            id = resultSet.getInt("id");
            title = resultSet.getString("title");
            artist = resultSet.getString("artist");
            album = resultSet.getString("album");
            emotionAudio = resultSet.getString("emotion_audio");
            emotionLyric = resultSet.getString("emotion_lyric");
            physicalPath = resultSet.getString("physical_path");
            song = new Song(physicalPath);
            song.setId(id);
            song.setTitle(title);
            song.setArtist(artist);
            song.setAlbum(album);
            song.setEmotionAudio(emotionAudio);
            song.setEmotionLyric(emotionLyric);
            songs.add(song);
        }
        connection.close();
        resultSet.close();
        return songs;
    }

    private String checkInfo(String string) {
        if(string == null || string.equals(null) || string.equals("")|| string.equals(" ")|| string.equals("  ")) {
            string = "Unknown";
            return string;
        }
        else {
            return string;
        }
    }

    public boolean featureExists(int id, String table) {
        try {
            createConnection();
            if (table.equals("audio")) {
                preparedStatement = connection.prepareStatement("SELECT COUNT (id) FROM audio_feature WHERE songId = ?");
                preparedStatement.setInt(1, id);
                resultSet = preparedStatement.executeQuery();
            }
            else {
                preparedStatement = connection.prepareStatement("SELECT COUNT (id) FROM lyric WHERE songId = ?");
                preparedStatement.setInt(1, id);
                resultSet = preparedStatement.executeQuery();
            }
            if (resultSet.getInt(1) > 0) {
                connection.close();
                resultSet.close();
                return true;
            }
            else {
                connection.close();
                resultSet.close();
                return false;
            }
        }catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private void createConnection() {
        try {
            if (connection == null) {
                Class.forName(JDBC_DRIVER);
                connection = DriverManager.getConnection(DB_URL);
            }
            else if (connection.isClosed()){
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch(SQLException e) {
            e.printStackTrace();
            System.out.println("Connection Not Found");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void buildDatabase() throws SQLException{
        createConnection();
        for (String s: STATEMENTES) {
            preparedStatement = connection.prepareStatement(s);
            preparedStatement.executeUpdate();
        }
        connection.close();
    }
}
