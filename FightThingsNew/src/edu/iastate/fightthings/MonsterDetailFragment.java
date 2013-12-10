package edu.iastate.fightthings;

import java.lang.reflect.Field;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import edu.iastate.fightthings.R.drawable;
import edu.iastate.fightthings.R.id;
import edu.iastate.fightthings.data.MonsterContent;
import edu.iastate.fightthings.game.GameProject;

public class MonsterDetailFragment extends Fragment {

	public static final String ARG_ITEM_ID = "item_id";

	private MonsterContent.MonsterItem mItem;

	public MonsterDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = MonsterContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.fragment_monster_detail, container, false);

		if (mItem != null) 
		{
			((TextView) rootView.findViewById(R.id.name))
				.setText(mItem.name);
			
			((TextView) rootView.findViewById(R.id.health))
				.setText(mItem.health);
			
			((Button) rootView.findViewById(R.id.fightButton)).setOnClickListener(fightButtonOnClickListener);
						
			try 
			{
			    Class<drawable> res = R.drawable.class;
			    Field field = res.getField(mItem.image);
			    int drawableId = field.getInt(null);
			    
			    ((ImageView) rootView.findViewById(R.id.imageView1))
					.setImageDrawable(getResources().getDrawable(drawableId));
			}
			catch (Exception e) {
			    Log.e("MonsterDetail", "Failure to get drawable id from imagename in database.", e);
			}			    
		    
		}
		
		return rootView;
	}	
	
	private OnClickListener fightButtonOnClickListener = new OnClickListener()
	   {
		
	      @Override
	      public void onClick(View v)
	      {
//	         Intent intent = new Intent(Intent.);
//	         intent.setType("image/*");
//	         startActivityForResult(Intent.createChooser(intent, 
//	            getResources().getText(R.string.chooser_image)), PICTURE_ID);
	    	  
	         
	    	  
	    	  Intent detailIntent = new Intent(getActivity(), GameProject.class);
				detailIntent.putExtra(MonsterDetailFragment.ARG_ITEM_ID, mItem.id);
				startActivity(detailIntent);
	      } // end method onClick
	   }; // end OnClickListener addPictureButtonListener
}
