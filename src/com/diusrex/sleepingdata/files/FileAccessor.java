package com.diusrex.sleepingdata.files;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.diusrex.sleepingdata.Question;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

public class FileAccessor {
    static private String LOG_TAG = "FileAccessor";

    static final String DATA_FOLDER = "";
    static final String QUESTIONS_FOLDER = "Questions";

    private FileAccessor() {
        
    }
    
    static public class NoAccessException extends Exception {
        private static final long serialVersionUID = 4274163361971136233L;
        public NoAccessException() { super(); }
        public NoAccessException(String message) { super(message); }
        public NoAccessException(String message, Throwable cause) { super(message, cause); }
        public NoAccessException(Throwable cause) { super(cause); }
    }

    public static void changeCategoryName(String oldCategoryName,
            String newCategoryName, Context appContext) {
        List<String[]> allData = FileLoader.loadAllData(oldCategoryName, appContext);
        FileSaver.saveAllData(newCategoryName, allData, appContext);

        List<Question> allQuestions = FileLoader.loadQuestions(oldCategoryName, appContext);
        FileSaver.saveQuestions(newCategoryName, allQuestions, appContext);

        deleteCategory(oldCategoryName, appContext);
    }

    public static void deleteCategory(String categoryName, Context appContext) {
        File dataFile;
        File questionsFile;

        try {
            dataFile = openDataFile(categoryName, appContext);
            questionsFile = openQuestionsFile(categoryName, appContext);
        } catch (IOException | NoAccessException e) {
            return;
        }

        dataFile.delete();
        questionsFile.delete();

        flagFileChanges(dataFile, appContext);
        flagFileChanges(questionsFile, appContext);
    }


    static public File openDataFile(String fileName, Context appContext) throws IOException, NoAccessException
    {
        return openFile(DATA_FOLDER, fileName, appContext);
    }

    static public File openQuestionsFile(String fileName, Context appContext) throws IOException, NoAccessException
    {
        return openFile(QUESTIONS_FOLDER, fileName, appContext);
    }

    static File openFile(String folder, String fileName, Context appContext) throws IOException, NoAccessException
    {
        if (!isExternalStorageAccessable()) {
            throw new NoAccessException();
        }

        File file = new File(Environment.getExternalStoragePublicDirectory("Save Data/" + folder), fileName + ".txt");

        if (!file.exists())
        {
            file.getParentFile().mkdirs();

            Log.w(LOG_TAG, "File is known to not exists");

            if (!file.createNewFile())
            {
                Log.e(LOG_TAG, "File was unable to be created");
            } else {
                flagFileChanges(file, appContext);
            }
        }

        return file;
    }

    static public void flagFileChanges(File fileChanged, Context appContext)
    {	    
        MediaScannerConnection.scanFile(appContext,
                new String[] { fileChanged.getAbsolutePath() }, null, null);
    }

    static private boolean isExternalStorageAccessable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }



}
