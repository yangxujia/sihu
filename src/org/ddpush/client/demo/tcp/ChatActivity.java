package org.ddpush.client.demo.tcp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ddpush.client.demo.tcp.service.OnlineService;
import org.ddpush.im.v1.client.appserver.Pusher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bestjiajia.database.DataHelper;
import com.bestjiajia.database.SqliteHelper;
import com.qq.R;


/**
 * 
 * @author 杨旭佳
 */
public class ChatActivity extends Activity implements OnClickListener{
    /** Called when the activity is first created. */

	private Button mBtnSend;
	private Button mBtnBack;
	private EditText mEditTextContent;
	private ListView mListView;
	private ImageView friendHead;
	private ImageView userHead;
	private ChatMsgViewAdapter mAdapter;
	private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();
	private String username;
	private String friendName;
	private Handler handler = new Handler() {
		//处理消息
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			reciever( msg.obj.toString());
//			Toast.makeText(ChatActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
		}
	};
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
         username = getIntent().getExtras().getString("username");
         friendName = getIntent().getExtras().getString("friendName");
//         Toast.makeText(this.getApplicationContext(), username+"___"+friendName, Toast.LENGTH_SHORT).show();
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
        initView();
        initData();
      //传递handler
		OnlineService.SetHandler(handler);
    }
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
//    	//setup 朋友聊天头像
//    	friendHead = (ImageView) findViewById(R.id.ivfriendhead);
//    	String url = Params.IMAGE_PATH+friendName+".jpg";
//    	ImageLoader imageLoader = new ImageLoader(ChatActivity.this, true);
//    	Log.i("url", url);
//    	imageLoader.DisplayImage(url, friendHead);
//    	//setup 用户聊天头像
//    	userHead = (ImageView) findViewById(R.id.ivuserhead);
//    	String url2 = Params.IMAGE_PATH+username+".jpg";
//    	ImageLoader imageLoader2 = new ImageLoader(ChatActivity.this, true);
//    	imageLoader2.DisplayImage(url2, userHead);
    }
    public void initView()
    {
    	mListView = (ListView) findViewById(R.id.listview);
    	mBtnSend = (Button) findViewById(R.id.btn_send);
    	mBtnSend.setOnClickListener(this);
    	mBtnBack = (Button) findViewById(R.id.btn_back);
    	mBtnBack.setOnClickListener(this);
    	mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);
    }
    

    public void initData()
    {
    	//查询数据库中的聊天记录
        SqliteHelper helper=new SqliteHelper(getApplicationContext(), "sihu.db", 5);
        SQLiteDatabase db=helper.getReadableDatabase();
        DataHelper dh = new DataHelper(getApplicationContext());
		//创建与该用户的聊天表
		dh.createTable(friendName); 
		dh.Close();
        Cursor cur = db.query("chat_"+friendName, new String[]{"id","createTime","message","flag"}, null,null, "id", null, null);
        while(cur.moveToNext()){
            long createTime=cur.getLong(cur.getColumnIndex("createTime"));
            SimpleDateFormat dateFormat = new SimpleDateFormat(
    				"yyyy-MM-dd HH:mm");
            String createTimeStr = dateFormat.format(createTime).toString();
            String message=cur.getString(cur.getColumnIndex("message"));
            int flag=cur.getInt(cur.getColumnIndex("flag"));
//        	Toast.makeText(getApplicationContext(), id+" "+createTimeStr+" "+message+" "+flag, Toast.LENGTH_LONG).show();
    		ChatMsgEntity entity = new ChatMsgEntity();
    		entity.setDate(createTimeStr);
    		if (flag == 1)
    		{
    			entity.setName(friendName);
    			entity.setMsgType(true);
    		}else{
    			entity.setName(username);
    			entity.setMsgType(false);
    		}
    		((TextView)findViewById(R.id.textViewFriendName)).setText(friendName);
    		entity.setText(message);
    		mDataArrays.add(entity);
        }
    	mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_send:
			send();
			break;
		case R.id.btn_back:
			finish();
			break;
		}
	}
	
	private void sendThrouhInternet(String targetUserName,String message){
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		String serverIp = Params.SERVERIP;
		String pushPort = Params.PUSHPORT;
		int port;
		try{
			port = Integer.parseInt(pushPort);
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "推送端口格式错误："+pushPort, Toast.LENGTH_SHORT).show();
			return;
		}
		byte[] uuid = null;
		try{
			uuid = Util.md5Byte(targetUserName);
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "错误："+e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		byte[] msg = null;
		try{
			msg = message.getBytes("UTF-8");
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "错误："+e.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
		Thread t = new Thread(new send0x20Task(this,serverIp,port,uuid,msg));
		t.start();
	}
	
	//发送消息
	private void send()
	{
		
		String contString = mEditTextContent.getText().toString();
		String msg = friendName+",,,"+username+",,,"+contString;
		sendThrouhInternet(friendName, msg);//通过服务器端发送
		if (contString.length() > 0)
		{
			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setDate(getDate());
			entity.setName(username);//用户自己的名字
			entity.setMsgType(false);
			entity.setText(contString);
			mDataArrays.add(entity);
			mAdapter.notifyDataSetChanged();
			mEditTextContent.setText("");
			mListView.setSelection(mListView.getCount() - 1);
			DataHelper dh = new DataHelper(getApplicationContext());
			//创建与该用户的聊天表
			dh.createTable(friendName);
			dh.SaveChat(entity, friendName);
			dh.Close();
		}
	}
	
	//接收 
	public void reciever(String str){
		if (str.length() > 0)
		{
			String []str2 = str.split(",,,");
			String message = str2[2];
			String friendName2 = str2[1] ;
			ChatMsgEntity entity = new ChatMsgEntity();
			entity.setDate(getDate());
			entity.setName(friendName);
			entity.setMsgType(true);
			entity.setText(message);
			if(friendName2.equals(friendName)){//写入适配器中
				mDataArrays.add(entity);
				mAdapter.notifyDataSetChanged();
				mEditTextContent.setText("");
				mListView.setSelection(mListView.getCount() - 1);
			}
			DataHelper dh = new DataHelper(getApplicationContext());
			//创建与该用户的聊天表
			dh.createTable(friendName2);
			dh.SaveChat(entity, friendName2);
			dh.Close();
		}
	}
    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm");
        return dateFormat.format(new Date()).toString();
    }
    
    
