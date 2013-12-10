// SpotOnView.java
// View that displays and manages the game
// EDITED FOR PROJECT!
package edu.iastate.fightthings.game;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

import edu.iastate.fightthings.MonsterDetailFragment;
import edu.iastate.fightthings.R;
import edu.iastate.fightthings.R.drawable;
import edu.iastate.fightthings.data.MonsterContent;
import edu.iastate.fightthings.data.TopscoreContent;

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
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameProjectView extends Fragment
{
   private int monsterHealth;
   
   private int viewWidth; // stores the width of this View
   private int viewHeight; // stores the height of this view
   
   private final Queue<ImageView> monsters = new ConcurrentLinkedQueue<ImageView>();
   private final Queue<ImageView> images = new ConcurrentLinkedQueue<ImageView>(); 
   private final Queue<Animator> animators =  new ConcurrentLinkedQueue<Animator>(); 
   
   private TextView monsterNameTextView; // displays high score
   private TextView monsterHealthTextView; // displays current score
   private TextView killedTextView; // displays current level
   private RelativeLayout relativeLayout; // displays spots
   private LayoutInflater layoutInflater; // used to inflate GUIs

   // time in milliseconds for animations
   private static final int MONSTER_ARRIVE_ANIMATION_DURATION = 2000;
   private static final int FIREBALL_ANIMATION_DURATION = 500;   
   
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
   
   public static final String ARG_ITEM_ID = "item_id";
   private MonsterContent.MonsterItem mItem;
   
   public GameProjectView()
   {   }
   
   @Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) 
		{
			mItem = MonsterContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
	}
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
   {  
	   View rootView = inflater.inflate(R.layout.main, container, false);
	   
		monsterNameTextView = ((TextView) rootView.findViewById(R.id.monsterNameTextView));
		monsterHealthTextView = ((TextView) rootView.findViewById(R.id.monsterHealthTextView));
		relativeLayout = ((RelativeLayout) rootView.findViewById(R.id.relativeLayout));
		killedTextView = ((TextView) rootView.findViewById(R.id.killedTextView));
	   
		monsterHealth = 100;
	      
		// save LayoutInflater
		layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		spotHandler = new Handler(); // used to add spots when game starts
		
		rootView.setOnTouchListener(onTouchListener);
		
		initializeSoundEffects(getActivity());
	      
		return rootView;
   }

   // called by the SpotOn Activity when it receives a call to onPause
   public void pause()
   {
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
      monsterNameTextView.setText(mItem.name);
      monsterHealthTextView.setText(getResources().getString(R.string.game_monster_health) + monsterHealth);
      killedTextView.setText(getResources().getString(R.string.game_killed) + " " + "1");
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
      
      fireball.setImageResource(R.drawable.ic_launcher); 	      
      
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
	   viewWidth = this.getView().getWidth();
	   viewHeight = this.getView().getHeight();
	   
      int x = viewWidth / 2 - MONSTER_DIAMETER / 2;
      int y = viewHeight / 2 - MONSTER_DIAMETER / 2;
      
      // create new spot
      final ImageView monster = (ImageView) layoutInflater.inflate(R.layout.untouched, null);
      
      
      monsters.add(monster); // add the new spot to our list of spots
      relativeLayout.addView(monster); // add spot to the screen
      
      
      monster.setLayoutParams(new RelativeLayout.LayoutParams(MONSTER_DIAMETER, MONSTER_DIAMETER));      
      
		try 
		{
		    Class<drawable> res = R.drawable.class;
		    Field field = res.getField(mItem.image);
		    int drawableId = field.getInt(null);
		    
		    monster.setImageDrawable(getResources().getDrawable(drawableId));
		}
		catch (Exception e) {
		    Log.e("MonsterDetail", "Failure to get drawable id from imagename in database.", e);
		}	
      
      monsterHealth = Integer.parseInt(mItem.health);
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
	  
	  displayScores();
            
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
   
	private OnTouchListener onTouchListener = new OnTouchListener()
	{

		@Override
		public boolean onTouch(View v, MotionEvent event) 
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
	   }; 
   
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
		
	TopscoreContent.setContext(getActivity());
	   
	 Builder dialogBuilder = new AlertDialog.Builder(getActivity());
	 dialogBuilder.setTitle("Top Scores");
	 dialogBuilder.setAdapter(new ArrayAdapter<TopscoreContent.TopscoreItem>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, TopscoreContent.ITEMS), null);
	 dialogBuilder.setPositiveButton(R.string.reset_game,
	    new DialogInterface.OnClickListener()
	    {
	       public void onClick(DialogInterface dialog, int which)
	       { 
	    	  getFragmentManager().popBackStackImmediate(); 
	       } 
	    } 
	 ); 
	 dialogBuilder.show(); 
   }
} 

