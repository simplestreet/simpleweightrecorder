package com.simplestreet.weightrecord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class EditActivity extends Activity {
	private SimpleDatabaseHelper helper = null;
  	private EditText et_weight;
	private EditText et_fat;
	private DatePicker datepicker;
	private TimePicker timepicker;
	private int id;
	private boolean fat_flag = false;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		
		Intent i = this.getIntent();
		final int pos = i.getIntExtra("position", -1);
		
		// 初期値の設定
		helper = new SimpleDatabaseHelper(this);
		String[] cols = {"_id","fat","weight","ts"};
		SQLiteDatabase db = helper.getReadableDatabase();
    	Cursor cs = db.query("health",cols, null,null,null,null,"ts desc");
    	if (cs.getCount() == 0){
    		db.close();
    		return;
    	}
    	cs.moveToPosition(pos);
    	
    	id = cs.getInt(0);
    	
    	float weight;
    	float fat;
    	weight = cs.getFloat(2);
    	fat = cs.getFloat(1);
    	
    	et_weight = (EditText)findViewById(R.id.editText1);
    	et_fat = (EditText)findViewById(R.id.editText2);
    	
    	et_weight.setText(Float.toString(weight));
    	et_fat.setText(Float.toString(fat));
    	
    	/***** 体脂肪率　非表示 *****/
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
        if(splitStr.length > 2){
            if(Integer.parseInt(splitStr[2]) != 1){
            	fat_flag = false;
            }else{
            	fat_flag = true;
            }
        }else{
        	fat_flag = false;
        }
        //if(Integer.parseInt(splitStr[2]) != 1){
        if(!fat_flag){
	    	et_fat.setVisibility(View.GONE);
	    	TextView tv4 = (TextView)findViewById(R.id.textView4);
	    	tv4.setVisibility(View.GONE);
	    	TextView tv6 = (TextView)findViewById(R.id.textView6);
	    	tv6.setVisibility(View.GONE);
        }
    	String[] strAry = cs.getString(3).split(" ");
    	String[] strDate = strAry[0].split("-");
    	String[] strTime = strAry[1].split(":");
    	
		datepicker = (DatePicker)findViewById(R.id.datePicker1);
		datepicker.updateDate(Integer.parseInt(strDate[0]), Integer.parseInt(strDate[1]) - 1,Integer.parseInt(strDate[2]));
		
		timepicker = (TimePicker)findViewById(R.id.timePicker1);
		timepicker.setCurrentHour(Integer.parseInt(strTime[0]));
		timepicker.setCurrentMinute(Integer.parseInt(strTime[1]));
		
		Button btn = (Button)findViewById(R.id.edit_footer);
		
		btn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//Toast.makeText(v.getContext(), Integer.toString(id), Toast.LENGTH_SHORT).show();
				SQLiteDatabase db = helper.getWritableDatabase();
				ContentValues cv = new ContentValues();
				
	        	String str_fat = et_fat.getText().toString();
	        	String str_wt = et_weight.getText().toString();
	        	if(str_wt.length() == 0){
	        		str_wt = "0";
	        	}
	        	if(str_fat.length() == 0){
	        		str_fat = "0";
	        	}
	        	float flt_wt = Float.parseFloat(str_wt);
	        	float flt_fat = Float.parseFloat(str_fat);
	        	cv.put("weight", flt_wt);
	        	cv.put("fat", flt_fat);
	        	cv.put("ts", String.format("%04d-%02d-%02d %02d:%02d:00",datepicker.getYear(),datepicker.getMonth()+1,datepicker.getDayOfMonth()
	        			,timepicker.getCurrentHour(),timepicker.getCurrentMinute()));
	        	db.update("health", cv,"_id = " + id,null);
				db.close();
					            
				finish();	
			}
		});
    	
		db.close();
	}
}
