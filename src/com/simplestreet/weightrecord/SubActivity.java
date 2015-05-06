package com.simplestreet.weightrecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SubActivity extends Activity {
	@Override
	public void onCreate(Bundle saveInstaneState){
		super.onCreate(saveInstaneState);
		setContentView(R.layout.activity_sub);
		
        //"SimpleWeight.dat"の読み込み
        StringBuffer str = new StringBuffer();
        try{
        	BufferedReader reader = new BufferedReader(
        			new InputStreamReader(openFileInput("SimpleWeight.dat")));
        	while(reader.ready()){
        		str.append(reader.readLine());
        	}
        	reader.close();
        }catch(FileNotFoundException e){
        	e.printStackTrace();
        	return;
        }catch(IOException e){
        	e.printStackTrace();
        	return;
        }
        String[] splitStr = str.toString().split(";");
        EditText et_objective = (EditText)this.findViewById(R.id.et_objective);
    	EditText et_height = (EditText)this.findViewById(R.id.et_height);
    	Switch sw_fat = (Switch)this.findViewById(R.id.fat_switch);
    	et_height.setText(splitStr[0]);
    	et_objective.setText(splitStr[1]);
    	if(splitStr.length > 2){
    		sw_fat.setChecked(Integer.parseInt(splitStr[2]) == 1);
    	}
	}
	public void onClick(View v){
		int ch_fat=0;
		try{
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(
							openFileOutput("SimpleWeight.dat",Context.MODE_PRIVATE)));
			
			EditText et_objective = (EditText)this.findViewById(R.id.et_objective);
			EditText et_height = (EditText)this.findViewById(R.id.et_height);
			Switch sw_fat = (Switch)this.findViewById(R.id.fat_switch);
        	String str_objective = et_objective.getText().toString();
        	String str_height = et_height.getText().toString();
        	if(str_objective.length() == 0 || str_height.length() == 0){
        		writer.close();
        		Toast.makeText(this,"身長と目標体重を入力してください。",Toast.LENGTH_LONG).show();
        		return;
        	}
        	
        	if(sw_fat.isChecked()){
        		ch_fat=1;
        	}
			// save to data file
			writer.write(str_height + ";" + str_objective + ";" + Integer.toString(ch_fat));
			writer.close();
		
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		Intent retIntent = new Intent();
		if(ch_fat == 1){
			retIntent.putExtra("fat_flag",true);
		}else{
			retIntent.putExtra("fat_flag",false);
		}
		
		setResult(0,retIntent);
		finish();
	}
}