package edu.iastate.fightthings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class MonsterListActivity extends FragmentActivity implements
		MonsterListFragment.Callbacks {


	private boolean mTwoPane;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monster_list);

		if (findViewById(R.id.monster_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((MonsterListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.monster_list))
					.setActivateOnItemClick(true);
		}				

	}


	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(MonsterDetailFragment.ARG_ITEM_ID, id);
			MonsterDetailFragment fragment = new MonsterDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.monster_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, MonsterDetailActivity.class);
			detailIntent.putExtra(MonsterDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
}
