package com.sap.sapdroid.views;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class UtilFunc {

	protected static final String TAG = "SAP:Play";
	
	public void delay()
	{
	    try {
	        Thread.sleep(1000);
	       } catch (InterruptedException e) {
	        e.printStackTrace();
	    }		
	}
	
	/**
	* 加载本地图片
	* http://bbs.3gstdy.com
	* @param url
	* @return
	*/
	public static Bitmap getLoacalBitmap(String url) {
	     try {
	          FileInputStream fis = new FileInputStream(url);
	          return BitmapFactory.decodeStream(fis);
	     } catch (FileNotFoundException e) {
	          e.printStackTrace();
	          return null;
	     }
	}
	/**
	* 从服务器取图片
	*http://bbs.3gstdy.com
	* @param url
	* @return
	*/
	public static Bitmap getHttpBitmap(String url) {
	     URL myFileUrl = null;
	     Bitmap bitmap = null;
	     try {
	          Log.d(TAG, url);
	          myFileUrl = new URL(url);
	     } catch (MalformedURLException e) {
	          e.printStackTrace();
	     }
	     try {
	          HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
	          conn.setConnectTimeout(0);
	          conn.setDoInput(true);
	          conn.connect();
	          InputStream is = conn.getInputStream();
	          bitmap = BitmapFactory.decodeStream(is);
	          is.close();
	     } catch (IOException e) {
	          e.printStackTrace();
	     }
	     return bitmap;
	
	}	
}
