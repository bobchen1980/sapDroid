/**
 * 
 */
package com.sap.sapdroid.views;


import java.util.ArrayList;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.raptureinvenice.webimageview.image.WebImageView;
import com.sap.ampache.Song;
import com.sap.player.StreamPlayer;
import com.sap.sapdroid.Controller;
import com.sap.sapdroid.R;
import com.sap.sapdroid.StableArrayAdapter;


/**
 * @author Chen Xiaoliang
 * 
 */
public class CurrentPlaylistView extends Fragment {

	protected static final String TAG = "SAP:Play";
	private Controller controller;
	private SeekBar seekBar;
	private TextView duration;
	private TextView currentDuration;
	private TextView songTitle;
	private ListView listview ;
	private Handler playbackHandler;
	private ImageButton  btn_startplay;
	private ImageButton  btn_stopplay;
	private ImageButton  btn_preplay;
	private ImageButton  btn_nextplay;	
	private ImageButton  btn_effect;
	private RadioGroup sap_group;
	private LinearLayout layout_effect;
	private WebImageView song_image ;
	private Handler mHandler;
	private Runnable mRunnable;
	private int mDuration;
	

	/**
	 * 
	 */
	public CurrentPlaylistView() {
		// TODO Auto-generated constructor stub
	}

	public static Fragment newInstance(Context context) {
		CurrentPlaylistView f = new CurrentPlaylistView();
		return f;
	}
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Log.d("SAP","onActivityCreated(savedInstanceState)");		
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		controller = Controller.getInstance();
	
