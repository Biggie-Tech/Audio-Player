import weka.classifiers.functions.SMO;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.stopwords.WordsFromFile;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.IOException;

/**
 * Created by D on 08/08/2017.
 */
public class ClassifierHandler {
    private static final String STOPWORDS_FILE = "Stopwords (Full).txt";
    private static volatile ClassifierHandler instance = null;
    private StringToWordVector filter;
    private boolean lyricClassBuilt = false;
    private SMO classifierLyric;
    //audio classification variables
    private boolean audioClassBuilt = false;
    private SMO classifierAudio;
    private Instances trainingDataAudio;
    //variables for both classifiers
    private ArffLoader loader;
    private DatabaseHandler databaseHandler;

    private ClassifierHandler() {
    }

    public static ClassifierHandler getInstance() {
        if (instance == null) {
            synchronized (ClassifierHandler.class) {
                if (instance == null) {
                    instance = new ClassifierHandler();
                }
            }
        }
        return instance;
    }

    public void classifySongAudio(Song song) {
        if (!audioClassBuilt) {
            audioClassBuilt = true;
            buildAudioClassifier();
            classifyAudio(song);
        } else {
            classifyAudio(song);
        }
    }

    public void classifySongLyric(Song song) {
        if (!lyricClassBuilt) {
            lyricClassBuilt = true;

            buildLyricClassifier();
            classifyLyric(song);
        } else {
            classifyLyric(song);
        }
    }

    private void classifyAudio(Song song) {
        try {
            databaseHandler = DatabaseHandler.getInstance();
            Instances unlabelledInstancesAudio = databaseHandler.getAudioInstance(song);
            for (int i = 0; i < unlabelledInstancesAudio.numInstances(); i++) {
                double result = classifierAudio.classifyInstance(unlabelledInstancesAudio.get(i));
                String emotion = trainingDataAudio.classAttribute().value((int)result);
                song.setEmotionAudio(emotion);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void classifyLyric(Song song) {
        try {
            databaseHandler = DatabaseHandler.getInstance();
            Instances lyricInstances = databaseHandler.getLyricInstance(song);
            Instances unlabelledInstancesLyric = Filter.useFilter(lyricInstances, filter);
            for (int i = 0; i < unlabelledInstancesLyric.numInstances(); i++) {
                double result = classifierLyric.classifyInstance(unlabelledInstancesLyric.get(i));
                String emotion = unlabelledInstancesLyric.classAttribute().value((int)result);
                song.setEmotionLyric(emotion);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildLyricClassifier() {
        loader = new ArffLoader();
        try {
            loader.setFile(new File("Lyrics Raw (Moody).arff"));
            Instances trainingDataLyrics = loader.getDataSet();
            trainingDataLyrics.setClassIndex(trainingDataLyrics.numAttributes() - 1);
            filter = buildFilter(trainingDataLyrics);
            Instances trainInstancesLyrics = Filter.useFilter(trainingDataLyrics, filter);
            saveModel(trainInstancesLyrics);
            classifierLyric = new SMO();
            classifierLyric.buildClassifier(trainInstancesLyrics);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void buildAudioClassifier() {
        loader = new ArffLoader();
        try {
            loader.setFile(new File("Audio Features (Train).arff"));
            trainingDataAudio = loader.getDataSet();
            trainingDataAudio.setClassIndex(trainingDataAudio.numAttributes() - 1);
            classifierAudio = new SMO();
            classifierAudio.buildClassifier(trainingDataAudio);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private StringToWordVector buildFilter(Instances instances) {
        try {
            filter = new StringToWordVector();
            filter.setInputFormat(instances);
            filter.setMinTermFreq(3);
            filter.setWordsToKeep(2000);
            filter.setTFTransform(true);
            filter.setIDFTransform(true);
            filter.setStopwordsHandler(buildStopwords());
            filter.setTokenizer(buildTokenizer());
            return filter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveModel(Instances instances) {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(instances);
        try {
            saver.setFile(new File("Lyrics (Moody).arff"));
            saver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private WordsFromFile buildStopwords() {
        try {
            WordsFromFile stopwords = new WordsFromFile();
            stopwords.setStopwords(new File(STOPWORDS_FILE));
            System.out.println(stopwords.getStopwords().length());
            return stopwords;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private NGramTokenizer buildTokenizer() {
        NGramTokenizer tokenizer = new NGramTokenizer();
        tokenizer.setNGramMinSize(1);
        tokenizer.setNGramMaxSize(2);
        return tokenizer;
    }
}
