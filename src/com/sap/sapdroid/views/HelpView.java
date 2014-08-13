/**
 * 
 */
package com.sap.sapdroid.views;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sap.sapdroid.R;

/**
 * @author Chen Xiaoliang
 * 
 */
public class HelpView extends Fragment {

	/**
	 * 
	 */
	public HelpView() {
		// TODO Auto-generated constructor stub
	}

	public static Fragment newInstance(Context context) {
		HelpView p = new HelpView();
		return p;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.help, null);
		return root;
	}
}
