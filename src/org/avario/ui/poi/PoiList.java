package org.avario.ui.poi;

import java.util.Arrays;
import java.util.List;

import org.avario.R;
import org.avario.engine.DataAccessObject;
import org.avario.engine.PoiProducer;
import org.avario.engine.consumerdef.POIConsumer;
import org.avario.engine.poi.KmlPois;
import org.avario.engine.poi.POI;
import org.avario.engine.poi.PoiManager;
import org.avario.utils.Logger;
import org.avario.utils.UnitsConverter;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

public class PoiList extends ExpandableListActivity implements POIConsumer {
	private static final int ACTIONACTIVEID = 0;
	private static final int ACTIONDELID = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PoiManager.get().readPOIs();
		refreshList();
		registerForContextMenu(getExpandableListView());
		PoiProducer.get().addConsumer(this);
	}

	public synchronized void refreshList() {
		setListAdapter(new PoiListAdapter(this.getApplicationContext()));
	}

	public void importListFromWeb() {
		try {
			Location lastLocation = DataAccessObject.get().getLastlocation();
			if (lastLocation != null) {
				Logger.get().log("Get Sites arround");
				new KmlPois(lastLocation.getLatitude(), lastLocation.getLongitude(), 50) {
					@Override
					public void onPoi(POI... poi) {
						Logger.get().log("Sites arround: " + poi.length);
						for (POI site : poi) {
							site.setName("(www) " + site.getName());
							PoiManager.get().addWebPOI(site);
							refreshList();
						}
					}
				}.start();
			}
		} catch (Exception ex) {
			Logger.get().log("Internat pg sites not available");
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
		int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
		List<POI> pois = Arrays.asList(PoiManager.get().getPOIs().toArray(new POI[0]));
		if (pois.size() > groupPos) {
			POI poi = pois.get(groupPos);
			menu.setHeaderTitle(poi.getName());
		}
		menu.add(0, ACTIONACTIVEID, 0, R.string.setactivepoi);
		menu.add(1, ACTIONDELID, 1, R.string.delpoi);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, Menu.NONE, R.string.addpoi);
		menu.add(1, 1, Menu.NONE, R.string.add_from_web);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			Intent intent = new Intent(this, PoiActivity.class);
			startActivity(intent);
			break;
		case 1:
			importListFromWeb();
			break;
		}

		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int type = ExpandableListView.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			List<POI> pois = Arrays.asList(PoiManager.get().getPOIs().toArray(new POI[0]));
			if (pois.size() > groupPos) {
				POI poi = pois.get(groupPos);
				if (item.getItemId() == ACTIONDELID) {
					PoiManager.get().delPOI(poi.getName());
					Toast.makeText(this, getApplicationContext().getApplicationContext().getString(R.string.poideleted, poi.getName()),
							Toast.LENGTH_SHORT).show();
				} else if (item.getItemId() == ACTIONACTIVEID) {
					PoiManager.get().setActivePOI(poi);
				}
			}
			refreshList();
			return true;
		}
		return false;
	}

	public class PoiListAdapter extends BaseExpandableListAdapter {
		private final Context context;

		protected PoiListAdapter(Context context) {
			this.context = context;
		}

		public Object getChild(int groupId, int childId) {
			List<POI> pois = Arrays.asList(PoiManager.get().getPOIs().toArray(new POI[0]));
			POI poi = pois.get(groupId);
			Location lastLocation = DataAccessObject.get().getLastlocation();
			return context.getApplicationContext().getString(R.string.poidetail, poi.getLatitude(), poi.getLongitude(),
					UnitsConverter.normalizedDistance(poi.distanceTo(lastLocation)));
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
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
			TextView textView = new TextView(PoiList.this);
			textView.setLayoutParams(lp);
			textView.setTextSize(20);
			textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			textView.setPadding(40, 0, 0, 0);
			return textView;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			TextView textView = getGenericView(80);
			textView.setTextSize(20);
			textView.setText(getChild(groupPosition, childPosition).toString());
			return textView;
		}

		public Object getGroup(int groupPosition) {
			POI poi = Arrays.asList(PoiManager.get().getPOIs().toArray(new POI[0])).get(groupPosition);
			POI activePoi = PoiManager.get().getActivePOI();
			int poiTextId = activePoi != null && activePoi.getName().equals(poi.getName()) ? R.string.poiactive : R.string.poinotactive;
			return context.getApplicationContext().getString(poiTextId, poi.getName());
		}

		@Override
		public int getGroupCount() {
			return PoiManager.get().getPOIs().size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			TextView textView = getGenericView(60);
			textView.setText(getGroup(groupPosition).toString());
			return textView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	@Override
	public void notifyWithPOI(POI poi) {
		PoiManager.get().addPOI(poi);
		Toast.makeText(this, getApplicationContext().getString(R.string.poiadded, poi.getName()), Toast.LENGTH_SHORT).show();
		refreshList();
	}
}
