package com.bestjiajia.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ddpush.client.demo.tcp.ChatMsgEntity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DataHelper {
	// 数据库名称
	private static String DB_NAME = "sihu.db";
	// 数据库版本
	private static int DB_VERSION = 5;
	private SQLiteDatabase db;
	private SqliteHelper dbHelper;

	public DataHelper(Context context) {
		dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION);
		db = dbHelper.getWritableDatabase();
	}

	// 关闭数据库
	public void Close() {
		db.close();
		dbHelper.close();
	}

	// 添加users表的记录
	public Long SaveChat(ChatMsgEntity entity,String userId) {//userId为朋友的id
		ContentValues values = new ContentValues();
		values.put("createTime", new Date().getTime());
		values.put("message", entity.getText());
		values.put("flag", entity.getMsgType());
		Long uid = db.insert("chat_"+userId, "id", values);
		Log.e("SaveChat", uid + "");
		return uid;
	}
	
	// 获取users表中的UserID、Access Token、Access Secret的记录
	public List<ChatMsgEntity> GetUserList(Boolean isSimple, String userId) {
		List<ChatMsgEntity> userList = new ArrayList<ChatMsgEntity>();
		// 获得数据表中的所有数据
		Cursor cursor = db.query("chat_" + userId, null, null, null, null,
				null, "id DESC");
		// 将游标移动到开始
		cursor.moveToFirst();
		// 循环遍历整个数据库
		while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
			ChatMsgEntity chat = new ChatMsgEntity();
			chat.setDate((new Date(cursor.getLong(1)).getTime())+""); // 将long 转出 date
			chat.setText(cursor.getString(2));
			chat.setMsgType(cursor.getInt(3)==1?true:false);
			userList.add(chat);
			cursor.moveToNext();
		}
		cursor.close();
		return userList;
	}

	public void createTable(String table) {
		// TODO Auto-generated method stub
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_"+table  
     			+"(id integer primary key,"+  
     			 " createTime integer ,"+  
     			 " message text,"+  
     			 " flag integer"+
     			")"  
     			); 
	}

	//普通的创建表方法，将sql开放到外面
	public void createNewsTable() {
        db.execSQL(
        		"CREATE TABLE IF NOT EXISTS news"  
     			+"(id integer primary key,"+  
     			 " name varchar,"+  
     			 " message text,"+  
     			 " time integer ,"+  
     			 " picPath integer"+
     			")" 
     			); 
	}
	// 判断users表中的是否包含某个UserID的记录
//	public Boolean HaveChat(String userId) {
//		Boolean b = false;
//		Cursor cursor = db.query("chat_" + userId, null, Chat.USERID + "="
//				+ UserId, null, null, null, null);
//		b = cursor.moveToFirst();
//		Log.e("HaveChat", b.toString());
//		cursor.close();
//		return b;
//	}

	// 判断users表中的是否包含某个UserID的记录
//	public Chat GetUserByName(String userName) {
//		Boolean b = false;
//		// 注意汉字为查询条件时需要加''
//		Cursor cursor = db.query(SqliteHelper.TB_NAME, null, Chat.USERNAME
//				+ "='" + userName + "'", null, null, null, null);
//		b = cursor.moveToFirst();
//		Log.e("GetUserByName", b.toString());
//		if (b != false) {
//			Chat chat = new Chat();
//			chat.setId(cursor.getInt(0));
//			chat.setCreateTime(new Date(cursor.getLong(1))); // 将long 转出 date
//			chat.setMessage(cursor.getString(2));
//			chat.setFlag(cursor.getInt(3));
//			ByteArrayInputStream stream = new ByteArrayInputStream(
//					cursor.getBlob(5));
//			Drawable icon = Drawable.createFromStream(stream, "image");
//			user.setUserIcon(icon);
//			cursor.close();
//			return user;
//		}
//		return null;
//	}

	// 更新users表的记录，根据UserId更新用户昵称和用户图标
//	public int UpdateChat(String userName, Bitmap userIcon, String UserId) {
//		ContentValues values = new ContentValues();
//		values.put(Chat.USERNAME, userName);
//		// BLOB类型
//		final ByteArrayOutputStream os = new ByteArrayOutputStream();
//		// 将Bitmap压缩成PNG编码，质量为100%存储
//		userIcon.compress(Bitmap.CompressFormat.PNG, 100, os);
//		// 构造SQLite的Content对象，这里也可以使用raw
//		values.put(Chat.USERICON, os.toByteArray());
//		int id = db.update(SqliteHelper.TB_NAME, values, Chat.USERID + "="
//				+ UserId, null);
//		Log.e("UpdateChat2", id + "");
//		return id;
//	}

	// 更新users表的记录
//	public int UpdateChat(Chat user) {
//		ContentValues values = new ContentValues();
//		values.put(Chat.USERID, user.getUserId());
//		values.put(Chat.TOKEN, user.getToken());
//		values.put(Chat.TOKENSECRET, user.getTokenSecret());
//		int id = db.update(SqliteHelper.TB_NAME, values, Chat.USERID + "="
//				+ user.getUserId(), null);
//		Log.e("UpdateChat", id + "");
//		return id;
//	}



	// 删除users表的记录
//	public int DelChat(String UserId) {
//		int id = db.delete(SqliteHelper.TB_NAME, Chat.USERID + "=" + UserId,
//				null);
//		Log.e("DelChat", id + "");
//		return id;
//	}
}