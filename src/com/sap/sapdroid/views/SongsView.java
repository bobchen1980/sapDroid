/**
 * 
 */
package com.sap.sapdroid.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Toast;

import com.raptureinvenice.webimageview.image.WebImageView;
import com.sap.ampache.Song;
import com.sap.sapdroid.Controller;
import com.sap.sapdroid.R;
import com.sap.sapdroid.StableArrayAdapter;

/**
 * @author Chen Xiaoliang
 * 
 */
public class SongsView extends Fragment {

	// private String urlString;
	private Controller controller;
	private ListView listview;

	/**
	 * 
	 */
	public SongsView() {
		// TODO Auto-generated constructor stub
	}

	public static Fragment newInstance(Context context) {
		SongsView p = new SongsView();
		return p;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Log.d("SAP","Songs onActivityCreated(savedInstanceState)");		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		controller = Controller.getInstance();
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ampache_songs, null);
		
		listview = (ListView) root.findViewById(R.id.songs_listview);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			listview.setFastScrollAlwaysVisible(true);
		}
		
		if (controller.getServer() != null) {
			ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
						
			for (Song s : controller.getSongs()) {
				HashMap<String,Object> map = new HashMap<String, Object>();
				map.put("list_image", s.getArt());
				map.put("list_title", s.toString()); 
				map.put("list_info", String.valueOf(s.getBitrate()/1000) + "Kb/s"); 
				list.add(map); 
			}
			
			SimpleAdapter adapter = new SimpleAdapter(getActivity().getApplicationContext(),list,R.layout.songs_list_item,new String[]{"list_image","list_title","list_info"},new int[]{R.id.list_image,R.id.list_title,R.id.list_info});             

			adapter.setViewBinder(new ViewBinder() {
			  
	            @Override
	            public boolean setViewValue(View view, Object data,
	                    String textRepresentation) {
	                if (view instanceof WebImageView && data instanceof String) {
	                	WebImageView iv = (WebImageView) view;
	                	Context context = getView().getContext();
	                	iv.setImageWithURL(context,(String) data);
	                    return true;
	                }
	                return false;
	            }
	        });			
			
			listview.setAdapter(adapter);  
			
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
					Log.d("Play now added:", controller.getSongs().get(position).toString());
					controller.getPlayNow().add(controller.getSongs().get(position));
					Context context = view.getContext();
					CharSequence text = "已添加到本地播放列表";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}
			 				

			});			
		}
		/*if (controller.getServer() != null) {
			ArrayList<String> list = new ArrayList<String>();
			for (Song s : controller.getSongs()) {
				list.add(s.toString());
			}
			StableArrayAdapter adapter = new StableArrayAdapter(getActivity().getApplicationContext(),
					R.layout.content_list_item, list);
			listview.setAdapter(adapter);
			listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
					Log.d("Play now added:", controller.getSongs().get(position).toString());
					controller.getPlayNow().add(controller.getSongs().get(position));
					Context context = view.getContext();
					CharSequence text = "已添加到本地播放列表";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
				}

			});
		}*/
		return root;
	}
	
}
