package com.lm2a.serverchecker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ProActivity extends Activity implements OnClickListener {

	WebView browser;
	Button pro_btn;
	ImageView orangu;
	TextView counter;
	ArrayList<String> listCountry; 
	LinearLayout relative;
	FrameLayout frame;
	int n = 5;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.pro);
		relative = (LinearLayout)findViewById(R.id.relative);
		//relative.setBackgroundResource(R.drawable.gradient);
		
		prepareList();  
		frame = (FrameLayout)findViewById(R.id.frame); 
//		ListView listView = (ListView)findViewById(R.id.listView);
//		listView.setAdapter(new ArrayAdapter<String>(
//		this, R.layout.list_item, listCountry));
		counter = (TextView) findViewById(R.id.counter);
		
		orangu = (ImageView)findViewById(R.id.icon);
		pro_btn = (Button) findViewById(R.id.pro_btn);
		pro_btn.setOnClickListener(this);

	}

	public final void launchMarket() {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("market://details?id=com.lm2a.qd.pro"));
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {


		
		if (pro_btn.getId() == v.getId()){ 
			orangu.setImageResource(R.mipmap.oranguhappy);
			pro_btn.setText("Thanks!!!");
			pro_btn.setEnabled(false);
			orangu.invalidate();
			pro_btn.invalidate();
			
			new CountDownTimer(7000,1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                	
                	counter.setText(""+n);
                   	--n;
                	frame.invalidate();
 
                }

                @Override
                public void onFinish() {

                	handler.sendEmptyMessage(1);
                }
            }.start();
            
			
			
		}


	}

	public void prepareList()  
	{  
		listCountry = new ArrayList<String>();  
		listCountry.add(getResources().getString(R.string.proLine2));  
		listCountry.add(getResources().getString(R.string.proLine3));  
		listCountry.add(getResources().getString(R.string.proLine4));  
		listCountry.add(getResources().getString(R.string.proLine5));  
	}  

	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case 0:
				orangu.invalidate();
				pro_btn.invalidate();
				break;
			case 1:
				launchMarket();
				break;
			default:
				break;

			}
		}
	};
}
