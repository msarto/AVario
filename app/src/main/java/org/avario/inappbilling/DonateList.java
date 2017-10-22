package org.avario.inappbilling;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.inappbilling.util.SkuDetails;
import org.avario.utils.Constants;
import org.avario.utils.Logger;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DonateList extends ExpandableListActivity {

	protected List<SkuDetails> donations = new ArrayList<SkuDetails>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Map<String, SkuDetails> items = Donate.get().getItems();
		for (String key : Constants.SKUS) {
			SkuDetails detail = items.get(key);
			donations.add(detail);
		}
		// TracksManager.get().readTracks();
		setListAdapter(new DonateListAdapter(this.getApplicationContext()));
		registerForContextMenu(getExpandableListView());
	}

	protected class DonateListAdapter extends BaseExpandableListAdapter {

		private final Context context;

		protected DonateListAdapter(Context context) {
			this.context = context;
		}

		public Object getChild(int groupId, int childId) {
			return donations.get(groupId).getDescription();
		}

		public long getChildId(int groupId, int childPosition) {
			return childPosition;
		}

		/*
		 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
		 */
		public int getChildrenCount(int groupId) {
			return 1;
		}

		private TextView getGenericView() {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			TextView textView = new TextView(DonateList.this);
			textView.setLayoutParams(lp);
			textView.setTextSize(16);
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.DISPLAY_CLIP_HORIZONTAL
					| Gravity.DISPLAY_CLIP_VERTICAL | Gravity.LEFT);
			return textView;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#getChildView(int, int,
		 * boolean, android.view.View, android.view.ViewGroup)
		 */
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {
			TextView textView = getGenericView();
			float density = textView.getResources().getDisplayMetrics().density;
			textView.setPadding(Math.round(density * 16), Math.round(10 * density), Math.round(density),
					Math.round(10 * density));
			textView.setText(getChild(groupPosition, childPosition).toString());
			return textView;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#getGroup(int)
		 */
		public Object getGroup(int groupPosition) {
			return donations.get(groupPosition).getTitle().replace("(Paragliding Altivario)", "");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#getGroupCount()
		 */
		@Override
		public int getGroupCount() {
			return donations.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#getGroupId(int)
		 */
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean,
		 * android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			RelativeLayout layout = null;
			try {
				LayoutInflater mInflater = (LayoutInflater) AVarioActivity.CONTEXT
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				layout = (RelativeLayout) mInflater.inflate(R.layout.widget_donate, parent, false);
				TextView donationText = (TextView) layout.findViewById(R.id.textdonate);
				donationText.setText(getGroup(groupPosition).toString());
				final SkuDetails sku = donations.get(groupPosition);
				Button donateButton = (Button) layout.findViewById(R.id.donatebutton);
				donateButton.setText(sku.getPrice());
				donateButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						Donate.get().donate(sku.getSku());
						// Toast.makeText(AVarioActivity.CONTEXT, "Donate " +
						// sku.getSku(), Toast.LENGTH_SHORT).show();
					}
				});
			} catch (Exception e) {
				Logger.get().log("Error creating seek bar preference", e);
			}
			return layout;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
		 */
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
}