		setLoggingHandlers();
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.current_playlist, null);
				
		listview = (ListView) root.findViewById(R.id.playNow_listview);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			listview.setFastScrollAlwaysVisible(true);
		}
		
		seekBar = (SeekBar) root.findViewById(R.id.playNow_seekbar);
		songTitle = (TextView) root.findViewById(R.id.playNow_song);
		duration = (TextView) root.findViewById(R.id.playNow_duration);
		currentDuration = (TextView) root.findViewById(R.id.playNow_duration_current);
		btn_startplay = (ImageButton ) root.findViewById(R.id.playlist_play);
		btn_stopplay = (ImageButton ) root.findViewById(R.id.playlist_stop);
		btn_preplay = (ImageButton ) root.findViewById(R.id.playlist_pre);
		btn_nextplay = (ImageButton ) root.findViewById(R.id.playlist_next);
		btn_effect = (ImageButton) root.findViewById(R.id.playlist_effect);
		song_image = (WebImageView) root.findViewById(R.id.playNow_image);
		layout_effect = (LinearLayout) root.findViewById(R.id.effect_layout);
		sap_group = (RadioGroup) root.findViewById(R.id.effect_radio);

		
		ArrayList<String> list = new ArrayList<String>();
		
		for (Song s : controller.getPlayNow()) {
			list.add(s.toString());
		}
		
		Log.d("songs:", list.toString());
		StableArrayAdapter adapter = new StableArrayAdapter(getActivity().getApplicationContext(),
				R.layout.content_list_item, list);
		
		listview.setAdapter(adapter);
		
		btn_startplay.setOnClickListener(myOnClickListener);
		btn_stopplay.setOnClickListener(myOnClickListener);
		btn_preplay.setOnClickListener(myOnClickListener);
		btn_nextplay.setOnClickListener(myOnClickListener);
		btn_effect.setOnClickListener(myOnClickListener);
		
		sap_group.setOnCheckedChangeListener(myOnCheckedChangeListener);
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
				controller.setPlayingNow(controller.getPlayNow().get(position));
				try {			
					
					UpdateSongsInfo();					
					startPlaying();
					
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d("Playing now:", controller.getPlayingNow().toString());
			}

		});
		return root;
	}
	
	private void setLoggingHandlers() {
        playbackHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case StreamPlayer.PLAYING_FAILED:
                    	ToastMessage("播放出现故障");
                        break;
                    case StreamPlayer.PLAYING_FINISHED:
                    	updateButtonState(true);
                    	//ToastMessage("播放已经完成");
                        break;
                    case StreamPlayer.PLAYING_STARTED:
                    	updateButtonState(false);
                    	//ToastMessage("准备开始播放");
                        break;
                }
            }
        };
    }	
	
	private void UpdateSongsInfo()
	{
		if( controller.getPlayingNow() == null )
			return;
		
		String title = controller.getPlayingNow().toString();
		songTitle.setText(title + "\n");
		
		String artist = controller.getPlayingNow().getArtist();
		if ( artist != null) {
			songTitle.append(artist + "\n");
		}
		
		String Album = controller.getPlayingNow().getAlbum();
		if ( Album != null) {
			songTitle.append(Album + "\n");
		}
		
		int bitrate = controller.getPlayingNow().getBitrate();
		String szbitrate = String.valueOf(bitrate/1000) + "Kb/s";
		songTitle.append( szbitrate + "\n");
		
		int size = controller.getPlayingNow().getSize();
		String szsize = String.format("%.2fMB", size/(1024.0*1024.0)) ;
		songTitle.append( szsize + "\n");
		
		mDuration = controller.getPlayingNow().getTime();
		String stime = String.format("%02d:%02d", mDuration/60,mDuration%60);
		duration.setText( stime);	
		currentDuration.setText("00:00");
		
		//Log.d("getArt:%s",controller.getPlayingNow().getArt());
		//Bitmap bitmap = getLoacalBitmap("/aa/aa.jpg"); //从本地取图片
		if ( controller.getPlayingNow().getArt() != null )
		{
		    //Bitmap bitmap = utils.getHttpBitmap(controller.getPlayingNow().getArt());
		    //song_image.setImageBitmap(bitmap);	
			Context context = getView().getContext();
			song_image.setImageWithURL(context,controller.getPlayingNow().getArt());
		}
		
		seekBar.setMax(mDuration);
	}
    
    private void ToastMessage(String msg) {
    	//Toast playing state
    	if(msg == null) return;
		Context context = getView().getContext();
		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		toast.show();
    }	
    
    private View.OnClickListener myOnClickListener = new View.OnClickListener() {  
    	@Override
    	public void onClick(View v) { 
    		 switch(v.getId()){
    		 	case R.id.playlist_play:
    		 	{
    		 		startPlaying();    
    		 		break;
    		 	}
    		 	case R.id.playlist_stop:
    		 	{
    		 		stopPlaying();
    		 		break;
    		 	}
    		 	case R.id.playlist_pre:
    		 	{
    		 		prePlaying();
    		 		break;
    		 	} 
    		 	case R.id.playlist_next:
    		 	{
    		 		nextPlaying();
    		 		break;
    		 	}   
    		 	case R.id.playlist_effect:
    		 	{
    		 		applyEffect();
    		 		break;
    		 	}     		 	
    		 }
    	}	
    };
    
    private void applyEffect()
    {
    	int curstatus = StreamPlayer.SAP_NONE;
    	if( layout_effect.getVisibility() == View.GONE )
 		{
    		layout_effect.setVisibility(View.VISIBLE);
    		
    		if (sap_group.getCheckedRadioButtonId() == R.id.radio_small ) curstatus = StreamPlayer.SAP_ROOM;
    		if (sap_group.getCheckedRadioButtonId() == R.id.radio_medium ) curstatus = StreamPlayer.SAP_STADIUM;
    		if (sap_group.getCheckedRadioButtonId() == R.id.radio_large ) curstatus = StreamPlayer.SAP_CINEMA;	
    		
    		if( controller.getPlayingNow() != null ) 
    		{
    			controller.getMediaPlayer().setCurrentEx(curstatus);
    		}
    		

 			return;
 		}
 		if( layout_effect.getVisibility() == View.VISIBLE)
 		{
 			layout_effect.setVisibility(View.GONE);
 			
    		if( controller.getPlayingNow() != null) 
    			controller.getMediaPlayer().setCurrentEx(StreamPlayer.SAP_NONE);
    		
 			return;
 		} 		
    }
    
    private RadioGroup.OnCheckedChangeListener myOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {  
    	@Override
    	public void onCheckedChanged(RadioGroup group, int checkedId) {
    		switch(group.getCheckedRadioButtonId()){
    			case R.id.radio_small:
    			{
    	    		if( controller.getPlayingNow() != null ) 
    	    			controller.getMediaPlayer().setCurrentEx(StreamPlayer.SAP_ROOM);
    				break;
    			}
    			case R.id.radio_medium:
    			{
    				if( controller.getPlayingNow() != null ) 
    					controller.getMediaPlayer().setCurrentEx(StreamPlayer.SAP_STADIUM);
    				break;
    			}
    			case R.id.radio_large:
    			{				
    				if( controller.getPlayingNow() != null ) 
    					controller.getMediaPlayer().setCurrentEx(StreamPlayer.SAP_CINEMA);
    				break;
    			}   			
    		}
    	}
    };
  
    private void updateButtonState(boolean isstate)
    {
    	btn_startplay.setEnabled(isstate);
    	btn_stopplay.setEnabled(!isstate);
    	
    	if( !isstate )  startProgressBar();
    	else  stopProgressBar();
    		
    }
    
    private void startProgressBar()
    {
		mHandler = new Handler();
		mRunnable = new Runnable() {
			@Override
			public void run() {
				if ( controller.getMediaPlayer().getCurrentPosition() <= mDuration  ) 
				{
					currentDuration.setText( String.format("%02d:%02d", controller.getMediaPlayer().getCurrentPosition()/60,controller.getMediaPlayer().getCurrentPosition()%60));
					seekBar.setProgress(controller.getMediaPlayer().getCurrentPosition());
				}
				
				mHandler.postDelayed(this, 1000);
			}
			
		};
		
		mHandler.post(mRunnable);
    }
    
    private void stopProgressBar()
    {
    	if(mHandler != null)
    	{
	    	currentDuration.setText("00:00");
	    	seekBar.setProgress(0);
	    	mHandler.removeCallbacks(mRunnable);
    	}
    }
    
    private void stopPlaying()
    {    	
    	if( controller.getPlayingNow() == null ) return;
 
    	controller.getMediaPlayer().stop();
    }
	
	private void startPlaying()
	{		
		if( controller.getPlayingNow() == null )
		{
			if(controller.getPlayNow().size() > 0)
			{
				listview.setSelection(0);
				controller.setPlayingNow(controller.getPlayNow().get(0));
				UpdateSongsInfo();
			}
			else return;
		}
		
		controller.getMediaPlayer().reset();
		controller.getMediaPlayer().prepare(playbackHandler);
		controller.getMediaPlayer().setSource(controller.getPlayingNow().getUrl());
		controller.getMediaPlayer().start();
        
	}  
	
	private void prePlaying()
	{	
		if( (controller.getPlayNowPosition() - 1) >= 0 )
		{
			listview.setSelection(listview.getCheckedItemPosition() - 1);
			controller.setPlayNowPosition(controller.getPlayNowPosition() - 1);
			controller.setPlayingNow(controller.getPlayNow().get(controller.getPlayNowPosition()));
			
			UpdateSongsInfo();
			
			startPlaying();
		}
	}
	
	private void nextPlaying()
	{
		if( (controller.getPlayNowPosition() + 1) < controller.getPlayNow().size())
		{
			listview.setSelection(listview.getCheckedItemPosition() + 1);
			controller.setPlayNowPosition(controller.getPlayNowPosition() + 1);
			controller.setPlayingNow(controller.getPlayNow().get(controller.getPlayNowPosition()));
			
			UpdateSongsInfo();
			
			startPlaying();
		}
		
	}	

    public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // TODO Auto-generated method stub
	    if (keyCode == event.KEYCODE_1) {
	    	startPlaying();
		}	    	
	    if (keyCode == event.KEYCODE_4) {
	    	stopPlaying();
		}	
	    if (keyCode == event.KEYCODE_2) {
	    	nextPlaying();
		}	
	    if (keyCode == event.KEYCODE_3) {
	    	prePlaying();
		}		    
	    if (keyCode == event.KEYCODE_6) {
	    	applyEffect();
	    }
	    if (keyCode == event.KEYCODE_7) {
	    	if( layout_effect.getVisibility() == View.VISIBLE)
	    		((RadioButton) sap_group.findViewById(R.id.radio_small)).setChecked(true);  
		}	
	    if (keyCode == event.KEYCODE_8) {
	    	if( layout_effect.getVisibility() == View.VISIBLE)
	    		((RadioButton) sap_group.findViewById(R.id.radio_medium)).setChecked(true); 
		}
		if (keyCode == event.KEYCODE_9) {
			if( layout_effect.getVisibility() == View.VISIBLE)
				((RadioButton) sap_group.findViewById(R.id.radio_large)).setChecked(true); 
		}	

	    return true;
	}	
}
