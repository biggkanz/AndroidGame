// SpotOn.java
// Activity for the SpotOn app
package edu.iastate.fightthings.game;

import edu.iastate.fightthings.R;
import edu.iastate.fightthings.data.MonsterContent;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.RelativeLayout;

public class GameProject extends FragmentActivity
{
   private GameProjectView view; // displays and manages the game
   
   // called when this Activity is first created
   @Override
   public void onCreate(Bundle savedInstanceState) 
   {
      super.onCreate(savedInstanceState);     
      
      setContentView(R.layout.main);

      // create a new SpotOnView and add it to the RelativeLayout
      RelativeLayout layout = 
         (RelativeLayout) findViewById(R.id.relativeLayout);
//      view = new GameProjectView(this, getPreferences(Context.MODE_PRIVATE), 
//         layout); 
//      layout.addView(view, 0); // add view to the layout
   } // end method onCreate
   
   // called when this Activity moves to the background
//   @Override
//   public void onPause()
//   {
//	   super.onPause();
//	   view.pause(); // release resources held by the View
//   } // end method onPause
//   
//   // called when this Activity is brought to the foreground
//   @Override
//   public void onResume()
//   {
//	   super.onResume();
//	   view.resume(this); // re-initialize resources released in onPause
//   } // end method onResume
} // end class SpotOn

