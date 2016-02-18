package com.huami.android.commons.toolbox;

import android.content.Context;
import android.content.res.Resources;

public class ResUtils {

	public static int getResIdByName(Context context, String reourcesType, String resourcesName) {
		Resources resources = context.getResources();
		int id = resources.getIdentifier(resourcesName, reourcesType, context.getPackageName());
		if (id == 0) {			
			throw new RuntimeException("Resource file is not found!");
		}
		return id;
	}
	
	public static int getLayoutIdByName(Context context,String resourcesName){
		return getResIdByName(context, "layout", resourcesName);
	}
	
	public static int getAnimIdByName(Context context,String resourcesName){
		return getResIdByName(context, "anim", resourcesName);
	}
	
	public static int getStyleIdByName(Context context,String resourcesName){
		return getResIdByName(context, "style", resourcesName);
	}
	
	public static int getColorIdByName(Context context, String resourcesName) {
		Resources resources = context.getResources();
		int id = resources.getIdentifier(resourcesName, "color", context.getPackageName());
		if (id == 0) {
			throw new RuntimeException("Resource file is not found!");
		}
		return id;
	}
	
	public static final int getResIdByName(Context context, String resourcesName) {
		Resources resources = context.getResources();
		int id = resources.getIdentifier(resourcesName, "id", context.getPackageName());
		if (id == 0) {
			throw new RuntimeException("Resource file is not found!");
		}
		return id;
	}
	
	public static int getDrawbleIdByName(Context context, String resourcesName) {
		Resources resources = context.getResources();
		int id = resources.getIdentifier(resourcesName, "drawable", context.getPackageName());
		if (id == 0) {
			throw new RuntimeException("Resource file is not found!");
		}
		return id;
	}
	
	public static int getStringIdByName(Context context, String resourcesName) {
		Resources resources = context.getResources();
		int id = resources.getIdentifier(resourcesName, "string", context.getPackageName());
		if (id == 0) {
			throw new RuntimeException("Resource file is not found!");
		}
		return id;
	}
}
