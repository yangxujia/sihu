package com.qq.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ddpush.client.demo.tcp.ChatActivity;
import org.ddpush.client.demo.tcp.ChatMsgViewAdapter;
import org.ddpush.client.demo.tcp.News;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.bestjiajia.database.SqliteHelper;
import com.qq.AsyncTaskBase;
import com.qq.R;
import com.qq.adapter.NewsAdapter;
import com.qq.bean.RecentChat;
import com.qq.test.TestData;
import com.qq.util.MyPreference;
import com.qq.view.CustomListView;
import com.qq.view.CustomListView.OnRefreshListener;
import com.qq.view.LoadingView;

public class NewsFragment extends Fragment {
	private static final String TAG = "NewsFragment";
	private Context mContext;
	private View mBaseView;
	private CustomListView mCustomListView;
	private LoadingView mLoadingView;
	private View mSearchView;
	private NewsAdapter adapter;
	private LinkedList<RecentChat> chats = new LinkedList<RecentChat>();
	List<RecentChat> recentChats = new ArrayList<RecentChat>();
	private String username;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		mBaseView = inflater.inflate(R.layout.fragment_news, null);
		mSearchView = inflater.inflate(R.layout.common_search_l, null);
		findView();
		init();
		return mBaseView;
	}

	private void findView() {
		mCustomListView = (CustomListView) mBaseView.findViewById(R.id.lv_news);
		mLoadingView = (LoadingView) mBaseView.findViewById(R.id.loading);


	}

	private void init() {
		adapter = new NewsAdapter(mContext, chats, mCustomListView);
		mCustomListView.setAdapter(adapter);
		//取出已保存用户名
		username = MyPreference.getInstance(mContext)  
                .getLoginName();
//加入点击事件
		mCustomListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				RecentChat recentChat = null;
				if(arg2>=2){
					recentChat = recentChats.get(arg2-2);
					Toast.makeText(mContext, recentChat.getUserName()+" ||"+recentChat.getUserFeel()+" || "+recentChat.getUserTime()+"||"+recentChat.getImgPath() , Toast.LENGTH_LONG).show();
				}
				Intent intent = new Intent();
				intent.putExtra("friendName",  recentChat.getUserName());
				intent.putExtra("username", username);
				intent.setClass(mContext,ChatActivity.class);
				startActivity(intent);
			}
			
		});
		
		mCustomListView.addHeaderView(mSearchView);
		mCustomListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				new AsyncRefresh().execute(0);
			}
		});
		mCustomListView.setCanLoadMore(false);
		new NewsAsyncTask(mLoadingView).execute(0);
	}

	private class NewsAsyncTask extends AsyncTaskBase {
		

		public NewsAsyncTask(LoadingView loadingView) {
			super(loadingView);
		}

		//加载测试数据
		@Override
		protected Integer doInBackground(Integer... params) {
			int result = -1;
			//从本地数据库加载数据
			SqliteHelper helper=new SqliteHelper(mContext, "sihu.db", 5);
	        SQLiteDatabase db=helper.getReadableDatabase();
	        Cursor cur = db.rawQuery("select * from news orderby time desc", null);
	        while(cur.moveToNext()){
	            long createTime=cur.getLong(cur.getColumnIndex("time"));
	            SimpleDateFormat dateFormat = new SimpleDateFormat(
	    				"yyyy-MM-dd HH:mm");
	            String createTimeStr = dateFormat.format(createTime).toString();
	            String message=cur.getString(cur.getColumnIndex("message"));
	            String name=cur.getString(cur.getColumnIndex("name"));
	            String picPath=cur.getString(cur.getColumnIndex("picPath"));
//	        	Toast.makeText(getApplicationContext(), id+" "+createTimeStr+" "+message+" "+flag, Toast.LENGTH_LONG).show();
	    		News news = new News();
	    		news.setName(name);
	    		news.setMessage(message);
	    		news.setPicPath(picPath);
	    		news.setTime(createTimeStr);
	    		RecentChat  recentChat = new RecentChat(name, message, createTimeStr, picPath);
	    		recentChats.add(recentChat);
	    }
	       //去掉测试数据
			//recentChats = TestData.getRecentChats();
			if (recentChats.size() > 0) {
				result = 1;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			chats.addAll(recentChats);
			adapter.notifyDataSetChanged();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

	private class AsyncRefresh extends
			AsyncTask<Integer, Integer, List<RecentChat>> {
		private List<RecentChat> recentchats = new ArrayList<RecentChat>();

		@Override
		protected List<RecentChat> doInBackground(Integer... params) {
			recentchats = TestData.getRecentChats();
			return recentchats;
		}

		@Override
		protected void onPostExecute(List<RecentChat> result) {
			super.onPostExecute(result);
			if (result != null) {
				for (RecentChat rc : recentchats) {
					chats.addFirst(rc);
				}
				adapter.notifyDataSetChanged();
				mCustomListView.onRefreshComplete();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

}
