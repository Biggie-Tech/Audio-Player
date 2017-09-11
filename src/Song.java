/**
 * Created by D on 26/07/2017.
 */
public class Song {

    private int id;
    private String path;
    private String title;
    private String artist;
    private String album;
    private String emotionAudio;
    private String emotionLyric;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getEmotionAudio() {
        return emotionAudio;
    }

    public void setEmotionAudio(String emotion) {
        this.emotionAudio = emotion;
    }

    public String getEmotionLyric() {
        return emotionLyric;
    }

    public void setEmotionLyric(String emotionLyric) {
        this.emotionLyric = emotionLyric;
    }

    public Song(String path) {
        this.path = path;
    }

}
