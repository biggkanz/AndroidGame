package edu.iastate.fightthings.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;


public class TopscoreContent 
{

	public static DatabaseConnector db;
	
	public static List<TopscoreItem> ITEMS = new ArrayList<TopscoreItem>();

	public static Map<String, TopscoreItem> ITEM_MAP = new HashMap<String, TopscoreItem>();

	public static void setContext(Context c) 
	{
	    if (db == null) 
	    	db = new DatabaseConnector(c); // SQLiteOpenHelper + SQLiteDatabase manager
	    if (db.isOpen() == false) 
	    {
	        db.open();
	        
	        Cursor c1 = db.getTopscores(); // database query
	        
	        if (c1.moveToFirst()) 
	        {
	            do 
	            {
	                TopscoreItem item = 
	                		new TopscoreItem(c1.getString(0), c1.getString(1), c1.getInt(2));
	                addItem(item);
	            } while (c1.moveToNext());
	        }
	    }
	}

	private static void addItem(TopscoreItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	public static class TopscoreItem 
	{
		public String id;
		public String name;
		public int score;

		public TopscoreItem(String id, String name, int score) 
		{
			this.id = id;
			this.name = name;
			this.score = score;
		}

		@Override
		public String toString() {
			return name + " " + score;
		}
	}
}
