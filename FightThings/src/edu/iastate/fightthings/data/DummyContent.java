package edu.iastate.fightthings.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

	public static List<MonsterItem> ITEMS = new ArrayList<MonsterItem>();

	public static Map<String, MonsterItem> ITEM_MAP = new HashMap<String, MonsterItem>();
	
	public static DatabaseConnector db;
	
	public static void setContext(Context c) throws SQLException
	{
	    if (db == null) 
	    	db = new DatabaseConnector(c); // SQLiteOpenHelper + SQLiteDatabase manager
	    if (db.isOpen() == false) 
	    {
	        db.open();  
	        
	        Cursor c1 = db.getAllContacts(); // database query
	        if (c1.moveToFirst()) 
	        {
	            do 
	            {
	            	MonsterItem item = new MonsterItem(c1.getString(0), c1.getString(1));
	                addItem(item);
	            } 
	            while (c1.moveToNext());
	        }
	    }
	}


	private static void addItem(MonsterItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class MonsterItem {
		public String id;
		public String content;

		public MonsterItem(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
