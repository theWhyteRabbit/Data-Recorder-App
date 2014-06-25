package com.diusrex.sleepingdata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.diusrex.sleepingdata.dialogs.ConfirmDialogFragment;
import com.diusrex.sleepingdata.dialogs.ConfirmListener;
import com.diusrex.sleepingdata.dialogs.NameSetterDialogFragment;
import com.diusrex.sleepingdata.dialogs.NameSetterListener;

public class InputGroupActivity extends Activity implements ConfirmListener, NameSetterListener {
    static final public String INPUT_GROUP_NAME = "InputGroupName";
    static final public String NEW_INPUT_GROUP = "NewInputGroup";
    
    static final String LOG_TAG = "InitialPromptInputActivity";
    
    static final int DELETE_CODE = 5;
    
    String inputGroupName;
    
    boolean isNew;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_group);
        
        Intent intent = getIntent();
        
        isNew = intent.getBooleanExtra(NEW_INPUT_GROUP, false);
        
        inputGroupName = intent.getStringExtra(INPUT_GROUP_NAME);
        
        if (isNew) {
            inputGroupName = "";
            changeInputGroupName();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        Bundle args = new Bundle();
        args.putString(INPUT_GROUP_NAME, inputGroupName);
        getIntent().putExtras(args);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        setUpInformation();
    }
    
    // TODO: Split this up into smaller functions? (Put them at bottom)
    void setUpInformation()
    {
        setInputGroupNameTV();
        
        int numberOfPrompts = FileLoader.numberOfPrompts(inputGroupName);
        
        String numberOfPromptsOutput;
        
        if (numberOfPrompts == 1) {
            numberOfPromptsOutput = getString(R.string.input_group_num_prompts_single);
        } else {
            numberOfPromptsOutput = getString(R.string.input_group_num_prompts_multiple);
            numberOfPromptsOutput = String.format(numberOfPromptsOutput, numberOfPrompts);
        }
        
        TextView numberPromptsInfo = (TextView) findViewById(R.id.numberOfPrompts);
        numberPromptsInfo.setText(numberOfPromptsOutput);
        
        
        int numberOfDataRows = FileLoader.numberOfDataRows(inputGroupName);
        String numberOfDataRowsOutput;
        
        if (numberOfDataRows == 1) {
            numberOfDataRowsOutput = getString(R.string.input_group_num_data_rows_single);
        } else {
            numberOfDataRowsOutput = getString(R.string.input_group_num_data_rows_multiple);
            numberOfDataRowsOutput = String.format(numberOfDataRowsOutput, numberOfDataRows);
        }

        TextView numberDataRowsInfo = (TextView) findViewById(R.id.numberOfDataRows);
        numberDataRowsInfo.setText(numberOfDataRowsOutput);
    }
    
    void setInputGroupNameTV()
    {
        String groupNameInfo = getString(R.string.current_input_group);

        TextView inputGroupNameTV = (TextView) findViewById(R.id.inputGroupName);
        inputGroupNameTV.setText(String.format(groupNameInfo, inputGroupName));
        inputGroupNameTV.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                changeInputGroupName();
            }
        });
    }
    
    void changeInputGroupName() {
        DialogFragment fragment = NameSetterDialogFragment.newInstance(inputGroupName, (NameSetterListener) this); 
        fragment.show(getFragmentManager(), "dialog");
    }
    
    @Override
    public void nameChanged(String newName) {
        if (!newName.equals(inputGroupName)) {
            MainActivity.changeInputGroupName(inputGroupName, newName, (Context) this);
            inputGroupName = newName;
            setInputGroupNameTV();
            
            if (isNew) {
                runPromptSetting();
            }
        } else if (isNew) {
            finish();
        }
    }
    
    public void changeButtonClicked(View view) {
        runPromptSetting();
    }
    
    void runPromptSetting() {
        Intent intent = new Intent(this, PromptSettingActivity.class);
        intent.putExtra(PromptSettingActivity.INPUT_GROUP_NAME, inputGroupName);

        startActivity(intent);
    }
    
    public void inputButtonClicked(View view) {
        if (FileLoader.loadPrompts(inputGroupName).size() != 0) {
            
        Intent intent = new Intent(this, InputDataActivity.class);
        intent.putExtra(InputDataActivity.INPUT_GROUP_NAME, inputGroupName);

        startActivity(intent);
        } else {
            createErrorDialog(getString(R.string.no_prompts));
        }
    }

    public void deleteButtonClicked(View view) {
        DialogFragment fragment = ConfirmDialogFragment.newInstance(getString(R.string.confirm_delete), DELETE_CODE, (ConfirmListener) this);
        fragment.show(getFragmentManager(), "dialog");
    }
    
    @Override
    public void wasConfirmed(int code) {
        switch (code) {
        case DELETE_CODE:
            deleteInputGroup();
            break;
            
        default:
            break;
        }
    }
    
    void deleteInputGroup() {
        MainActivity.deleteInputGroup(inputGroupName, (Context) this);
        
        finish();
    }

    @Override
    public void createErrorDialog(String output) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setMessage(output);
        builder.setPositiveButton(getString(android.R.string.ok), new AlertDialog.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        builder.show();
    }
}
