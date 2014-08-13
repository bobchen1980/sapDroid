/**
 * 
 */
package com.sap.sapdroid.views;

import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.sap.ampache.Artist;
import com.sap.ampache.Song;
import com.sap.sapdroid.Controller;
import com.sap.sapdroid.R;
import com.sap.sapdroid.StableArrayAdapter;

/**
 * @author Chen Xiaoliang
 * 
 */
public class ArtistsView extends Fragment {

//	private String urlString;
	private Controller controller;

	/**
	 * 
	 */
	public ArtistsView() {
		// TODO Auto-generated constructor stub
	}

	public static Fragment newInstance(Context context) {
		ArtistsView p = new ArtistsView();
		return p;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		controller = Controller.getInstance();
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ampache_songs, null);
		ListView listview = (ListView) root.findViewById(R.id.songs_listview);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			listview.setFastScrollAlwaysVisible(true);
		}
		if (controller.getServer() != null) {
			// ServerConnector server = controller.getServer();
			// urlString = server.getServer() + "/server/xml.server.php?action=artists&auth=" + server.getAuthKey();
			// if (controller.getArtists().size() != controller.getServer().getAmpacheConnection().getArtists()) {
			// controller.parseArtists(urlString);
			// }
			ArrayList<String> list = new ArrayList<String>();
			for (Artist a : controller.getArtists()) {
				list.add(a.toString());
			}
			StableArrayAdapter adapter = new StableArrayAdapter(getActivity().getApplicationContext(),
					R.layout.content_list_item, list);
			listview.setAdapter(adapter);
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
					Artist a = controller.getArtists().get(position);
					Log.d("get Songs", controller.findSongs(a).toString());
					for (Song s : controller.findSongs(a)) {
						controller.getPlayNow().add(s);
					}
					Context context = view.getContext();
					CharSequence text = "����ӵ����ز����б�";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}

			});
		}
		return root;
	}
}
