package com.qq;

import com.bestjiajia.database.DataHelper;
import com.qq.activity.LoginActivity;
import com.qq.fragment.ConstactFatherFragment;
import com.qq.fragment.DynamicFragment;
import com.qq.fragment.NewsFatherFragment;
import com.qq.fragment.SettingFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class MainActivity extends FragmentActivity {

	protected static final String TAG = "MainActivity";
	private Context mContext;
	private ImageButton mNews,mConstact,mDeynaimic,mSetting;
	private View mPopView;
	private View currentButton;
	
	private TextView app_cancle;
	private TextView app_exit;
	private TextView app_change;
	
	private int mLevel=1;
	private PopupWindow mPopupWindow;
	private LinearLayout buttomBarGroup;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext=this;
		
		findView();
		init();
	}
	
	private void findView(){
		mPopView=LayoutInflater.from(mContext).inflate(R.layout.app_exit, null);
		buttomBarGroup=(LinearLayout) findViewById(R.id.buttom_bar_group);
		mNews=(ImageButton) findViewById(R.id.buttom_news);
		mConstact=(ImageButton) findViewById(R.id.buttom_constact);
		mDeynaimic=(ImageButton) findViewById(R.id.buttom_deynaimic);
		mSetting=(ImageButton) findViewById(R.id.buttom_setting);
		
		app_cancle=(TextView) mPopView.findViewById(R.id.app_cancle);
		app_change=(TextView) mPopView.findViewById(R.id.app_change_user);
		app_exit=(TextView) mPopView.findViewById(R.id.app_exit);
	}
	
	private void init(){
		mNews.setOnClickListener(newsOnClickListener);
		mConstact.setOnClickListener(constactOnClickListener);
		mDeynaimic.setOnClickListener(deynaimicOnClickListener);
		mSetting.setOnClickListener(settingOnClickListener);
		//初始话，创建消息表
		//查询数据库中的聊天记录
        
        DataHelper dh = new DataHelper(mContext);
		dh.createNewsTable(); 
		dh.Close();
		mNews.performClick();//默认模拟点击
		//根据用户名
		mPopupWindow=new PopupWindow(mPopView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
		
		app_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPopupWindow.dismiss();
			}
		});
		
		app_change.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(mContext, LoginActivity.class);
				startActivity(intent);
				((Activity)mContext).overridePendingTransition(R.anim.activity_up, R.anim.fade_out);
				finish();
			}
		});
		
		app_exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	FragmentManager fm=getSupportFragmentManager();
	NewsFatherFragment newsFatherFragment=new NewsFatherFragment();
	ConstactFatherFragment constactFatherFragment=new ConstactFatherFragment();
	DynamicFragment dynamicFragment=new DynamicFragment();
	SettingFragment settingFragment=new SettingFragment();
	
	private OnClickListener newsOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentTransaction ft=fm.beginTransaction();
			if(!newsFatherFragment.isAdded()){
				ft.add(R.id.fl_content, newsFatherFragment,MainActivity.TAG);
			}
			ft.hide(constactFatherFragment);
			ft.hide(dynamicFragment);
			ft.hide(settingFragment);
			ft.show(newsFatherFragment);
//			ft.addToBackStack(null);
			ft.commit();
			setButton(v);
		}
	};
	
	private OnClickListener constactOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentTransaction ft=fm.beginTransaction();
			if(!constactFatherFragment.isAdded()){
				ft.add(R.id.fl_content, constactFatherFragment,MainActivity.TAG);
			}
			ft.hide(dynamicFragment);
			ft.hide(newsFatherFragment);
			ft.hide(settingFragment);
			ft.show(constactFatherFragment);
//			ft.addToBackStack(null);
			ft.commit();
			setButton(v);
			
		}
	};
	
	private OnClickListener deynaimicOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentTransaction ft=fm.beginTransaction();
			if(!dynamicFragment.isAdded()){
				ft.add(R.id.fl_content, dynamicFragment,MainActivity.TAG);
			}
			ft.hide(constactFatherFragment);
			ft.hide(newsFatherFragment);
			ft.hide(settingFragment);
			ft.show(dynamicFragment);
//			ft.addToBackStack(null);
			ft.commit();
			setButton(v);
			
		}
	};
	
	private OnClickListener settingOnClickListener=new OnClickListener() {
		@Override
		public void onClick(View v) {
			FragmentTransaction ft=fm.beginTransaction();
			if(!settingFragment.isAdded()){
				ft.add(R.id.fl_content, settingFragment,MainActivity.TAG);
			}
			ft.hide(constactFatherFragment);
			ft.hide(newsFatherFragment);
			ft.hide(dynamicFragment);
			ft.show(settingFragment);
//			ft.addToBackStack(null);
			ft.commit();
			setButton(v);
			
		}
	};
	
	//被点中的按钮将不能再按了
	private void setButton(View v){
		if(currentButton!=null&&currentButton.getId()!=v.getId()){
			currentButton.setEnabled(true);
		}
		v.setEnabled(false);
		currentButton=v;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_MENU){
			mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
			mPopupWindow.showAtLocation(buttomBarGroup, Gravity.BOTTOM, 0, 0);
			mPopupWindow.setAnimationStyle(R.style.app_pop);
			mPopupWindow.setOutsideTouchable(true);
			mPopupWindow.setFocusable(true);
			mPopupWindow.update();
		}
		return super.onKeyDown(keyCode, event);
	}
}
