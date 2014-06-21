package com.diusrex.sleepingdata;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.EditText;

public class DataChangeHandler {
    
    static final String LOG_TAG = "DataChangeHandler";
    static final String CHANGES_FILE = "DataChangePreferences";
    
    final SharedPreferences settings;
    String inputGroupName;
    
    List<ChangeInfo> allChanges;
    
    
    
    List<EditText> inputs;
    
    DataChangeHandler(String inputGroupName, Context appContext)
    {
        this.inputGroupName = inputGroupName;
        
        settings = appContext.getSharedPreferences(CHANGES_FILE, 0);
        
        
        loadExistingDataChanges();
    }
    
    void loadExistingDataChanges()
    {
        allChanges = new ArrayList<ChangeInfo>();
        
        String line = settings.getString(inputGroupName, null);
        
        if (line != null) {
            String[] allWords = line.split(" ");
            int position = 0;
            
            while (position < allWords.length) {
                String id = allWords[position++];
                
                if (id.equals(AddData.IDENTIFIER)) {
                    AddData newChange = new AddData();
                    position = newChange.loadFromArray(allWords, position);
                    allChanges.add(newChange);
                } else if (id.equals(DeleteData.IDENTIFIER)) {
                    DeleteData newChange = new DeleteData();
                    position = newChange.loadFromArray(allWords, position);
                    allChanges.add(newChange);
                }
                else {
                    Log.e(LOG_TAG, "The id '" + id + "' was not recognized");
                }
            }
            
            SharedPreferences.Editor editor = settings.edit();
            
            editor.remove(inputGroupName);
            editor.commit();
        }
    }
    
    public void promptAdded(int position, String dataToAdd)
    {
        allChanges.add(new AddData(dataToAdd, position));
    }
    
    public void promptRemoved(int position) {
        allChanges.add(new DeleteData(position));
    }
    
    public void reset()
    {
        allChanges = new ArrayList<ChangeInfo>();
    }
    
    public void saveDataChanges()
    {
        if (allChanges.size() == 0) {
            return;
        }
        
        String line = "", separator = "";
        
        for (ChangeInfo current : allChanges) {
            line += separator + current.toString();
            separator = " ";
        }
        
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putString(inputGroupName, line);
        editor.commit();
    }
    
    public void applyDataChanges()
    {
        List<String[]> allData;
        
        allData = FileLoader.loadAllData(inputGroupName);
        
        for (int i = 0; i < allData.size(); ++i) {
            for (ChangeInfo current : allChanges) {
                allData.set(i, current.applyToData(allData.get(i)));
            }
        }
        
        allChanges = new ArrayList<ChangeInfo>();
        
        FileSaver.saveAllData(inputGroupName, allData);
    }
}
