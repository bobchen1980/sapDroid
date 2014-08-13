package com.sap.sapdroid;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
//import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sapdroid.R;
import com.sap.sapdroid.views.CurrentPlaylistView;
import com.sap.ampache.ServerConnection;

public class MainActivity extends FragmentActivity {

	private String[] mNavItems;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	//private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private Controller controller;
	private int mProgressStatus = 0;
	private ProgressBar mProgress;
	private TextView loadingText;
	private LinearLayout progressLinearLayout;
	private boolean syncAlbums;
	private boolean syncArtists;
	private boolean syncPlaylists;
	private boolean syncSongs;
	private int syncFilesCount;
	private String syncText;
	private FrameLayout contentFrame;
	private Fragment currentFrame; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		controller = Controller.getInstance();
		mNavItems = getResources().getStringArray(R.array.menu_array);
		//mDrawerTitle = getResources().getString(R.string.app_name);
		mTitle = controller.getFragmentsNames()[1];

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle(mTitle);

		mProgress = (ProgressBar) findViewById(R.id.load_progressbar);
		mProgress.setVisibility(ProgressBar.GONE);
		loadingText = (TextView) findViewById(R.id.load_progressbar_text);
		loadingText.setVisibility(TextView.GONE);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressbar_layout);
		progressLinearLayout.setVisibility(LinearLayout.GONE);
		contentFrame = (FrameLayout) findViewById(R.id.content_frame);

		/** Media Control **/
		//setVolumeControlStream(AudioManager.STREAM_MUSIC);
		/** Connection established **/
		if (controller.getServerConfig(this) != null
				&& controller.getServer().isConnected(controller.isOnline(getApplicationContext()))) {
			Log.d("bug", controller.getServer().getAmpacheConnection().getAuth());
			FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
			currentFrame = Fragment.instantiate(MainActivity.this, controller.getFragments()[1]);
			tx.replace(R.id.content_frame, currentFrame);
			tx.commit();
			showToast("流媒体服务器连接成功", Toast.LENGTH_LONG);
			controller.loadCachedFiles();
			/** Sync Files **/
			synchronize(false);

		} else if (controller.getServerConfig(this) != null
				&& !controller.getServer().isConnected(controller.isOnline(getApplicationContext()))) {
			FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
			currentFrame = Fragment.instantiate(MainActivity.this, controller.getFragments()[5]);
			tx.replace(R.id.content_frame, currentFrame);
			tx.commit();
			mTitle = controller.getFragmentsNames()[5];
			getActionBar().setTitle(mTitle);
			showToast("流媒体服务器连接失败", Toast.LENGTH_LONG);
		} else {
			FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
			currentFrame = Fragment.instantiate(MainActivity.this, controller.getFragments()[5]);
			tx.replace(R.id.content_frame, currentFrame);
			tx.commit();
			mTitle = controller.getFragmentsNames()[5];
			getActionBar().setTitle(mTitle);
			showToast("请配置流媒体服务器连接参数", Toast.LENGTH_LONG);
		}

		// just styling option add shadow the right edge of the drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mNavItems));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int pos, long id) {
				FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
				currentFrame =  Fragment.instantiate(MainActivity.this, controller.getFragments()[pos]);
				tx.replace(R.id.content_frame,currentFrame);
				tx.commit();
				mTitle = controller.getFragmentsNames()[pos];
				getActionBar().setTitle(controller.getFragmentsNames()[pos]);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
				//mDrawerLayout.closeDrawer(mDrawerList);
			}
		});
		 
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		//Fragment curframe = Fragment.instantiate(MainActivity.this, controller.getFragments()[5]));
		//Log.d("Fragment",controller.getFragments()[5]);
		Log.d( "key Event is ", String.valueOf(keyCode));
		
		if(currentFrame instanceof CurrentPlaylistView){
			CurrentPlaylistView fg = (CurrentPlaylistView)currentFrame;
			fg.onKeyDown(keyCode, event);
		  }
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		// Handle your other action bar items...
		if (item.toString().equals(getResources().getString(R.string.settingsEffect))) {
			FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
			currentFrame = Fragment.instantiate(MainActivity.this, controller.getFragments()[0]);
			tx.replace(R.id.content_frame,currentFrame );
			tx.commit();
			getActionBar().setTitle(R.string.settingsEffect);
		}
		
		return super.onOptionsItemSelected(item);
	}

	public void saveSettings(View view) {
		String server = ((EditText) findViewById(R.id.settingsServer)).getText().toString();
		String user = ((EditText) findViewById(R.id.settingsUser)).getText().toString();
		String password = ((EditText) findViewById(R.id.settingsPassword)).getText().toString();
		
		if( server.toLowerCase().indexOf("http://") < 0 )
			server = "http://" + server;
		
		if (controller.getServer() != null && !controller.getServer().getPassword().equals(password)) {
			password = controller.generateShaHash(password);
		}
		controller.saveSettings(password, user, server);
		if (!controller.saveServer(getApplicationContext())) {
			showToast("请配置流媒体服务器连接参数", Toast.LENGTH_LONG);
		} else {
			showToast("流媒体服务器配置成功", Toast.LENGTH_SHORT);
			if (!this.controller.getServer().isConnected(controller.isOnline(getApplicationContext()))) {
				showToast("流媒体服务器连接失败", Toast.LENGTH_SHORT);
			} else {
				showToast("流媒体服务器连接成功", Toast.LENGTH_SHORT);
				FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
				currentFrame = Fragment.instantiate(MainActivity.this, controller.getFragments()[1]);
				tx.replace(R.id.content_frame,currentFrame );
				tx.commit();
				getActionBar().setTitle(controller.getFragmentsNames()[1]);

				/** Sync Files **/
				synchronize(true);
			}
		}
	}

	/**
	 * Synchronizes the files if the corresponding boolean is true and starts an AsyncTask.
	 * 
	 * @param force boolean True if a synchronization is forced to synchronize everything
	 */
	public void synchronize(boolean force) {
		syncFilesCount = 0;
		ServerConnection ampache = controller.getServer().getAmpacheConnection();
		Log.d("bug songs", controller.getServer().getCachedData().getSongs().size() + ", "
				+ controller.getServer().getAmpacheConnection().getSongs());
		Log.d("bug albums", controller.getServer().getCachedData().getAlbums().size() + ", "
				+ controller.getServer().getAmpacheConnection().getAlbums());
		Log.d("bug artists", controller.getServer().getCachedData().getArtists().size() + ", "
				+ controller.getServer().getAmpacheConnection().getArtists());
		Log.d("bug playlists", controller.getServer().getCachedData().getPlaylists().size() + ", "
				+ controller.getServer().getAmpacheConnection().getPlaylists());
		if (controller.getServer().getCachedData().getAlbums().size() != controller.getServer().getAmpacheConnection()
				.getAlbums()
				|| force) {
			syncAlbums = true;
			syncFilesCount += ampache.getAlbums();
		}
		if (controller.getServer().getCachedData().getArtists().size() != controller.getServer().getAmpacheConnection()
				.getArtists()
				|| force) {
			syncArtists = true;
			syncFilesCount += ampache.getArtists();
		}
		if (controller.getServer().getCachedData().getPlaylists().size() != controller.getServer()
				.getAmpacheConnection().getPlaylists()
				|| force) {
			syncPlaylists = true;
			syncFilesCount += ampache.getPlaylists();
		}
		if (controller.getServer().getCachedData().getSongs().size() != controller.getServer().getAmpacheConnection()
				.getSongs()
				|| force) {
			syncSongs = true;
			syncFilesCount += ampache.getSongs();
		}

		if (syncAlbums || syncArtists || syncPlaylists || syncSongs || force) {
			new DownloadFilesTask().execute();
		}
	}

	private class DownloadFilesTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressLinearLayout.setVisibility(LinearLayout.VISIBLE);
			mProgress.setVisibility(ProgressBar.VISIBLE);
			loadingText.setVisibility(TextView.VISIBLE);
			float scale = getResources().getDisplayMetrics().density;
			int dpAsPixels = (int) (48 * scale + 0.5f);
			int dpAsPixelsDim = (int) (16 * scale + 0.5f);
			contentFrame.setPadding(dpAsPixelsDim, dpAsPixels, dpAsPixelsDim, dpAsPixelsDim);
		}

		/*
		 * (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			String urlString = "";
			if (syncSongs) {
				syncText = "正在同步歌曲";
				urlString = controller.getServer().getHost() + "/server/xml.server.php?action=songs&auth="
						+ controller.getServer().getAuthKey();
				controller.parseSongs(urlString);
				controller.getServer().getCachedData().setSongs(controller.getSongs());
				publishProgress();
			}

			if (syncAlbums) {
				syncText = "正在同步专辑";
				urlString = controller.getServer().getHost() + "/server/xml.server.php?action=albums&auth="
						+ controller.getServer().getAuthKey();
				controller.parseAlbums(urlString);
				controller.getServer().getCachedData().setAlbums(controller.getAlbums());
				publishProgress();
			}

			if (syncPlaylists) {
				syncText = "正在同步列表";
				urlString = controller.getServer().getHost() + "/server/xml.server.php?action=playlists&auth="
						+ controller.getServer().getAuthKey();
				controller.parsePlaylists(urlString);
				controller.getServer().getCachedData().setPlaylists(controller.getPlaylists());
				publishProgress();
			}

			if (syncArtists) {
				syncText = "正在同步歌手";
				urlString = controller.getServer().getHost() + "/server/xml.server.php?action=artists&auth="
						+ controller.getServer().getAuthKey();
				controller.parseArtists(urlString);
				controller.getServer().getCachedData().setArtists(controller.getArtists());
				publishProgress();
			}
			if (controller.saveServer(getApplicationContext())) {
				Log.d("bug", "同步已经成功");
				Log.d("bug", "添加音乐" + controller.getServer().getCachedData().getSongs().size() + " 文件");
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... params) {
			loadingText.setText(syncText);
			mProgressStatus = ((int) ((((double) (controller.getProgress()) / ((double) (syncFilesCount))) * 100)));
			Log.d("progress", String.valueOf(controller.getProgress()));
			Log.d("progress status", String.valueOf(mProgressStatus));
			mProgress.setProgress(mProgressStatus);
		}

		@Override
		protected void onPostExecute(Void result) {
			mProgress.setVisibility(ProgressBar.GONE);
			loadingText.setVisibility(TextView.GONE);
			progressLinearLayout.setVisibility(LinearLayout.GONE);
			Log.d("sync", "done");
			Log.d("bug songs", controller.getServer().getCachedData().getSongs().size() + ", "
					+ controller.getServer().getAmpacheConnection().getSongs());
			Log.d("bug albums", controller.getServer().getCachedData().getAlbums().size() + ", "
					+ controller.getServer().getAmpacheConnection().getAlbums());
			Log.d("bug artists", controller.getServer().getCachedData().getArtists().size() + ", "
					+ controller.getServer().getAmpacheConnection().getArtists());
			Log.d("bug playlists", controller.getServer().getCachedData().getPlaylists().size() + ", "
					+ controller.getServer().getAmpacheConnection().getPlaylists());
			controller.setProgress(0);
			float scale = getResources().getDisplayMetrics().density;
			int dpAsPixels = (int) (16 * scale + 0.5f);
			contentFrame.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
		}

	}

	/**
	 * Reconnects the server connection and gets new session or prolongs it.
	 * 
	 * @param view View
	 */
	public void reconnect(View view) {
		try {
			if (controller.isOnline(getApplicationContext())) {
				if (this.controller.getServer().isConnected(controller.isOnline(getApplicationContext()))) {
					showToast("流媒体服务器重连成功", Toast.LENGTH_SHORT);
					/** Sync Files **/
					synchronize(false);
				} else {
					showToast("流媒体服务器重连失败", Toast.LENGTH_SHORT);
				}
			} else {
				showToast("流媒体服务器已经在线", Toast.LENGTH_SHORT);
			}
		} catch (NullPointerException e) {
			Log.d("bug", "Server reconnect failed");
		}
	}

	public void showToast(String message, int duration) {
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}

	/**
	 * Forces a sync of all files.
	 * 
	 * @param view View
	 */
	public void forceSync(View view) {
		try {
			if (controller.isOnline(getApplicationContext())) {
				if (this.controller.getServer().isConnected(controller.isOnline(getApplicationContext()))) {
					showToast("流媒体服务器同步成功", Toast.LENGTH_SHORT);
					/** Sync Files **/
					synchronize(true);
				} else {
					showToast("流媒体服务器同步失败", Toast.LENGTH_SHORT);
				}
			} else {
				showToast("流媒体服务器同步错误", Toast.LENGTH_SHORT);
			}
		} catch (NullPointerException e) {
			Log.d("bug", "Server sync failed");
		}
	}

	/**
	 * @return the mProgress
	 */
	public ProgressBar getmProgress() {
		return mProgress;
	}

	/**
	 * @param mProgress the mProgress to set
	 */
	public void setmProgress(ProgressBar mProgress) {
		this.mProgress = mProgress;
	}

	/**
	 * @return the loadingText
	 */
	public TextView getLoadingText() {
		return loadingText;
	}

	/**
	 * @param loadingText the loadingText to set
	 */
	public void setLoadingText(TextView loadingText) {
		this.loadingText = loadingText;
	}

}
