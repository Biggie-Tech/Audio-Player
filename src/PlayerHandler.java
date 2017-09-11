import javafx.scene.control.Alert;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;

/**
 * Created by D on 26/07/2017.
 */
public class PlayerHandler {

    private File file;
    private Media media;
    private MediaPlayer player;
    private boolean isPlaying = false;

    public MediaPlayer getPlayer() {
        return player;
    }

    public PlayerHandler() {
    }


    public String pauseOrResume() {
        try{
            if (isPlaying == true) {
                player.pause();
                isPlaying = false;
                return "Paused";
            } else {
                player.play();
                isPlaying = true;
                return "Playing";
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            getDialogBox();
            return "????";
        }
    }

    public void play(Song song) {
        if (player != null) {
            player.dispose();
            createPlayer(song);
            player.play();
            isPlaying = true;
        }
        else {
            createPlayer(song);
            player.play();
            isPlaying = true;
        }
    }

    private void createPlayer(Song s) {
        file = new File(s.getPath());
        media = new Media(file.toURI().toString());
        player = new MediaPlayer(media);
    }

    public void createPlayer(File file) {
        media = new Media(file.toURI().toString());
        player = new MediaPlayer(media);
        player.play();
        isPlaying = true;
    }

    public void getDialogBox(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR!");
        alert.setContentText("You Need To Add Songs First!");
        alert.showAndWait();
    }

    public void seek(Double time) {
        player.seek(player.getTotalDuration().multiply(time / 100));
    }

}
