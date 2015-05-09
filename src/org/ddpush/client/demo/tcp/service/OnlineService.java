package org.ddpush.client.demo.tcp.service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.ddpush.client.demo.tcp.ChatActivity;
import org.ddpush.client.demo.tcp.ChatMsgEntity;
import org.ddpush.client.demo.tcp.ChatMsgViewAdapter;
import org.ddpush.client.demo.tcp.DateTimeUtil;
import org.ddpush.client.demo.tcp.MainActivity;
import org.ddpush.client.demo.tcp.Params;
import org.ddpush.client.demo.tcp.Util;
import org.ddpush.client.demo.tcp.receiver.TickAlarmReceiver;
import org.ddpush.im.v1.client.appuser.Message;
import org.ddpush.im.v1.client.appuser.TCPClientBase;

import com.qq.R;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class OnlineService extends Service {
	
	protected PendingIntent tickPendIntent;
	protected TickAlarmReceiver tickAlarmReceiver = new TickAlarmReceiver();
	WakeLock wakeLock;
	MyTcpClient myTcpClient;
	Notification n;
	
	private static Handler mhandler;//更新给前台的handler
	public static void SetHandler(Handler handler)
	{
		mhandler = handler;
	}
	
	public class MyTcpClient extends TCPClientBase {

		public MyTcpClient(byte[] uuid, int appid, String serverAddr, int serverPort)
				throws Exception {
			super(uuid, appid, serverAddr, serverPort, 10);

		}

		@Override
		public boolean hasNetworkConnection() {
			return Util.hasNetwork(OnlineService.this);
		}
		

		@Override
		public void trySystemSleep() {
			tryReleaseWakeLock();
		}

		@Override
		public void onPushMessage(Message message) {
			if(message == null){
				return;
			}
			if(message.getData() == null || message.getData().length == 0){
				return;
			}
			if(message.getCmd() == 16){// 0x10 通用推送信息
				notifyUser(16,"DDPush通用推送信息","时间："+DateTimeUtil.getCurDateTime(),"收到通用推送信息");
			}
			if(message.getCmd() == 17){// 0x11 分组推送信息
				long msg = ByteBuffer.wrap(message.getData(), 5, 8).getLong();
				notifyUser(17,"DDPush分组推送信息",""+msg,"收到通用推送信息");
			}
			if(message.getCmd() == 32){// 0x20 自定义推送信息
				String str = null;
				try{
					str = new String(message.getData(),5,message.getContentLength(), "UTF-8");
				}catch(Exception e){
					str = Util.convert(message.getData(),5,message.getContentLength());
				}
				String []str2 = str.split(",,,");
				if(mhandler!=null)
				{
				android.os.Message message2 = new android.os.Message();
				message2.obj = str;
				mhandler.sendMessage(message2);//更新消息
				}
				notifyUser(32,"玩乎",""+str,"收到 "+str2[1]+" 发来的消息");
			}
			setPkgsInfo();
		}

	}

	public OnlineService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.setTickAlarm();
		
		PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "OnlineService");
		
		resetClient();
		
		notifyRunning();
	}

	@Override
	public int onStartCommand(Intent param, int flags, int startId) {
		if(param == null){
			return START_STICKY;
		}
		String cmd = param.getStringExtra("CMD");
		if(cmd == null){
			cmd = "";
		}
		if(cmd.equals("TICK")){
			if(wakeLock != null && wakeLock.isHeld() == false){
				wakeLock.acquire();
			}
		}
		if(cmd.equals("RESET")){
			if(wakeLock != null && wakeLock.isHeld() == false){
				wakeLock.acquire();
			}
			resetClient();
		}
		if(cmd.equals("TOAST")){
			String text = param.getStringExtra("TEXT");
			if(text != null && text.trim().length() != 0){
				Toast.makeText(this, text, Toast.LENGTH_LONG).show();
			}
		}
		
		setPkgsInfo();

		return START_STICKY;
	}
	
	protected void setPkgsInfo(){
		if(this.myTcpClient == null){
			return;
		}
		long sent = myTcpClient.getSentPackets();
		long received = myTcpClient.getReceivedPackets();
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = account.edit();
		editor.putString(Params.SENT_PKGS, ""+sent);
		editor.putString(Params.RECEIVE_PKGS, ""+received);
		editor.commit();
	}
	
	protected void resetClient(){
		SharedPreferences account = this.getSharedPreferences(Params.DEFAULT_PRE_NAME,Context.MODE_PRIVATE);
		String serverIp = Params.SERVERIP;
		String serverPort = Params.SERVERPORT;
		String pushPort = Params.PUSHPORT;
		String userName = account.getString(Params.USER_NAME, "");
		if(serverIp == null || serverIp.trim().length() == 0
				|| serverPort == null || serverPort.trim().length() == 0
				|| pushPort == null || pushPort.trim().length() == 0
				|| userName == null || userName.trim().length() == 0){
			return;
		}
		if(this.myTcpClient != null){
			try{myTcpClient.stop();}catch(Exception e){}
		}
		try{
			myTcpClient = new MyTcpClient(Util.md5Byte(userName), 1, serverIp, Integer.parseInt(serverPort));
			myTcpClient.setHeartbeatInterval(50);
			myTcpClient.start();
			SharedPreferences.Editor editor = account.edit();
			editor.putString(Params.SENT_PKGS, "0");
			editor.putString(Params.RECEIVE_PKGS, "0");
			editor.commit();
		}catch(Exception e){
			Toast.makeText(this.getApplicationContext(), "操作失败："+e.getMessage(), Toast.LENGTH_LONG).show();
		}
//		Toast.makeText(this.getApplicationContext(), "ddpush：终端重置", Toast.LENGTH_LONG).show();
	}
	
	protected void tryReleaseWakeLock(){
		if(wakeLock != null && wakeLock.isHeld() == true){
			wakeLock.release();
		}
	}
	
	protected void setTickAlarm(){
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);  
		Intent intent = new Intent(this,TickAlarmReceiver.class);
		int requestCode = 0;  
		tickPendIntent = PendingIntent.getBroadcast(this,  
		requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
		//小米2s的MIUI操作系统，目前最短广播间隔为5分钟，少于5分钟的alarm会等到5分钟再触发！2014-04-28
		long triggerAtTime = System.currentTimeMillis();
		int interval = 300 * 1000;  
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
	}
	
	protected void cancelTickAlarm(){
		AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmMgr.cancel(tickPendIntent);  
	}
	
	protected void notifyRunning(){
		NotificationManager notificationManager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		n = new Notification();  
		Intent intent = new Intent(this,MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_ONE_SHOT);
		n.contentIntent = pi;
		n.setLatestEventInfo(this, "DDPushDemoTCP", "正在运行", pi);
		//n.defaults = Notification.DEFAULT_ALL;
		//n.flags |= Notification.FLAG_SHOW_LIGHTS;  
		//n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.flags |= Notification.FLAG_ONGOING_EVENT;
		n.flags |= Notification.FLAG_NO_CLEAR;
		//n.iconLevel = 5;
		           
		n.icon = R.drawable.ic_launcher;  
		n.when = System.currentTimeMillis();
		n.tickerText = "DDPushDemoTCP正在运行";
		notificationManager.notify(0, n);
	}
	
	protected void cancelNotifyRunning(){
		NotificationManager notificationManager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}
	
	public void notifyUser(int id, String title, String str, String tickerText){
		NotificationManager notificationManager=(NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification();  
		Intent intent = new Intent(this,ChatActivity.class);
		String []str2 = str.split(",");
		intent.putExtra("friendName", str2[1]);
		intent.putExtra("username", str2[0]);
		PendingIntent pi = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_ONE_SHOT);
		n.contentIntent = pi;

		n.setLatestEventInfo(this, title, str2[2], pi);
		n.defaults = Notification.DEFAULT_ALL;
		n.flags |= Notification.FLAG_SHOW_LIGHTS;  
		n.flags |= Notification.FLAG_AUTO_CANCEL;

		n.icon = R.drawable.ic_launcher;  
		n.when = System.currentTimeMillis();
		n.tickerText = tickerText;
		notificationManager.notify(id, n);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//this.cancelTickAlarm();
		cancelNotifyRunning();
		this.tryReleaseWakeLock();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


}
