package edu.iastate.fightthings;

import java.io.File;

import edu.iastate.fightthings.data.DatabaseConnector;

import android.app.Application;
import android.content.ContextWrapper;

public class FightThings extends Application 
{
	public int kills = 0;
	
	public FightThings()
	{ 
		super();
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		if (!databaseExists(this, getString(R.string.database_name)))
		{
			DatabaseConnector db = new DatabaseConnector(this);
			db.insertMonster("Sea Monster", "m_seamonster", "50");
			db.insertMonster("Cthulu", "m_cthulu", "150");
			db.insertMonster("Old One", "m_oldone", "100");
			
			db.insertTopscore("Biggs", 5);
			db.insertTopscore("Player1", 7);
			db.insertTopscore("Player2", 1);
		}
		
	}
	
	
	private static boolean databaseExists(ContextWrapper context, String dbName) 
	{
	    File dbFile=context.getDatabasePath(dbName);
	    return dbFile.exists();
	}
}
