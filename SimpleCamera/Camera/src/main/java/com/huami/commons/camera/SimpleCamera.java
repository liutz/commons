package com.huami.commons.camera;

import java.util.ArrayList;
import java.util.List;
import com.huami.android.commons.toolbox.ResUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.AutoFocusCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class SimpleCamera extends FrameLayout implements BasePreview.OnCameraStatusListener,SensorEventListener, AutoFocusCallback{

	public static final int INIT_STATE = 1;
	public static final int TAKE_STATE = 2;
	
	private BasePreview mBasePreview;
    private FocusView mFocusView;
    private ImageView mImageView; 
    private RelativeLayout mCameraPreview;
    
    private int mCurrentState;
    
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    private boolean mInitialized = false;
    private Context mContext;
    
    public void takePicture(){
    	if(mBasePreview != null){
    		mBasePreview.takePicture();
    	}
    }
	
	public SimpleCamera(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}	
	
	public boolean isPreViewState() {
		return mCurrentState == INIT_STATE ? true:false;
	}
	
	public SimpleCamera(Context context) {
		super(context);
		init(context);
	}
	
	public void switchCamera(){
		if(mBasePreview != null){
			mBasePreview.changeCamera();
		}
	}
	
	private void init(Context context) {
		mContext = context;
        final LayoutInflater inflater = LayoutInflater.from(context);
        
        final View v = inflater.inflate(ResUtils.getLayoutIdByName(context, "view_camera_container"), this, true);
        mBasePreview = (BasePreview) v.findViewById(ResUtils.getResIdByName(context, "base_preview"));
        mFocusView = (FocusView) v.findViewById(ResUtils.getResIdByName(context, "camera_focus"));
        mImageView = (ImageView) v.findViewById(ResUtils.getResIdByName(context, "camera_image"));
        mCameraPreview = (RelativeLayout) v.findViewById(ResUtils.getResIdByName(context, "camera_preview"));
        LayoutParams lp = (LayoutParams) mCameraPreview.getLayoutParams();
		lp.height = Utils.getWidthInPx(mContext);
		mCameraPreview.setLayoutParams(lp);
		RelativeLayout.LayoutParams lp1 = (android.widget.RelativeLayout.LayoutParams) mImageView.getLayoutParams();
		lp1.height = Utils.getWidthInPx(mContext);
		mImageView.setLayoutParams(lp1);
		
		mFocusView.Y = getInitFocusY();
        mBasePreview.setOnCameraStatusListener(this);
        
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        updateCameraState(INIT_STATE);
        mCameraPreview.setOnTouchListener(mOnTouchListener);
	}
	
	public int getInitFocusY(){
		return mFocusTopMargin + ((Utils.getHeightInPx(getContext()) - Utils.getWidthInPx(getContext())) / 2);
	}
	
	public void setFlashMode(String mode) {
		mBasePreview.setFlashMode(mode);
	}	

	@Override
	public void onCameraStopped(int cameraType,byte[] data) {
		if(data==null){return;}	
		
		Bitmap bitmap = Bitmap.createBitmap(BitmapFactory.decodeByteArray(data , 0, data .length), 0, mCameraPreview.getTop(),  Utils.getWidthInPx(getContext()),  Utils.getWidthInPx(getContext()));
		if(cameraType == BasePreview.FRONT_CAMERA){
			bitmap = convertBmp(bitmap);
		}
		mImageView.setImageBitmap(bitmap);
		
		updateCameraState(TAKE_STATE);
	}
	
	public View getCameraImageView(){
		return mImageView;
	}
	
	 private Bitmap convertBmp(Bitmap bmp) {
         int w = bmp.getWidth();
         int h = bmp.getHeight();

         Matrix matrix = new Matrix();
         matrix.postRotate(180);
         matrix.postScale(-1, 1);
         Bitmap convertBmp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

         return convertBmp;
     }
	
	private int mFocusTopMargin;
	
	public void setPreviewTopMargin(int topMargin){
		if(mCameraPreview != null){
			mFocusTopMargin = topMargin;
			postInvalidate();
		}		
	}	
	
	public void updateCameraState(int state){
		if(mCurrentState == state){
			return;
		}
		
		if(state == INIT_STATE){
			mBasePreview.setVisibility(View.VISIBLE);
			mFocusView.setVisibility(View.VISIBLE);
			mImageView.setVisibility(View.GONE);
			mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
		}
		
		if(state == TAKE_STATE){
			mImageView.setVisibility(View.VISIBLE);	
			mBasePreview.setVisibility(View.GONE);
			mFocusView.setVisibility(View.GONE);
			mSensorManager.unregisterListener(this);
		}
		
		mCurrentState = state;		
	}	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        
        if (!mInitialized){
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        
        float deltaX = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);

        if(deltaX > 0.8 || deltaY > 0.8 || deltaZ > 0.8){
            setFocus();
        }
        
        mLastX = x;
        mLastY = y;
        mLastZ = z;		
	}

	@Override
	public void onSurfaceChanged() {}
	
	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 遍历所有子视图
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int id = childView.getId();
            // 获取在onMeasure中计算的视图尺寸
            int measuredWidth = childView.getMeasuredWidth();
            
            if(id == ResUtils.getResIdByName(getContext(), "base_preview")){
            	childView.layout(l, t, r, b);
            }else if(id == ResUtils.getResIdByName(getContext(), "camera_preview")){
            	childView.layout(l, t+mFocusTopMargin, measuredWidth, t+mFocusTopMargin+measuredWidth);
            }
        }
    }

	/**
	 * 设置自动聚焦，并且聚焦的圈圈显示在屏幕中间位置
	 */
	public void setFocus() {
		if(!mFocusView.isFocusing()) {
			try {				
				int x = mFocusView.X;
				if(x == 0){
					mFocusView.setX((Utils.getWidthInPx(getContext()) - mFocusView.getWidth()) / 2);
				}else{
					mFocusView.setX(x);
				}
				
				int y = mFocusView.Y;
				if(y == 0){
					mFocusView.setY((Utils.getHeightInPx(getContext()) - mFocusView.getHeight()) / 2);					
				}else{
					mFocusView.setY(y);	
				}
				
				mFocusView.beginFocus();
				mBasePreview.getCamera().autoFocus(this);
			} catch (Exception e) {}
		}
	}
	
	/**
	 * 设置焦点和测光区域
	 *
	 * @param event
	 */
	public void focusOnTouch(MotionEvent event) {

		int[] location = new int[2];		
		mCameraPreview.getLocationOnScreen(location);

		Rect focusRect = Utils.calculateTapArea(mFocusView.getWidth(),
				mFocusView.getHeight(), 1f, event.getRawX(), event.getRawY(),
				location[0], location[0] + mCameraPreview.getWidth(), location[1],
				location[1] + mCameraPreview.getHeight());
		Rect meteringRect = Utils.calculateTapArea(mFocusView.getWidth(),
				mFocusView.getHeight(), 1.5f, event.getRawX(), event.getRawY(),
				location[0], location[0] + mCameraPreview.getWidth(), location[1],
				location[1] + mCameraPreview.getHeight());

		Camera.Parameters parameters = mBasePreview.getCamera().getParameters();
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

		if (parameters.getMaxNumFocusAreas() > 0) {
			List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
			focusAreas.add(new Camera.Area(focusRect, 1000));

			parameters.setFocusAreas(focusAreas);
		}

		if (parameters.getMaxNumMeteringAreas() > 0) {
			List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
			meteringAreas.add(new Camera.Area(meteringRect, 1000));

			parameters.setMeteringAreas(meteringAreas);
		}

		try {
			mBasePreview.getCamera().setParameters(parameters);
		} catch (Exception e) {
		}
		mBasePreview.getCamera().autoFocus(this);
	}
	
	/**
	 * 点击显示焦点区域
	 */
	OnTouchListener mOnTouchListener = new OnTouchListener() {
		@SuppressWarnings("deprecation")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				int width = mFocusView.getWidth();
				int height = mFocusView.getHeight();
				mFocusView.setX(event.getX() - (width / 2));
				mFocusView.setY(event.getY() - (height / 2));
				mFocusView.beginFocus();
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				focusOnTouch(event);
			}
			return true;
		}
	};

	@Override
	public void onAutoFocus(boolean success, Camera camera) {}
}
