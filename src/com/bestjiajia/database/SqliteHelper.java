package com.bestjiajia.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * sqlite 辅助类
 * @author sRoger
 *
 */
public class SqliteHelper extends SQLiteOpenHelper {

    /**
     * 版本号
     */
    private static final int VERSION = 5;
    
    private final String tag="SqliteHelper";
    private String table;
    
    public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	/**
     * 构造函数
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    public SqliteHelper(Context context, String name, CursorFactory factory, int version) {
        // 调用父类构造函数
        super(context, name, factory, version);
    }
    
    public SqliteHelper(Context context,String name,int version){
        this(context,name,null,version);
    }
    
    public SqliteHelper(Context context,String name){
        this(context,name,VERSION);
    }

    public SqliteHelper(Context context, String name,
			String table) {
    	this(context,name,VERSION);
    	this.table = table;
	}

	@Override
    public void onCreate(SQLiteDatabase db) {
//        Log.d(tag, "创建了一个Sqlite数据库"+table);
//        db.execSQL("create table "+table+
//                    "(" +
//                    "    id int," +
//                    "    name varchar(20)" +
//                    ")"
//        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(tag, "Upgrade execute.");
    }

}