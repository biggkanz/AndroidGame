// DatabaseConnector.java
// Provides easy connection and creation of Usermonsters database.
package edu.iastate.fightthings.data;

import edu.iastate.fightthings.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseConnector 
{
   private SQLiteDatabase database; // database object
   private DatabaseOpenHelper databaseOpenHelper; // database helper

   // public constructor for DatabaseConnector
   public DatabaseConnector(Context context) 
   {
      // create a new DatabaseOpenHelper
	
      databaseOpenHelper = new DatabaseOpenHelper(
    		  context, 
    		  context.getString(R.string.database_name),
    		  null, 1);
   } // end DatabaseConnector constructor

   // open the database connection
   public void open() throws SQLException 
   {
      if(!this.isOpen())
    	  database = databaseOpenHelper.getWritableDatabase();
   } // end method open

   // close the database connection
   public void close() 
   {
      if (database != null)
         database.close(); // close the database connection
   } // end method close
   
   // inserts a new contact in the database
   public void insertTopscore(String name, int score)
   {
	  ContentValues newTopscore = new ContentValues();
	  newTopscore.put("name", name);
	  newTopscore.put("score", score);
	
	  open(); // open the database
	  database.insert("topscores", null, newTopscore);
	  //close(); // close the database
   } // end method insertContact
   
   public Cursor getTopscores() 
   {
	      return database.query("topscores", new String[] {"_id", "name", "score"}, 
	    	         null, null, null, null, "score DESC");
   }

   // inserts a new contact in the database
   public void insertMonster(String name, String image, String health)
   {
	  ContentValues newContact = new ContentValues();
	  newContact.put("name", name);
	  newContact.put("image", image);
	  newContact.put("health", health);
	
	  open(); // open the database
	  database.insert("monsters", null, newContact);
	  //close(); // close the database
   } // end method insertContact

   // inserts a new contact in the database
   public void updateMonster(long id, String name, String image, String health)
   {
		ContentValues editContact = new ContentValues();
		editContact.put("name", name);
		editContact.put("image", image);
		editContact.put("health", health);

		open(); // open the database
		database.update("monsters", editContact, "_id=" + id, null);
	    //close(); // close the database
   } // end method updateContact
   
   public Cursor getMonsters() 
   {
	      return database.query("monsters", new String[] {"_id", "name", "image", "health"}, 
	    	         null, null, null, null, "name");
   }

   // get a Cursor containing all information about the contact specified
   // by the given id
   public Cursor getOneContact(long id) 
   {
      return database.query(
         "monsters", null, "_id=" + id, null, null, null, null);
   } // end method getOnContact   

   // delete the contact specified by the given String name
   public void deleteContact(long id) 
   {
      open(); // open the database
      database.delete("monsters", "_id=" + id, null);
      close(); // close the database
   } // end method deleteContact
   
   private class DatabaseOpenHelper extends SQLiteOpenHelper 
   {
      // public constructor
      public DatabaseOpenHelper(Context context, String name,
         CursorFactory factory, int version) 
      {
         super(context, name, factory, version);
      } // end DatabaseOpenHelper constructor

      
      @Override
      public void onCreate(SQLiteDatabase db) 
      {
         // query to create a new table named monsters
         String createMonstersTable = "CREATE TABLE monsters" +
            "(_id integer primary key autoincrement," +
            "name TEXT, image TEXT, health TEXT);";
                  
         db.execSQL(createMonstersTable); // execute the query
         
         String createTopscoresTable = "CREATE TABLE topscores" +
                 "(_id integer primary key autoincrement," +
                 "name TEXT, score INT);";
         
         db.execSQL(createTopscoresTable);
         
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, 
          int newVersion) 
      {
      } 
   } 

	public boolean isOpen() 
	{	
		if (database != null)
			return true;
		else
			return false;
	}

} 