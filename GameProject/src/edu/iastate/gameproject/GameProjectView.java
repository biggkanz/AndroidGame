// SpotOnView.java
// View that displays and manages the game
// EDITED FOR PROJECT!
package edu.iastate.gameproject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

import edu.iastate.gameproject.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameProjectView extends View 
{
   // constant for accessing the high score in SharedPreference
   private static final String HIGH_SCORE = "HIGH_SCORE";
   private SharedPreferences preferences; // stores the high score

   // variables for managing the game
   private int playerHealth;
   private int monsterHealth;
   
//   private int spotsTouched; // number of spots touched
//   private int score; // current score
//   private int level; // current level
   private int viewWidth; // stores the width of this View
   private int viewHeight; // stores the height of this view
//   private boolean gameOver; // whether the game has ended
   private boolean gamePaused; // whether the game has ended
   private boolean dialogDisplayed; // whether the game has ended
//   private int highScore; // the game's all time high score
//   
   // collections of spots (ImageViews) and Animators 
   private final Queue<ImageView> monsters = new ConcurrentLinkedQueue<ImageView>();
   private final Queue<ImageView> images = new ConcurrentLinkedQueue<ImageView>(); 
   private final Queue<Animator> animators =  new ConcurrentLinkedQueue<Animator>(); 
   
   private TextView monsterHealthTextView; // displays high score
   private TextView playerHealthTextView; // displays current score
   private TextView levelTextView; // displays current level
   private LinearLayout livesLinearLayout; // displays lives remaining
   private RelativeLayout relativeLayout; // displays spots
   private Resources resources; // used to load resources
   private LayoutInflater layoutInflater; // used to inflate GUIs

   // time in milliseconds for animations
   private static final int MONSTER_ARRIVE_ANIMATION_DURATION = 2000;
   private static final int FIREBALL_ANIMATION_DURATION = 500;   
   
   private static final Random random = new Random(); // for random coords
   
   private static final float FIREBALL_SCALE_X = .25f; // end animation x scale
   private static final float FIREBALL_SCALE_Y = .25f; // end animation y scale
   private static final float FIREBALL_SCALE_X_START = 2.00f; // beginning animation x scale
   private static final float FIREBALL_SCALE_Y_START = 2.00f; // beginning animation y scale
   private static final int FIREBALL_DAMAGE = 50; // the amount of damage a fireball does
   
   private float targetX; // where the mouse was clicked X
   private float targetY; // where the mouse was clicked Y
   private boolean monsterWasHit;
   
   
   private static final int MONSTER_DIAMETER = 100; // size of monster for detecting touch events
   private static final float MONSTER_SCALE_X = 4.00f; // end animation x scale
   private static final float MONSTER_SCALE_Y = 4.00f; // end animation y scale
   private static final float MONSTER_SCALE_X_START = .25f; // beginning animation x scale
   private static final float MOSNTER_SCALE_Y_START = .25f; // beginning animation y scale
      
   private Handler spotHandler; // adds new spots to the game 

   // sound IDs, constants and variables for the game's sounds
   private static final int HIT_SOUND_ID = 1;
   private static final int MISS_SOUND_ID = 2;
   private static final int DISAPPEAR_SOUND_ID = 3;
   private static final int SOUND_PRIORITY = 1; 
   private static final int SOUND_QUALITY = 100;
   private static final int MAX_STREAMS = 4;
   private SoundPool soundPool; // plays sound effects
   private int volume; // sound effect volume
   private Map<Integer, Integer> soundMap; // maps ID to soundpool
   
   // constructs a new SpotOnView
   public GameProjectView(Context context, SharedPreferences sharedPreferences,RelativeLayout parentLayout)
   {
      super(context);
      
      monsterHealth = 100;
      playerHealth = 100;
      
      // load the high score
      preferences = sharedPreferences;
//      highScore = preferences.getInt(HIGH_SCORE, 0);

      // save Resources for loading external values
      resources = context.getResources();

      // save LayoutInflater
      layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      // get references to various GUI components
      relativeLayout = parentLayout;
      livesLinearLayout = (LinearLayout) relativeLayout.findViewById(R.id.lifeLinearLayout); 
      monsterHealthTextView = (TextView) relativeLayout.findViewById(R.id.highScoreTextView);
      playerHealthTextView = (TextView) relativeLayout.findViewById(R.id.scoreTextView);
      levelTextView = (TextView) relativeLayout.findViewById(R.id.levelTextView);

      spotHandler = new Handler(); // used to add spots when game starts
   } // end SpotOnView constructor

   // store SpotOnView's width/height
   @Override
   protected void onSizeChanged(int width, int height, int oldw, int oldh)
   {
      viewWidth = width; // save the new width
      viewHeight = height; // save the new height
   } // end method onSizeChanged

   // called by the SpotOn Activity when it receives a call to onPause
   public void pause()
   {
      gamePaused = true;
      soundPool.release(); // release audio resources
      soundPool = null;
      cancelAnimations(); // cancel all outstanding animations
   } // end method pause

   // cancel animations and remove ImageViews representing spots
   private void cancelAnimations()
   {
      // cancel remaining animations
      for (Animator animator : animators)
         animator.cancel();

      // remove images from the screen
      for (ImageView view : images)
         relativeLayout.removeView(view);
      
      // remove monsters from the screen
      for (ImageView view : monsters)
         relativeLayout.removeView(view);

      spotHandler.removeCallbacks(addMonsterRunnable);
      spotHandler.removeCallbacks(addFireballRunnable);
      
      animators.clear();
      monsters.clear();
      images.clear();
   } // end method cancelAnimations
   
   // called by the SpotOn Activity when it receives a call to onResume
   public void resume(Context context)
   {
      gamePaused = false;
      initializeSoundEffects(context); // initialize app's SoundPool

      if (!dialogDisplayed)
         resetGame(); // start the game
   } // end method resume

   // start a new game
   public void resetGame()
   {
	  monsterHealth = 100;
	   
	  images.clear(); // empty the List of spots
	  animators.clear(); // empty the List of Animators
	  livesLinearLayout.removeAllViews(); // clear old lives from screen
	  displayScores(); // display scores and level            
   } 

   // create the app's SoundPool for playing game audio
   private void initializeSoundEffects(Context context)
   {
      // initialize SoundPool to play the app's three sound effects
      soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, SOUND_QUALITY);

      // set sound effect volume
      AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
      volume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);

      // create sound map
      soundMap = new HashMap<Integer, Integer>(); // create new HashMap

      // add each sound effect to the SoundPool
      soundMap.put(HIT_SOUND_ID, soundPool.load(context, R.raw.hit, SOUND_PRIORITY));
      soundMap.put(MISS_SOUND_ID, soundPool.load(context, R.raw.miss, SOUND_PRIORITY));
      soundMap.put(DISAPPEAR_SOUND_ID, soundPool.load(context, R.raw.disappear, SOUND_PRIORITY));
   } // end method initializeSoundEffect

   // display scores and level
   private void displayScores()
   {
      // display the high score, current score and level
      monsterHealthTextView.setText(resources.getString(R.string.monster_health) + " " + monsterHealth);
      playerHealthTextView.setText(resources.getString(R.string.player_health) + " " + playerHealth);
      levelTextView.setText(resources.getString(R.string.level) + " " + "1");
   } // end function displayScores

   // Runnable used to add new spots to the game at the start
   private Runnable addMonsterRunnable = new Runnable()
   {
	      public void run()
	      {
	         addNewMonster(); // add a new monster
	      } 
   }; 
     
   private Runnable addFireballRunnable = new Runnable()
   {
	   public void run()
	      {
	         addNewFireball(); 
	      } 
   };
   
   public void addNewFireball()
   {
	   float x = targetX;
	   float y = targetY;
	      
	   // create new fireball
      final ImageView fireball = (ImageView) layoutInflater.inflate(R.layout.untouched, null);
      
      
      images.add(fireball); // add the new spot to our list of spots
      relativeLayout.addView(fireball); // add spot to the screen
            
      fireball.setLayoutParams(new RelativeLayout.LayoutParams(MONSTER_DIAMETER, MONSTER_DIAMETER));      
      
      fireball.setImageResource(R.drawable.fireball);	 	      
      
      // fireball starts at bottom center of the screen
	  fireball.setX(viewWidth / 2); // set spot's starting x location
	  fireball.setY(viewHeight + 200); // set spot's starting y location (+200 to push it off the edge of screen)
	  
	  // firebal goes from small to big
	  fireball.setScaleX(FIREBALL_SCALE_X_START);
	  fireball.setScaleY(FIREBALL_SCALE_Y_START);

	  fireball.animate()
	  .x(x)
	  .y(y)
	  .scaleX(FIREBALL_SCALE_X)
	  .scaleY(FIREBALL_SCALE_Y)
	  .setDuration(FIREBALL_ANIMATION_DURATION)
	  .setListener(new FireballAnimatorListenerAdapter(fireball)); 
   }
   
   public void addNewMonster()
   {      
      int x = viewWidth / 2 - MONSTER_DIAMETER / 2;
      int y = viewHeight / 2 - MONSTER_DIAMETER / 2;
      
      // create new spot
      final ImageView monster = (ImageView) layoutInflater.inflate(R.layout.untouched, null);
      
      
      monsters.add(monster); // add the new spot to our list of spots
      relativeLayout.addView(monster); // add spot to the screen
      
      
      monster.setLayoutParams(new RelativeLayout.LayoutParams(MONSTER_DIAMETER, MONSTER_DIAMETER));      
      
      monster.setImageResource(R.drawable.seamonster);	  
      
      monster.setOnClickListener( // listens for spot being clicked
         new OnClickListener()
         {            
            public void onClick(View v)
            {
               touchedMonster(monster); // handle touched spot
            } 
         }
      );       
      
	  monster.setX(x); // set spot's starting x location
	  monster.setY(y); // set spot's starting y location
	  monster.setScaleX(MONSTER_SCALE_X_START);
	  monster.setScaleY(MOSNTER_SCALE_Y_START);

	  monster.animate()
	  .scaleX(MONSTER_SCALE_X)
	  .scaleY(MONSTER_SCALE_Y)
	  .setDuration(MONSTER_ARRIVE_ANIMATION_DURATION)
	  .setListener(new MonsterAnimatorListenerAdapter(monster)); 
            
   } // end addNewSpot method
   
   // Add and remove animator from the animators list
   class MonsterAnimatorListenerAdapter extends AnimatorListenerAdapter
   {
	  ImageView monster;
	  
	  public MonsterAnimatorListenerAdapter(ImageView spot)
	  {
		  this.monster = spot;
	  }
	   
	  @Override
	  public void onAnimationStart(Animator animation)
	  {
	     animators.add(animation); // save for possible cancel
	  } // end method onAnimationStart
	
	  public void onAnimationEnd(Animator animation)
	  {
	     animators.remove(animation);   
	  } 
   } 
   
   
   class FireballAnimatorListenerAdapter extends AnimatorListenerAdapter
   {
	  ImageView fireball;
	  
	  public FireballAnimatorListenerAdapter(ImageView fireball)
	  {
		  this.fireball = fireball;
	  }
	   
	  @Override
	  public void onAnimationStart(Animator animation)
	  {
	     animators.add(animation); // save for possible cancel
	     
	      // play the hit sounds
	      if (soundPool != null)
	         soundPool.play(MISS_SOUND_ID, volume, volume, 
	            SOUND_PRIORITY, 0, 1f);
	  }
	
	  public void onAnimationEnd(Animator animation)
	  {
	     animators.remove(animation);   
	     images.remove(fireball);
	     relativeLayout.removeView(fireball);	 
	     
	     if(monsterWasHit)
	     {
	    	 monsterWasHit = false;
	    	 
	         if(monsterHealth - FIREBALL_DAMAGE <= 0)
		  	   {		  		   
		  		   relativeLayout.removeView(monsters.element());
		  		   monsters.clear();
		  		   monsterHealth = 0;	
		  		   
		  		   gameOver();
		  	   }
		  	   else
		  		   monsterHealth -= FIREBALL_DAMAGE;
	         
		      // play the hit sounds
		      if (soundPool != null)
		         soundPool.play(HIT_SOUND_ID, volume, volume, 
		            SOUND_PRIORITY, 0, 1f);
	  	   
	  	   	displayScores();
	     }
	  } 	   
   }   

   // called when the user touches the screen, but not a spot
   @Override
   public boolean onTouchEvent(MotionEvent event)
   {
	  if(animators.isEmpty())
	  {
	      // Add a monster if there are none
		  if(monsters.isEmpty())
			  spotHandler.post(addMonsterRunnable);
		  else
		  {
			  targetX = event.getX();
			  targetY = event.getY();
			  
			  spotHandler.post(addFireballRunnable);		  
		  }
	  }      
	  
	  return true;
   } 
   
   // Called when a monster is touched.
   private void touchedMonster(ImageView monster)
   {		   
	   // Only respond to touches if all the animations are finished.
	   if(animators.isEmpty())
	   {	   
		   spotHandler.post(addFireballRunnable);
		   
		   targetX = monster.getX();
		   targetY = monster.getY();
		   monsterWasHit = true;		   
	 
	      displayScores(); // update score/level on the screen
	   }      
   } 

   // called when a spot finishes its animation without being touched
   public void gameOver()
   {      
      // if the game has been lost
      if (livesLinearLayout.getChildCount() == 0)
      {          
         // display a high score dialog
         Builder dialogBuilder = new AlertDialog.Builder(getContext());
         dialogBuilder.setTitle(R.string.game_over);
         dialogBuilder.setMessage(resources.getString(R.string.score) +
            " " + playerHealth);
         dialogBuilder.setPositiveButton(R.string.reset_game,
            new DialogInterface.OnClickListener()
            {
               public void onClick(DialogInterface dialog, int which)
               { 
                  displayScores(); // ensure that score is up to date
                  dialogDisplayed = false;
                  resetGame(); // start a new game
               } // end method onClick
            } // end DialogInterface
         ); // end call to dialogBuilder.setPositiveButton
         dialogDisplayed = true;
         dialogBuilder.show(); // display the reset game dialog
      } // end if
      else // remove one life   
      {
         livesLinearLayout.removeViewAt( // remove life from screen
            livesLinearLayout.getChildCount() - 1); 
         addNewMonster(); // add another spot to game
      } // end else
   } // end method missedSpot
} // end class SpotOnView

