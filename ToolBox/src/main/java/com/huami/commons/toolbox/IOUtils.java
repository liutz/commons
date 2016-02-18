package com.huami.android.commons.toolbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.content.Context;
import android.content.res.AssetManager;

public class IOUtils {

	public static String readAsset(Context context, String path) {
		AssetManager assetManager = context.getResources().getAssets();
	    String contents = "";
	    InputStream is = null;
	    BufferedReader reader = null;
	    try {
	        is = assetManager.open(path);
	        reader = new BufferedReader(new InputStreamReader(is));
	        contents = reader.readLine();
	        String line = null;
	        while ((line = reader.readLine()) != null) {
	            contents += '\n' + line;
	        }
	    } catch (final Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (is != null) {
	            try {
	                is.close();
	            } catch (IOException ignored) {
	            }
	        }
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException ignored) {
	            }
	        }
	    }
	    return contents;
	}

}