//    public void head_xiaohei(View v) {     //标题栏 返回按钮
//    	Intent intent = new Intent (ChatActivity.this,InfoAFeng.class);			
//		startActivity(intent);	
//      } 
}
//向外发送的类
class send0x20Task implements Runnable{
	private Context context;
	private String serverIp;
	private int port;
	private byte[] uuid;
	private byte[] msg;
	
	public send0x20Task(Context context, String serverIp, int port, byte[] uuid, byte[] msg){
		this.context = context;
		this.serverIp = serverIp;
		this.port = port;
		this.uuid = uuid;
		this.msg = msg;
	}
	
	@Override
	public void run(){
		Pusher pusher = null;
		Intent startSrv = new Intent(context, OnlineService.class);
		startSrv.putExtra("CMD", "TOAST");
		try{
			boolean result;
			pusher = new Pusher(serverIp,port, 1000*5);
			result = pusher.push0x20Message(uuid,msg);
			if(result){
				startSrv.putExtra("TEXT", "自定义信息发送成功");
			}else{
				startSrv.putExtra("TEXT", "发送失败！格式有误");
			}
		}catch(Exception e){
			e.printStackTrace();
			startSrv.putExtra("TEXT", "发送失败！"+e.getMessage());
		}finally{
			if(pusher != null){
				try{pusher.close();}catch(Exception e){};
			}
		}
		context.startService(startSrv);
	}
}