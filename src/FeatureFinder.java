import com.omt.lyrics.SearchLyrics;
import com.omt.lyrics.beans.Lyrics;
import com.omt.lyrics.beans.LyricsServiceBean;
import com.omt.lyrics.beans.SearchLyricsBean;
import com.omt.lyrics.exception.SearchLyricsException;
import com.omt.lyrics.util.Sites;
import jAudioFeatureExtractor.ACE.DataTypes.Batch;
import jAudioFeatureExtractor.DataModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by D on 28/07/2017.
 */
public class FeatureFinder {

    private static final String FEATURE_FILE = "features.xml";
    private static final String SETTINGS_FILE = "settings(full).xml";
    private static final String FK_OUTPUT_FILE = "feature_definitions.xml";
    private static final String FV_OUTPUT_FILE = "feature_values.arff";

    private Batch extractor;

    public FeatureFinder() {
    }

    public ArrayList<Double> extractAudioFeatures(File file) {
        createAudioFeatureExtractor();
        try {
            File[] files = new File[]{file};
            extractor.setRecordings(files);
            extractor.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        double[][][] results = extractor.getResults();
        ArrayList<Double> result = new ArrayList<Double>();
        for (int i = 0; i < results[0].length; i++) {
            for (int j = 0; j < results[0][i].length; j++) {
                result.add(results[0][i][j]);
            }
        }
        return result;
    }

    private void createAudioFeatureExtractor() {
        extractor = new Batch(FEATURE_FILE, null);
        System.out.println(extractor.getName());
        DataModel dm = extractor.getDataModel();
        try {
            extractor.getAggregator();
            extractor.setSettings(SETTINGS_FILE);
            OutputStream valsavepath = new FileOutputStream(FV_OUTPUT_FILE);
            OutputStream defsavepath = new FileOutputStream(FK_OUTPUT_FILE);
            dm.featureKey = defsavepath;
            dm.featureValue = valsavepath;
        } catch (Exception e) {

        }
        extractor.setDataModel(dm);
    }

    public String findLyric(String title, String artist) {
        String lyrics = "";
        SearchLyrics searchLyrics = new SearchLyrics();

        LyricsServiceBean serviceBean = new LyricsServiceBean();
        serviceBean.setSongName(title);
        serviceBean.setSongArtist(artist);

        SearchLyricsBean searchBean = new SearchLyricsBean();
        searchBean.setSongName(title);
        searchBean.setSongArtist(artist);

        try {
            List<Lyrics> lyricsList = searchLyrics.searchLyrics(serviceBean);
            for (Lyrics l : lyricsList) {
                lyrics = lyrics + l.getText();
            }
            if (lyrics.equals("")) {
                searchBean.setSites(Sites.METROLYRICS);
                lyricsList = searchLyrics.searchLyrics(searchBean);
                for (Lyrics l : lyricsList) {
                    lyrics = lyrics + l.getText();
                    //lyrics.replaceAll("advisory  the following lyrics contain explicit language:","");
                }
                if (lyrics.equals("")) {
                    searchBean.setSites(Sites.AZLYRICS);
                    lyricsList = searchLyrics.searchLyrics(searchBean);
                    for (Lyrics l : lyricsList) {
                        lyrics = lyrics + l.getText();
                        //lyrics.replaceAll("advisory  the following lyrics contain explicit language:","");
                    }
                    /*if (lyrics.equals("")) {
                        searchBean.setSites(Sites.SONGMEANINGS);
                        lyricsList = searchLyrics.searchLyrics(searchBean);
                        for (Lyrics l : lyricsList) {
                            lyrics = lyrics + l.getText();
                        }
                    }*/
                }
            }
        } catch (SearchLyricsException e) {
            System.out.println(artist);
            System.out.println(title);
            e.printStackTrace();
        }
        cleanLyric(lyrics);
        return lyrics;
    }

    private void cleanLyric(String lyrics) {
        deAccent(lyrics);
        lyrics = lyrics.toLowerCase().replaceAll("\\(feat.*?\\)" ,"");
        lyrics = lyrics.toLowerCase().replaceAll("\\(w/.*?\\)" ,"");
        lyrics = lyrics.replaceAll("featuring." ,"");
        lyrics = lyrics.replaceAll("  ", "");
        lyrics = lyrics.replaceAll("\\.", "");
        lyrics = lyrics.replaceAll("-", "");
        lyrics = lyrics.replaceAll(",", "");
        lyrics = lyrics.replaceAll("'", "");
        lyrics = lyrics.replaceAll("\\?", "");
        lyrics = lyrics.replaceAll("\\[(.*?)\\]", "");
        lyrics = lyrics.replaceAll("[^a-zA-Z0-9\\\\s+]\"", "");
        lyrics = lyrics.replaceAll("\\((lyric.*?)\\)", "");
        lyrics = lyrics.replaceAll("\\((0-9.*?)\\)", "");
        lyrics = lyrics.replaceAll("!", "");
        lyrics = lyrics.replaceAll("\\(", "");
        lyrics = lyrics.replaceAll("\\)", "");
        lyrics = lyrics.replaceAll("i m","im");
        lyrics = lyrics.replaceAll("i ll","ill");
        lyrics = lyrics.replaceAll("i ve","ive");
        lyrics = lyrics.replaceAll("ain t","aint");
        lyrics = lyrics.replaceAll("i m","im");
        lyrics = lyrics.replaceAll("i m","im");
        lyrics = lyrics.replaceAll("&amp","&");
        lyrics.replaceAll("Advisory - the following lyrics contain explicit language:","");
    }

    private void deAccent(String string) {
        String nfdNormalizedString = Normalizer.normalize(string, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        pattern.matcher(nfdNormalizedString).replaceAll("");
    }
}
