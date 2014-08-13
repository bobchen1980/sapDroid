package com.sap.player;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.*;
import android.os.Process;
import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

import com.sap.sapdroid.views.UtilFunc;
import com.sap.spatialex.spatialExFeed;
import com.sap.spatialex.spatialExJni;

public class StreamPlayer implements Runnable {

	    private static final String TAG = "StreamPlayer";
	    public static final int PLAYING_FINISHED = 46314;
	    public static final int PLAYING_FAILED = 46315;
	    public static final int PLAYING_STARTED = 46316;
	    public static final int  SAP_NONE =  0;
	    public static final int  SAP_ROOM = 10;
	    public static final int  SAP_STADIUM = 20;
	    public static final int  SAP_CINEMA  = 30;
	    private int exfect_state = SAP_NONE;
	    private Handler handler;
	    private AudioTrack audioTrack;
	    private UtilFunc utils;
	    
	    private static enum PlayerState {
	        PLAYING, STOPPED, BUFFERING
	     }	    
    
	    private spatialExFeed decodeFeed;
	    private String urlfileToPlay;
	    private int currentPosition = 0;
	    
	    private AtomicReference<PlayerState> currentState = new AtomicReference<PlayerState>(PlayerState.STOPPED);	
		
	    private class URLFileDecodeFeed implements spatialExFeed {
	        
	        @Override
	        public synchronized void decodeCallBack(short[] pcmData, int amountToRead) {
	            //If we received data and are playing, write to the audio track	  
	        	//Here need paly buffer, but demo is simple 
	        	if (audioTrack != null) {
	        		audioTrack.write(pcmData, 0, amountToRead); 
	        	}
	        }

	        @Override
	        public void stopCallBack() {
	        	Log.d(TAG, "stop callback");
	            //Set our state to stopped
	            currentState.set(PlayerState.STOPPED);
	            
	            handler.sendEmptyMessage(PLAYING_FINISHED);
	        }

	        @Override
	        public void startCallBack() {
       	
	            //We're starting to read actual content
	            currentState.set(PlayerState.PLAYING);
	            
	            handler.sendEmptyMessage(PLAYING_STARTED);
	        }

	    }
	    
	    public StreamPlayer() {
	    	this.urlfileToPlay = null;
	    	this.decodeFeed = null;
	    	this.handler = null;
	    	this.audioTrack = null;
	    
	    }	   
	    	    

	    /**
	     * Starts the audio recorder with a given sample rate and channels
	     */
	    @SuppressWarnings("all")
	    public synchronized void prepare(Handler handler) {
	    	
	        if (handler == null) {
	            throw new IllegalArgumentException("Handler must not be null.");
	        }
	        
            int minSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STREAM);
            
	        this.decodeFeed = new URLFileDecodeFeed();
	        this.handler = handler;
	        
	        utils = new UtilFunc();
   	
	    }
	    
	    public synchronized void reset() {
	    	int i = 0;
	    	this.stop();
	    	while( isPlaying() && i < 60)
	    	{
	    		this.utils.delay();
	    		i++;
	    	}
	    	
	    	this.urlfileToPlay = null;
	    	this.decodeFeed = null;
	    	this.handler = null;	   
	    	
	    	if (audioTrack != null) {
	    		audioTrack.release();
	    		audioTrack = null;
	    	}
	    }
	    
	    public synchronized void setSource(String fileToPlay) {
	        
	    	if (fileToPlay == null) {
	            throw new IllegalArgumentException("File to play must not be null.");
	        }
	    	this.urlfileToPlay = fileToPlay;
	    }	    
	    
	    public synchronized void start() {
	        if (isStopped()) {
	        	
	        	if (audioTrack != null) {
	        		audioTrack.play();
	        	}
	        	new Thread(this).start();
	        }
	    }
	    
	    /**
	     * Stops the player and notifies the decode feed
	     */
	    public synchronized void stop() {
	    	
	    	if(isPlaying() || isBuffering())
	    	{
	    		spatialExJni.StopDecoding();
            	//Stop the audio track
                if (audioTrack != null) {
                    audioTrack.stop();
                }		        
	    	}
	        
	    }

	    @Override
	    public void run() {
	        //Start the native decoder
	        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
	       
	        if(urlfileToPlay != null)
	        {
		        spatialExJni.SetFileSource(urlfileToPlay);	
		        spatialExJni.StartDecoding(decodeFeed);
	        }
	        //int result = spatialExJni.StopDecoding();
	        //Log.d(TAG, spatialExJni.GetVersion());
	    }

	    public synchronized int setCurrentEx(int param)
	    {
	    	exfect_state = spatialExJni.SetSpatialEx(param);
	    	return exfect_state;
	    }
	    
	    public synchronized int getCurrentEx()
	    {
	    	return exfect_state;
	    }	    
	    
	    public synchronized int getCurrentPosition() {
	    	
	    	currentPosition = spatialExJni.getCurPosition();
	    	
	        return currentPosition;
	    }
	    
	    /**
	     * Checks whether the player is currently playing
	     *
	     * @return <code>true</code> if playing, <code>false</code> otherwise
	     */
	    private synchronized boolean isPlaying() {
	        return currentState.get() == PlayerState.PLAYING;
	    }

	    /**
	     * Checks whether the player is currently stopped (not playing)
	     *
	     * @return <code>true</code> if playing, <code>false</code> otherwise
	     */
	    private synchronized boolean isStopped() {
	        return currentState.get() == PlayerState.STOPPED;
	    }


	    /**
	     * Checks whether the player is currently buffering
	     *
	     * @return <code>true</code> if buffering, <code>false</code> otherwise
	     */
	    private synchronized boolean isBuffering() {
	        return currentState.get() == PlayerState.BUFFERING;
	    }	    
	    
}
