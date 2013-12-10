package edu.iastate.fightthings.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;


public class MonsterContent 
{

	public static DatabaseConnector db;
	
	public static List<MonsterItem> ITEMS = new ArrayList<MonsterItem>();

	public static Map<String, MonsterItem> ITEM_MAP = new HashMap<String, MonsterItem>();

	public static void setContext(Context c) 
	{
	    if (db == null) 
	    	db = new DatabaseConnector(c); // SQLiteOpenHelper + SQLiteDatabase manager
	    if (db.isOpen() == false) 
	    {
	        db.open();
	        
	        Cursor c1 = db.getMonsters(); // database query
	        
	        if (c1.moveToFirst()) 
	        {
	            do 
	            {
	                MonsterItem item = new MonsterItem(c1.getString(0), c1.getString(1), c1.getString(2), c1.getString(3));
	                addItem(item);
	            } while (c1.moveToNext());
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
	public static class MonsterItem 
	{
		public String id;
		public String name;
		public String image;
		public String health;

		public MonsterItem(String id, String name, String image, String health) 
		{
			this.id = id;
			this.name = name;
			this.image = image;
			this.health = health;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
