package com.simplestreet.weightrecord;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
//import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends Activity implements ActionBar.TabListener {

    ViewPager mViewPager;
    final int SETTING_CODE = 1;
    final int FIRST_SETTING = 2;
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	boolean ret = true;
    	switch(item.getItemId()){
    	case R.id.action_settings:
    		//Toast.makeText(this,"押されたよ", Toast.LENGTH_LONG).show();
    		Intent intent = new Intent(getApplicationContext(),SubActivity.class);
    		startActivityForResult(intent,SETTING_CODE);
    		
    		break;
    	case R.id.action_finish:
    		AlertDialogFragment newFragment = new AlertDialogFragment();
    		newFragment.show(getFragmentManager(),"showAlertDialog");
    		break;
    	case R.id.action_repeat:
    		//Toast.makeText(this, "t",Toast.LENGTH_SHORT).show();
    		
    		//グラフの更新
    		ViewPagerAdapter va = (ViewPagerAdapter)mViewPager.getAdapter();
            LinearLayout chartContainer = (LinearLayout)findViewById(R.id.chart);
    		chartContainer.removeAllViews();
    		SimpleFragment sf = (SimpleFragment)va.instantiateItem(mViewPager, 2/*グラフのposition*/);
    		sf.changeFatWeightChart();
    		sf.updateChart();
    		break;
    	}
    	return ret;
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
		ViewPager vp = (ViewPager) findViewById(R.id.pager);
		ViewPagerAdapter adapter = (ViewPagerAdapter)vp.getAdapter();
		
    	if(requestCode == SETTING_CODE){
    		if(data == null){
    			return;
    		}
    		SimpleFragment3 sf3 = (SimpleFragment3)adapter.instantiateItem(vp, 1/*最新情報のposition*/);
    		SimpleFragment2 sf2 = (SimpleFragment2)adapter.instantiateItem(vp,0);
    		SimpleFragment sf = (SimpleFragment)adapter.instantiateItem(vp,2);
    		if(data.getBooleanExtra("fat_flag",false) != true){
    			sf.setWeightChart();
    		}
    		
    		/*if( data.getIntExtra("fat_flag",0) == 1 ) {
    			setFragmentFatFlag(true);
    		}else{
    			setFragmentFatFlag(false);
    		}*/
    		sf3.listviewUpdate();//最新情報の更新
    		if(mViewPager.getCurrentItem() != 2){ //グラフが表示されているときはデータ一覧のフラグメントが削除されているため。
    			sf2.updateList();//データ一覧の更新
    		}
    		invalidateOptionsMenu();
    	}
    }
    @Override
    public void onResume(){
    	super.onResume();
    	
    	SharedPreferences preference;
    	Editor editor;
    	
        //プリファレンスの準備  
        preference = getSharedPreferences("SimpleWeightRecorder", MODE_PRIVATE);  
        editor = preference.edit();  
   
        if (preference.getBoolean("Launched", false)==false) {  
            //初回起動時の処理  
   
            //プリファレンスの書き変え  
            editor.putBoolean("Launched", true);  
            editor.commit();
            Intent intent = new Intent(getApplicationContext(),SubActivity.class);
    		//startActivityForResult(intent,FIRST_SETTING);
            startActivity(intent);
            //Toast.makeText(this,"初回起動だよ",Toast.LENGTH_LONG).show();
        } else {  
            //二回目以降の処理  
   
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        
        // タブを使うときはNAVIGATION_MODE_TABSをセットする
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        //タイトル名を非表示にする。
        //actionBar.setDisplayShowTitleEnabled(false);
        
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());

        // ViewPagerにアダプターをセット
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);

        // ViewPagerのページ数だけタブを追加
        for (int i = 0; i < adapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(adapter.getPageTitle(i))
                    .setTabListener(this));
        }

        // ViewPagerのページが切り替わったら、選択されているタブの位置を変更
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                        
                        /*if(position == 0){
                        	Toast t = Toast.makeText(getApplicationContext(), "長押しで  編集 or 削除",Toast.LENGTH_SHORT);
                        	t.setGravity(Gravity.CENTER_VERTICAL,0,0);
                        	t.show();
                        }*/
                        invalidateOptionsMenu();
                    }
                });
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
        int pageNum = mViewPager.getCurrentItem();
        MenuItem item = menu.findItem(R.id.action_repeat);
        if(pageNum== 2 /*グラフ*/){
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
            	return true;
            }catch(IOException e){
            	e.printStackTrace();
            	return true;
            }
			
            String[] splitStr = str.toString().split(";");
            
            if(splitStr.length > 2){
	            if(Integer.parseInt(splitStr[2]) != 1){
	            	item.setVisible(false);
	            }else{
	            	item.setVisible(true);
	            }
            }else{
            	item.setVisible(false);
            }
        }else{
        	item.setVisible(false);
        }
        return true;
    }
    
    private boolean FinishFlag;
    @Override
    public void onBackPressed(){
		if (FinishFlag) {
			/*finish();*/
			moveTaskToBack(true);
		} else {
			Toast.makeText(this, "もう一度バックボタンを押すと終了します。", Toast.LENGTH_SHORT).show();
			FinishFlag = true;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					FinishFlag = false;
				}
			}, 2000/*2秒*/);
		}
    }
    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	if(position == 1){
        		return SimpleFragment3.getInstance(position);
        	}else if(position == 2){
        		return SimpleFragment.getInstance(position);
        	}else{
        		return SimpleFragment2.getInstance(position);
        	}
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
            case 0:
                return getString(R.string.title_section2).toUpperCase(l);
            case 1:
                return getString(R.string.title_section4).toUpperCase(l);
            case 2:
                return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
        // タブを選択されたら、ViewPagerのページをその位置に移動
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //最新情報
    public static class SimpleFragment3 extends Fragment {
    	boolean fat_flag = false;
    	SimpleDatabaseHelper helper = null;
    	ArrayList<HashMap <String,String>> map_lv;

        public static SimpleFragment3 getInstance(int position) {
            SimpleFragment3 f = new SimpleFragment3();
            Bundle args = new Bundle();
            args.putInt("position", position);
            f.setArguments(args);
            return f;
        }
//        LinearLayout layout_ad;//広告表示用スペース
        AdView adView;
        
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            map_lv = new ArrayList<HashMap<String,String>>();
            helper = new SimpleDatabaseHelper(getActivity());

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
       		return inflater.inflate(R.layout.fragment_simple3, container, false);
        }
        public ListView m_lv;
        public ViewPager m_ViewPager;
        
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            adView = (AdView)getActivity().findViewById(R.id.adView);/*new AdView(getActivity());*/
            
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
			/**/
            Button btn = (Button)view.findViewById(R.id.button2);
            btn.setOnClickListener(
            			new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								m_lv = (ListView)getActivity().findViewById(R.id.lv);
								m_ViewPager = (ViewPager) getActivity().findViewById(R.id.pager);
								Resources resources = getResources();
								Configuration config = resources.getConfiguration();
								DisplayMetrics metrics = resources.getDisplayMetrics(); 
								int dialogWidth = (int) (metrics.widthPixels * 0.8);
								int dialogHeight;
								
								switch(config.orientation){
								case Configuration.ORIENTATION_LANDSCAPE:
								    dialogHeight = (int)(metrics.heightPixels * 0.9);	
									break;
								case Configuration.ORIENTATION_PORTRAIT:
								default :
									dialogHeight = (int)(metrics.heightPixels * 0.6);		
									break;
								}
								
								final Dialog d = new Dialog(getActivity());
								d.setContentView(R.layout.fragment_simple1);
								d.setTitle("データ入力");
								WindowManager.LayoutParams lp = d.getWindow().getAttributes();
								lp.width = dialogWidth;
								lp.height = dialogHeight;
								d.getWindow().setAttributes(lp);
								
					            StringBuffer str = new StringBuffer();
					            try{
					            	BufferedReader reader = new BufferedReader(
					            			new InputStreamReader(getActivity().openFileInput("SimpleWeight.dat")));
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
					            if(!fat_flag){
									EditText et_d_fat = (EditText)d.findViewById(R.id.et_fat);
									et_d_fat.setVisibility(View.INVISIBLE);
									TextView tv1_d_fat = (TextView)d.findViewById(R.id.tv1_fat);
									tv1_d_fat.setVisibility(View.INVISIBLE);
									TextView tv2_d_fat = (TextView)d.findViewById(R.id.tv2_fat);
									tv2_d_fat.setVisibility(View.INVISIBLE);
					            }
								
								Button btn = (Button)d.findViewById(R.id.button1);
					            btn.setOnClickListener(
				            			new View.OnClickListener() {
											@Override
											public void onClick(View v) {
												
												d.dismiss();
												// TODO Auto-generated method stub
												SQLiteDatabase db_rw = helper.getWritableDatabase();
												ContentValues cv = new ContentValues();
												
												EditText et_wt;
												String str_fat;
												String str_wt;
												EditText et_fat;
												if(!fat_flag){  //体重のみ
										        	//EditText et_fat = (EditText)d.findViewById(R.id.et_fat);
										        	et_wt = (EditText)d.findViewById(R.id.et_wt);
										        	str_fat = "0"/*et_fat.getText().toString()*/;
										        	str_wt = et_wt.getText().toString();
										        	if(str_wt.length() == 0 /*|| str_fat.length() == 0*/){
										        		db_rw.close();
										        		return;
										        	}
												}else{ //体脂肪つき
										        	et_fat = (EditText)d.findViewById(R.id.et_fat);
										        	et_wt = (EditText)d.findViewById(R.id.et_wt);
										        	str_fat = et_fat.getText().toString();
										        	str_wt = et_wt.getText().toString();
										        	if(str_wt.length() == 0 || str_fat.length() == 0){
										        		db_rw.close();
										        		return;
										        	}
												}

									        	float flt_wt = Float.parseFloat(str_wt);
									        	float flt_fat = Float.parseFloat(str_fat);
									        	
									        	cv.put("weight", flt_wt);
									        	cv.put("fat", flt_fat);
									        	db_rw.insert("health", null, cv);
									        	db_rw.close();
									        	
									        	//最新情報更新
									        	listviewUpdate();
									        	
									            //データ一覧の更新
									    		ViewPager vp = (ViewPager) getActivity().findViewById(R.id.pager);
									    		ViewPagerAdapter va = (ViewPagerAdapter)vp.getAdapter();
									    		SimpleFragment2 sf2 = (SimpleFragment2)va.instantiateItem(vp, 0/*データ一覧のposition*/);
									    		sf2.updateList();   
									            
									            //グラフの更新
									            LinearLayout chartContainer = (LinearLayout)getActivity().findViewById(R.id.chart);
							            		chartContainer.removeAllViews();
									    		//ViewPager vp = (ViewPager)getActivity().findViewById(R.id.pager);
									    		//ViewPagerAdapter va = (ViewPagerAdapter)vp.getAdapter();
									    		SimpleFragment sf = (SimpleFragment)va.instantiateItem(vp, 2/*グラフのposition*/);
									    		sf.updateChart(); 
									            /*****************************/
										    	Toast tst = Toast.makeText(getActivity(), "最新情報が更新されました。", Toast.LENGTH_LONG);
										    	tst.setGravity(Gravity.CENTER, 0, 0);
										    	tst.show();
										}
				            		});
								
								d.show();
							}
            			});
            
            listviewUpdate();            
        }
        private void listviewUpdate()
        {
        	if(helper == null){
        		return;
        	}

        	
        	/***** データベース検索処理 *****/
	        //"SimpleWeight.dat"の読み込み
        	SQLiteDatabase db = helper.getReadableDatabase();
	        StringBuffer str = new StringBuffer();
	        try{
	        	BufferedReader reader = new BufferedReader(
	        			new InputStreamReader(getActivity().openFileInput("SimpleWeight.dat")));
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
			String[] cols = {"_id","weight","ts","fat"};
			Cursor cs = db.query("health", cols,null, null, null, null, "ts desc");
        	if (cs.getCount() == 0){
        		db.close();
        		return;
        	}
        	float recent_weight,last_weight,recent_fat;
        	
        	cs.moveToFirst();
            ArrayList<HashMap <String,String>> map = new ArrayList
            		<HashMap<String,String>>();
            HashMap<String,String> item_ts = new HashMap<String,String>();
            HashMap<String,String> item_weight = new HashMap<String,String>();
            HashMap<String,String> item_fat = new HashMap<String,String>();
            HashMap<String,String> item_height = new HashMap<String,String>();
            HashMap<String,String> item_objective = new HashMap<String,String>();
            HashMap<String,String> item_total = new HashMap<String,String>();
            HashMap<String,String> item_bmi = new HashMap<String,String>();
            
            item_ts.put("recent_item","更新日時");
            item_ts.put("recent_number",cs.getString(2).substring(5, 16));
            map.add(item_ts);
            
            item_height.put("recent_item","身長");
            item_height.put("recent_number",String.format(splitStr[0] + " cm"));
            map.add(item_height);
            
            recent_weight=cs.getFloat(1);
            item_weight.put("recent_item","体重");
            item_weight.put("recent_number",String.format("%.1f kg", recent_weight));
            map.add(item_weight);
        
            if(fat_flag){
            	recent_fat=cs.getFloat(3);
	            item_fat.put("recent_item","体脂肪率");
	            String tmp_str = String.format("%.1f ", recent_fat) + "%";
	            item_fat.put("recent_number",tmp_str);
	            map.add(item_fat);
            }
            item_objective.put("recent_item","目標体重");
            item_objective.put("recent_number",String.format(splitStr[1] + " kg"));
            map.add(item_objective);
            
            cs.moveToLast();
            last_weight=cs.getFloat(1);
            item_total.put("recent_item","総減量");
            if( (recent_weight - last_weight) > 0 ){
            	item_total.put("recent_number",String.format("+%.1f kg", recent_weight - last_weight));
            }else{
            	item_total.put("recent_number",String.format("%.1f kg", recent_weight - last_weight));
            }
            map.add(item_total);

            float f_bmi,f_height;
            f_height = Float.valueOf(splitStr[0]) / 100;
            f_bmi = recent_weight/(f_height * f_height);
            item_bmi.put("recent_item","BMI値");
            item_bmi.put("recent_number",String.format("%.1f", f_bmi));
            map.add(item_bmi);
            
            SimpleAdapter adapter = new SimpleAdapter(getActivity(),map,R.layout.list_item2,
            		new String[] {"recent_item","recent_number"},
            		new int[] {R.id.recent_item,R.id.recent_number}
            );
            ListView lv2 = (ListView)getActivity().findViewById(R.id.lv2);
            
            lv2.setAdapter(null);
            lv2.setAdapter(adapter);
            db.close();

        }
    }
    //グラフページ
    public static class SimpleFragment extends Fragment {
    	SimpleDatabaseHelper helper = null;
    	boolean fat_flag = false;
        public static SimpleFragment getInstance(int position) {
            SimpleFragment f = new SimpleFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            f.setArguments(args);
            return f;
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            helper = new SimpleDatabaseHelper(getActivity());
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
       		//return inflater.inflate(R.layout.fragment_simple, container, false);
        	return inflater.inflate(R.layout.main_chart, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            
            updateDate();  
            updateChart();

            ImageButton ib_p = (ImageButton)getActivity().findViewById(R.id.chart_button1);
            ImageButton ib_n = (ImageButton)getActivity().findViewById(R.id.chart_button2);
          
            ib_p.setOnClickListener(new View.OnClickListener(){
            	@Override
                public void onClick(View v) {
            		dateCnt-=1;
            		LinearLayout chartContainer = (LinearLayout)getActivity().findViewById(R.id.chart);
            		chartContainer.removeAllViews();
            		updateChart();
            		updateDate();                    
                }
            });
            ib_n.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					dateCnt+=1;
            		LinearLayout chartContainer = (LinearLayout)getActivity().findViewById(R.id.chart);
            		chartContainer.removeAllViews();
					updateChart();
					updateDate();
				}
			});
        }
        private int dateCnt=0;
        public void updateDate(){
        	Calendar calendar = getTargetCal();
        	int month = calendar.get(Calendar.MONDAY)+1;
        	int day = calendar.get(Calendar.DAY_OF_MONTH);
        	
        	calendar.add(Calendar.DAY_OF_YEAR, -6);
        	int year_p = calendar.get(Calendar.YEAR);
        	int month_p = calendar.get(Calendar.MONDAY)+1;
        	int day_p = calendar.get(Calendar.DAY_OF_MONTH);
        	
        	TextView tv = (TextView)getActivity().findViewById(R.id.chart_textView1);
        	tv.setText(String.format("%d/%02d/%02d - %02d/%02d",year_p, month_p,day_p,month,day));
        	
        }
        private Calendar getTargetCal(){
        	Calendar calendar = Calendar.getInstance();
        	if(dateCnt > 0){
        		calendar.add(Calendar.DAY_OF_YEAR, (dateCnt * 7) );
        	}else if(dateCnt < 0){
        		calendar.add(Calendar.DAY_OF_YEAR, (dateCnt * 7) );
        	}
        	return calendar;
        }
        private boolean fat_view = false;
        public void changeFatWeightChart(){
        	fat_view = !fat_view;
        }
        public void setWeightChart(){
        	fat_view = false;
        }
        public void updateChart() {
        	XYSeries series; //体重用データ
        	XYSeries series2; //目標体重用データ
        	
           	// 体脂肪率　非表示
        	if(fat_view == false){
	            series = this.makeSeries();
	            
	            XYMultipleSeriesDataset dataset = this.makeDataset(series);
	           
	            series2 = this.makeObjectiveSeries();
	            dataset.addSeries(series2);
	
	            XYMultipleSeriesRenderer renderer = this.makeRenderer();
	            double diff = series.getMaxY() - series.getMinY();
	            if(diff < 1){
	            	diff = 1;
	            }
	            diff *= 0.1;
	            renderer.setYAxisMax(series.getMaxY() + diff);
	            renderer.setYAxisMin(series.getMinY() - diff);
	            LinearLayout chartContainer = (LinearLayout)getActivity().findViewById(R.id.chart);
	            GraphicalView graph = ChartFactory.getLineChartView(getActivity(), dataset, renderer);
	            chartContainer.addView(graph);
        	}else{// 体脂肪率　表示
	            series = this.makeSeries();
	            
	            XYMultipleSeriesDataset dataset = this.makeDataset(series);
	           
	            //series2 = this.makeObjectiveSeries();
	            //dataset.addSeries(series2);
	
	            XYMultipleSeriesRenderer renderer = this.makeFatRenderer();
	            double diff = series.getMaxY() - series.getMinY();
	            if(diff < 1){
	            	diff = 1;
	            }
	            diff *= 0.1;
	            renderer.setYAxisMax(series.getMaxY() + diff);
	            renderer.setYAxisMin(series.getMinY() - diff);
	            LinearLayout chartContainer = (LinearLayout)getActivity().findViewById(R.id.chart);
	            GraphicalView graph = ChartFactory.getLineChartView(getActivity(), dataset, renderer);
	            chartContainer.addView(graph);        		
        	}
        }
        XYSeries makeObjectiveSeries(){
	        StringBuffer str = new StringBuffer();
	        try{
	        	BufferedReader reader = new BufferedReader(
	        			new InputStreamReader(getActivity().openFileInput("SimpleWeight.dat")));
	        	while(reader.ready()){
	        		str.append(reader.readLine());
	        	}
	        	reader.close();
	        }catch(FileNotFoundException e){
	        	e.printStackTrace();
	        	return null;
	        }catch(IOException e){
	        	e.printStackTrace();
	        	return null;
	        }
	        String[] splitStr = str.toString().split(";");
	        
        	XYSeries series = new XYSeries("Objective");
            series.add(-1, Float.valueOf(splitStr[1]));
            series.add(7, Float.valueOf(splitStr[1]));
            
            return series;
        }
        
        XYSeries makeSeries() {
       	 
        	float[] weightArray = new float[7];
        	
        	SQLiteDatabase db = helper.getReadableDatabase();
        	String[] cols = {"_id","fat","weight","ts"};
        	String[] targetDate = new String[2];
        	Calendar calSQL = getTargetCal();
        	targetDate[1] = String.format("%d-%02d-%02d 23:59:59",calSQL.get(Calendar.YEAR),
        			calSQL.get(Calendar.MONTH)+1,calSQL.get(Calendar.DAY_OF_MONTH));
        	calSQL.add(Calendar.DAY_OF_YEAR, -6);
        	targetDate[0] = String.format("%d-%02d-%02d",calSQL.get(Calendar.YEAR),
        			calSQL.get(Calendar.MONTH)+1,calSQL.get(Calendar.DAY_OF_MONTH));
        	Cursor cs = db.query("health",cols, "ts between ? and ?",targetDate,null,null,"ts desc");
        	
        	String strCal[] = new String[7];
        	Calendar cal = getTargetCal();
        	for(int i =0 ; i < 7; i++,cal.add(Calendar.DAY_OF_YEAR,-1)){
        		strCal[i] = String.format("%d-%02d-%02d",cal.get(Calendar.YEAR),
        				cal.get(Calendar.MONTH)+1,cal.get(Calendar.DAY_OF_MONTH));
        	}
        	int targetNum=2; // デフォルト　体重
        	if(fat_view){
        		targetNum = 1; // 体脂肪
        	}else{
        		targetNum = 2; //　体重
        	}
        	//cs.moveToNext();
        	if(cs.getCount() != 0){
        		int i = 0;
        		boolean x = cs.moveToNext();
        		while(x){
        			if(i < 7){
		        		if(strCal[i].equals(cs.getString(3).substring(0, 10))){/*等しい場合*/
		        			//weightArray[6-i]= cs.getFloat(2);
		        			weightArray[6-i]= cs.getFloat(targetNum);
		        			x = cs.moveToNext();
		        			i++;
		        		}else if(strCal[i].compareTo(cs.getString(3).substring(0, 10)) < 0){
		        			x = cs.moveToNext();
		        		}else{
		        			i++;
		        		}
        			}
	        	}
        	}
        	
        	db.close();
        	
        	/*if (weightArray[0] == 0.0f){
        	Toast.makeText(getActivity(), String.format("%s", cs.getString(3)), Toast.LENGTH_SHORT).show();
        	cs.moveToLast();
        	Toast.makeText(getActivity(), String.format("%d  : %s",cs.getCount(), cs.getString(3)),Toast.LENGTH_SHORT).show();
        	}*/
        	
            XYSeries series = new XYSeries("TEST");
            double x[] = new double[7];
            double y[] = new double[7];
     
            for (int i = 0; i < 7; i++) {
                x[i] = i;
                y[i] = (double)weightArray[i]/*i * i*/;
            }
            for (int i = 0; i < 7; i++) {
            	if(y[i] == 0){
            		continue;
            	}
                series.add(x[i], y[i]);
            }
            return series;
     
        }
        XYMultipleSeriesDataset makeDataset(XYSeries series) {
        	 
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
            dataset.addSeries(series);
            return dataset;
        }
        XYMultipleSeriesRenderer makeFatRenderer() {
            
            XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
            XYSeriesRenderer r = new XYSeriesRenderer();
            
            /* weight graph setting*/
            r.setColor(Color.parseColor("#ffbb33"));
            r.setLineWidth(3);
            r.setPointStyle(PointStyle.CIRCLE);
            r.setFillPoints(true);
            
            renderer.setPointSize(12);
            renderer.setPanEnabled(false,true);
            renderer.setZoomEnabled(false, true);
            renderer.setXAxisMax(6.5);
            renderer.setXAxisMin(-0.5);
            renderer.setYTitle("体脂肪");
            //renderer.setAxisTitleTextSize(10);
            renderer.setAxisTitleTextSize(35);
            renderer.setLabelsTextSize(30);
            renderer.setXLabels(0);
            
            /*X軸　日付の入力*/
            Calendar calendar = getTargetCal();
            for(int i = 6;i >=0;i--,calendar.add(Calendar.DAY_OF_YEAR, -1)){
                renderer.addXTextLabel(i,String.format("%02d/%02d",calendar.get(Calendar.MONTH)+1 ,
                		calendar.get(Calendar.DAY_OF_MONTH)));
            }
            renderer.setApplyBackgroundColor(true);
            renderer.setBackgroundColor(Color.BLACK);
            renderer.setShowGrid(true);
            renderer.addSeriesRenderer(r);
            renderer.setShowLegend(false);
            
            int[] margin = {10,55,10,55};
            renderer.setMargins(margin);
            /*renderer.setMarginsColor(Color.parseColor("#FF0000"));*/
            return renderer;
        }
        XYMultipleSeriesRenderer makeRenderer() {
     
            XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
            XYSeriesRenderer r = new XYSeriesRenderer();
            XYSeriesRenderer r2 = new XYSeriesRenderer();
            
            /* weight graph setting*/
            r.setColor(Color.parseColor("#33b5e5")/*Color.RED*/);
            r.setLineWidth(3);
            r.setPointStyle(PointStyle.CIRCLE);
            r.setFillPoints(true);
            
            /* objective weight setting*/
            r2.setColor(Color.parseColor("#ff4444"));
            r2.setLineWidth(3);
            r2.setFillPoints(true);
            r2.setStroke(BasicStroke.DASHED);
            
            renderer.setPointSize(12);
            renderer.setPanEnabled(false,true);
            renderer.setZoomEnabled(false, true);
            renderer.setXAxisMax(6.5);
            renderer.setXAxisMin(-0.5);
            //renderer.setXTitle("日付");
            renderer.setYTitle("体重");
            renderer.setAxisTitleTextSize(35);
            renderer.setLabelsTextSize(30);
            renderer.setXLabels(0);
            
            /*X軸　日付の入力*/
            Calendar calendar = getTargetCal();
            for(int i = 6;i >=0;i--,calendar.add(Calendar.DAY_OF_YEAR, -1)){
                renderer.addXTextLabel(i,String.format("%02d/%02d",calendar.get(Calendar.MONTH)+1 ,
                		calendar.get(Calendar.DAY_OF_MONTH)));
            }
            renderer.setApplyBackgroundColor(true);
            renderer.setBackgroundColor(Color.BLACK);
            renderer.setShowGrid(true);
            renderer.addSeriesRenderer(r);
            renderer.addSeriesRenderer(r2);
            renderer.setShowLegend(false);
            
            int[] margin = {10,55,10,55};
            renderer.setMargins(margin);
            /*renderer.setMarginsColor(Color.parseColor("#FF0000"));*/
            return renderer;
        }
     
        GraphicalView makeGraph(Context context, XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
     
            GraphicalView graph = ChartFactory.getLineChartView(context, dataset, renderer);
            return graph;
        }
    }
 
    //データ一覧
    public static class SimpleFragment2 extends Fragment {
    	boolean fat_flag = false;
    	SimpleDatabaseHelper helper = null;
    	SimpleAdapter adapter;
    	ArrayList<HashMap <String,String>> map;
    	AdapterContextMenuInfo info;
    	
        public static SimpleFragment2 getInstance(int position) {
            SimpleFragment2 f = new SimpleFragment2();
            Bundle args = new Bundle();
            args.putInt("position", position);
            f.setArguments(args);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
       		return inflater.inflate(R.layout.fragment_simple2, container, false);
      		
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            map = new ArrayList<HashMap<String,String>>();
            
        }   
        @Override
        public void onStart() {
         super.onStart();
        }
        public void onResume(){
        	super.onResume();
        }
        @Override
        public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo){
        	super.onCreateContextMenu(menu, v, menuInfo);
        	getActivity().getMenuInflater().inflate(R.menu.listview_context, menu);
        }
        @Override
        public boolean onContextItemSelected(MenuItem item){
        	info = (AdapterContextMenuInfo)item.getMenuInfo();
        	switch(item.getItemId()){
        	case R.id.listview1_delete:
        		
        		AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
        		ad.setTitle("データの削除");
        		ad.setMessage("選択したデータを削除してもよろしいですか?");
        		ad.setPositiveButton("はい", 
        				new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								String[] cols = {"_id","fat","weight","ts"};
								SQLiteDatabase db = helper.getReadableDatabase();
					        	Cursor cs = db.query("health",cols, null,null,null,null,"ts desc");
					        	if (cs.getCount() == 0){
					        		db.close();
					        		return;
					        	}
					        	cs.moveToPosition(info.position);
					        	db.delete("health", "_id=?",new String[]{cs.getString(0)});
					        	db.close();
					        	
					        	/*Toast.makeText(getActivity(),cs.getString(0), Toast.LENGTH_SHORT).show();*/
					        	
				        		//リストから削除
				        		map.remove(info.position);
				        		adapter.notifyDataSetChanged();
				        		
				        		//最新情報の更新
					    		ViewPager vp = (ViewPager)getActivity().findViewById(R.id.pager);
					    		ViewPagerAdapter adapter = (ViewPagerAdapter)vp.getAdapter();
					    		SimpleFragment3 sf3 = (SimpleFragment3)adapter.instantiateItem(vp, 1/*最新情報のposition*/);
					    		sf3.listviewUpdate(); 
							}
						});
        		ad.setNegativeButton("いいえ", 
        				new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						});
        		ad.show();
        		break;
        	case R.id.listview1_edit:
        		Intent i = new Intent(getActivity(),com.simplestreet.weightrecord.EditActivity.class);
        		i.putExtra("position", info.position);
        		startActivityForResult(i,1);
        		break;
        	}
        	return true;
        }
        AdView adView;
        @Override
        public void onActivityResult(int requestCode,int resultCode,Intent data){
        	if(requestCode == 1){
        		//リストの再表示
        		updateList();
        		
	    		ViewPager vp = (ViewPager)getActivity().findViewById(R.id.pager);
	    		ViewPagerAdapter adapter = (ViewPagerAdapter)vp.getAdapter();
	    		SimpleFragment3 sf3 = (SimpleFragment3)adapter.instantiateItem(vp, 1/*最新情報のposition*/);
	    		sf3.listviewUpdate(); 
        	}
        }
        public void updateList(){
            StringBuffer str = new StringBuffer();
            try{
            	BufferedReader reader = new BufferedReader(
            			new InputStreamReader(getActivity().openFileInput("SimpleWeight.dat")));
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
            
            helper = new SimpleDatabaseHelper(getActivity());
			SQLiteDatabase db = helper.getReadableDatabase();
			
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
            	//Toast.makeText(getActivity(), "fat月", Toast.LENGTH_SHORT).show();
           
	            /*******************************/
	            TextView tv_fat = (TextView)getActivity().findViewById(R.id.textView3);
	            tv_fat.setVisibility(View.GONE);
	            TextView tv_weight = (TextView)getActivity().findViewById(R.id.textView1);
	            tv_weight.setGravity(Gravity.NO_GRAVITY);
	            /*******************************/
	
	            
				/***** データベース検索処理 *****/
				String[] cols = {"_id","fat","weight","ts"};
	        	Cursor cs = db.query("health",cols, null,null,null,null,"ts desc");
	        	if (cs.getCount() == 0){
	        		db.close();
	        		return;
	        	}
	
	        	map.clear();
	            while (cs.moveToNext()){  
	            	HashMap<String,String> item = new HashMap<String,String>();
	
	            	String strDate = cs.getString(3);
	            	
	            	item.put("date",strDate.substring(5,16));
	            	//item.put("fat",String.format("%.1f", cs.getFloat(1)));
	            	item.put("weight",String.format("%.1f", cs.getFloat(2)));
	            	map.add(item);
	            }
	            
	            adapter = new SimpleAdapter(getActivity(),map,/*R.layout.list_item*/R.layout.list_weight,
	            		new String[] {"date",/*"fat",*/"weight"},
	            		new int[] {R.id.item_date,/*R.id.item_fat,*/R.id.item_weight}
	            );
            }else{

	            /*******************************/
	            TextView tv_fat = (TextView)getActivity().findViewById(R.id.textView3);
	            tv_fat.setVisibility(View.VISIBLE);
	            TextView tv_weight = (TextView)getActivity().findViewById(R.id.textView1);
	            tv_weight.setGravity(Gravity.CENTER);
	            /*******************************/
	
	            
				/***** データベース検索処理 *****/
				String[] cols = {"_id","fat","weight","ts"};
	        	Cursor cs = db.query("health",cols, null,null,null,null,"ts desc");
	        	if (cs.getCount() == 0){
	        		db.close();
	        		return;
	        	}
	
	        	map.clear();
	            while (cs.moveToNext()){  
	            	HashMap<String,String> item = new HashMap<String,String>();
	
	            	String strDate = cs.getString(3);
	            	
	            	item.put("date",strDate.substring(5,16));
	            	item.put("fat",String.format("%.1f", cs.getFloat(1)));
	            	item.put("weight",String.format("%.1f", cs.getFloat(2)));
	            	map.add(item);
	            }
	            
	            adapter = new SimpleAdapter(getActivity(),map,R.layout.list_item,
	            		new String[] {"date","fat","weight"},
	            		new int[] {R.id.item_date,R.id.item_fat,R.id.item_weight}
	            );
            }
	            
	           
            ListView lv = (ListView)getActivity().findViewById(R.id.lv);

            
            //クリック動作だけで、ContextMenuの表示
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            	public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
            		getActivity().openContextMenu(view); // これが大事
            	};
            });
            
            lv.setAdapter(null);
            lv.setAdapter(adapter);
            db.close();
            
        }
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            //advertisement の表示
            /*
            adView = (AdView)getActivity().findViewById(R.id.adView2);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);*/
            updateList();
            
            ListView lv = (ListView)getActivity().findViewById(R.id.lv);
       		/* context menu の登録*/
            registerForContextMenu(lv);
			/***********************/
			
        }
    }
}
