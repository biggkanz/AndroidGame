package edu.iastate.fightthings;

import java.lang.reflect.Field;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import edu.iastate.fightthings.R.drawable;
import edu.iastate.fightthings.R.id;
import edu.iastate.fightthings.data.MonsterContent;

/**
 * A fragment representing a single Monster detail screen. This fragment is
 * either contained in a {@link MonsterListActivity} in two-pane mode (on
 * tablets) or a {@link MonsterDetailActivity} on handsets.
 */
public class MonsterDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private MonsterContent.MonsterItem mItem;
	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
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
}
