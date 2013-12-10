package edu.iastate.fightthings;

import edu.iastate.fightthings.data.DatabaseConnector;
import android.app.Application;
import android.database.Cursor;

public class FightThings extends Application 
{
	public static DatabaseConnector db;
	public static Cursor cursors;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		db = new DatabaseConnector(this);
		db.insertMonster(
				"Sea Monster", 
				"seamonster.png", 
				"100");	
		
	}
}
