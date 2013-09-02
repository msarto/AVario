package org.avario.ui.tracks;

import java.io.File;
import java.util.List;

import org.avario.R;
import org.avario.engine.tracks.TrackInfo;
import org.avario.engine.tracks.TracksManager;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

public class TracksList extends ExpandableListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TracksManager.get().readTracks();
		setListAdapter(new TracksListAdapter(this.getApplicationContext()));
		registerForContextMenu(getExpandableListView());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.setHeaderTitle(R.string.tracks_list);
		menu.add(0, 0, 0, R.string.tracks_del);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();

		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			List<TrackInfo> tracks = TracksManager.get().getTracks();
			if (tracks.size() > groupPos) {
				String trackFile = tracks.get(groupPos).getTrackFileName();
				(new File(trackFile)).delete();
				trackFile = trackFile.replace(".meta", ".igc");
				(new File(trackFile)).delete();
				Toast.makeText(this, R.string.trkdeleted, Toast.LENGTH_SHORT).show();
				TracksManager.get().readTracks();
				setListAdapter(new TracksListAdapter(this.getApplicationContext()));
			}
			return true;
		}
		return false;
	}

	protected class TracksListAdapter extends BaseExpandableListAdapter {

		private final Context context;

		protected TracksListAdapter(Context context) {
			this.context = context;
		}

		public Object getChild(int groupId, int childId) {

			List<TrackInfo> tracks = TracksManager.get().getTracks();
			TrackInfo track = tracks.get(groupId);
			return buildTrackInfoStr(track);
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

		private TextView getGenericView(int height) {
			// Layout parameters for the ExpandableListView
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);

			TextView textView = new TextView(TracksList.this);
			textView.setLayoutParams(lp);
			textView.setTextSize(20);
			// Center the text vertically
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			// Set the text starting position
			textView.setPadding(36, 0, 0, 0);
			return textView;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#getChildView(int, int,
		 * boolean, android.view.View, android.view.ViewGroup)
		 */
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			TextView textView = getGenericView(200);
			textView.setTextSize(18);
			textView.setText(getChild(groupPosition, childPosition).toString());
			return textView;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#getGroup(int)
		 */
		public Object getGroup(int groupPosition) {
			List<TrackInfo> tracks = TracksManager.get().getTracks();
			TrackInfo track = tracks.get(groupPosition);
			return UnitsConverter.humanTime(track.getFlightStart());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#getGroupCount()
		 */
		@Override
		public int getGroupCount() {
			return TracksManager.get().getTracks().size();
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
			TextView textView = getGenericView(60);
			textView.setText(getGroup(groupPosition).toString());
			return textView;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
		 */
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
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

		/**
		 * @param track
		 * @return String
		 */
		private String buildTrackInfoStr(TrackInfo track) {
			StringBuilder sb = new StringBuilder();
			sb.append(context.getString(R.string.trkstart, UnitsConverter.humanTime(track.getFlightStart()))).append("\r\n");
			sb.append(context.getString(R.string.trkdur, UnitsConverter.humanTimeSpan(track.getFlightDuration()))).append("\r\n");
			sb.append(
					context.getString(R.string.trkdist,
							StringFormatter.twoDecimals(UnitsConverter.toPreferredLong(track.getFlightLenght() / 1000f)),
							UnitsConverter.preferredDistLong())).append("\r\n");
			sb.append(
					context.getString(R.string.trkstartalt,
							StringFormatter.noDecimals(UnitsConverter.toPreferredShort(track.getStartAlt())),
							UnitsConverter.preferredDistShort())).append("\r\n");
			sb.append(
					context.getString(R.string.trkmaxalt,
							StringFormatter.noDecimals(UnitsConverter.toPreferredShort(track.getHighestAlt())),
							UnitsConverter.preferredDistShort())).append("\r\n");
			sb.append(
					context.getString(R.string.trkmaxvario,
							StringFormatter.twoDecimals(UnitsConverter.toPreferredVSpeed(track.getHighestVSpeed())),
							UnitsConverter.preferredDistShort())).append("\r\n");
			sb.append(
					context.getString(R.string.trkminvario,
							StringFormatter.twoDecimals(UnitsConverter.toPreferredVSpeed(track.getHighestSink())),
							UnitsConverter.preferredDistShort())).append("\r\n");
			float speed = UnitsConverter.msTokmh(track.getMaxSpeed());
			sb.append(
					context.getString(R.string.trkmaxspeed, StringFormatter.noDecimals(UnitsConverter.toPreferredLong(speed)),
							UnitsConverter.preferredDistLong())).append("\r\n");
			sb.append(context.getString(R.string.trkendalt, StringFormatter.noDecimals(UnitsConverter.toPreferredShort(track.getEndAlt())),
					UnitsConverter.preferredDistShort()));
			return sb.toString();
		}
	}
}
