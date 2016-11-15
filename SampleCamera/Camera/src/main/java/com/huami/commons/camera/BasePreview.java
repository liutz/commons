package com.huami.commons.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BasePreview extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final String TAG = "CameraPreview";

	private int viewWidth = 0;
	private int viewHeight = 0;

	/** 监听接口 */
	private OnCameraStatusListener mCameraStatusListener;

	private SurfaceHolder mHolder;
	private Camera mCamera;
	

	//创建一个PictureCallback对象，并实现其中的onPictureTaken方法
	private PictureCallback mPictureCallback = new PictureCallback() {

		// 该方法用于处理拍摄后的照片数据
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// 停止照片拍摄
			try {
				camera.stopPreview();
			} catch (Exception e) {
			}
			// 调用结束事件
			if (null != mCameraStatusListener) {
				mCameraStatusListener.onCameraStopped(mCameraType,data);
			}
		}
	};

	// Preview类的构造方法
	public BasePreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		// 获得SurfaceHolder对象
		mHolder = getHolder();
		// 指定用于捕捉拍照事件的SurfaceHolder.Callback对象
		mHolder.addCallback(this);
		// 设置SurfaceHolder对象的类型
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);		
	}
	
	public String getFlashMode() {
		return (mCamera.getParameters().getFlashMode());
	}

	public void setFlashMode(String mode) {
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();

	        params.setFlashMode(mode);
	        mCamera.setParameters(params);
		}
	}

	// 在surface创建时激发
	public void surfaceCreated(SurfaceHolder holder) {
		openCamera();
	}
	
	private void openCamera(){

		Log.e(TAG, "==surfaceCreated==");
		if(!Utils.checkCameraHardware(getContext())) {
			Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
			return;
		}
		// 获得Camera对象
		mCamera = getCameraInstance();
		try {
			// 设置用于显示拍照摄像的SurfaceHolder对象
			mCamera.setPreviewDisplay(mHolder);
		} catch (Exception e) {
			e.printStackTrace();
			// 释放手机摄像头
			if(mCamera != null){
				try{
					mCamera.release();					
				}catch (Exception e1){}finally{
					mCamera = null;
				}				
			}
			
		}
		updateCameraParameters();
		if (mCamera != null) {
			mCamera.startPreview();
		}
		if(mCameraStatusListener != null){
			mCameraStatusListener.onSurfaceChanged();
		}
	
	}
	
	public void destroyCamera(){
		// 释放手机摄像头				
		try{
			mCamera.release();					
		}catch (Exception e){}finally{
			mCamera = null;
		}
	}

	// 在surface销毁时激发
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "==surfaceDestroyed==");
		destroyCamera();	
	}
	
	public Camera getCamera(){
		return mCamera;
	}

	// 在surface的大小发生改变时激发
	public void surfaceChanged(final SurfaceHolder holder, int format, int w, int h) {
		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e){
			// ignore: tried to stop a non-existent preview
		}
		// set preview size and make any resize, rotate or
		// reformatting changes here
		updateCameraParameters();
		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.startPreview();

		} catch (Exception e){
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
		}
		//setFocus();
		if(mCameraStatusListener != null){
			mCameraStatusListener.onSurfaceChanged();
		}
	}	

	/**
	 * 获取摄像头实例
	 * @return
	 */
	private Camera getCameraInstance() {
		Camera c = null;
		try {
			int cameraCount = 0;
			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
			cameraCount = Camera.getNumberOfCameras(); // get cameras number

			for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
				Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
				// 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
				if (mCameraType == BACK_CAMERA && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
					try {
						c = Camera.open(camIdx);   //打开后置摄像头
					} catch (RuntimeException e) {
						Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
					}
				}
				
				if(mCameraType == FRONT_CAMERA && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
					try {
						c = Camera.open(camIdx);   //打开前置摄像头
					} catch (RuntimeException e) {
						Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
					}
				}
			}
			if (c == null) {
				c = Camera.open(0); // attempt to get a Camera instance
			}
		} catch (Exception e) {
			Toast.makeText(getContext(), "摄像头打开失败！", Toast.LENGTH_SHORT).show();
		}
		return c;
	}
	
	public static final int FRONT_CAMERA = 1;	//前置摄像头标记
    public static final int BACK_CAMERA = 2;	//后置摄像头标记
    private int mCameraType = BACK_CAMERA;	//当前打开的摄像头标记
    
	public void changeCamera(){
		destroyCamera();
        if(mCameraType == FRONT_CAMERA){
        	mCameraType = BACK_CAMERA;
        }else if(mCameraType == BACK_CAMERA){
        	mCameraType = FRONT_CAMERA;
        }
        openCamera();
    }

	private void updateCameraParameters() {
		if (mCamera != null) {
			Camera.Parameters p = mCamera.getParameters();

			setParameters(p);

			try {
				mCamera.setParameters(p);
			} catch (Exception e) {
				Camera.Size previewSize = findBestPreviewSize(p);
				p.setPreviewSize(previewSize.width, previewSize.height);
				p.setPictureSize(previewSize.width, previewSize.height);
				mCamera.setParameters(p);
			}
		}
	}
	
	private void setParameters(Camera.Parameters p) {
		List<String> focusModes = p.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
			p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		}

		long time = new Date().getTime();
		p.setGpsTimestamp(time);
		// 设置照片格式
		p.setPictureFormat(PixelFormat.JPEG);
		Camera.Size previewSize = findPreviewSizeByScreen(p);
		p.setPreviewSize(previewSize.width, previewSize.height);
		p.setPictureSize(previewSize.width, previewSize.height);
		p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		if (getContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			mCamera.setDisplayOrientation(90);
			p.setRotation(90);
		}
	}

	// 进行拍照，并将拍摄的照片传入PictureCallback接口的onPictureTaken方法
	public void takePicture() {
		if (mCamera != null) {
			try {
				mCamera.takePicture(null, null, mPictureCallback);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// 设置监听事件
	public void setOnCameraStatusListener(OnCameraStatusListener listener) {
		this.mCameraStatusListener = listener;
	}	

	public void start() {
		if (mCamera != null) {
			mCamera.startPreview();
		}
	}

	public void stop() {
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	/**
	 * 相机拍照监听接口
	 */
	public interface OnCameraStatusListener {
		// 相机拍照结束事件
		void onCameraStopped(int cameraType,byte[] data);
		// 基础相机表视层改变回调
		void onSurfaceChanged();		
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		viewWidth = MeasureSpec.getSize(widthSpec);
		viewHeight = MeasureSpec.getSize(heightSpec);
		super.onMeasure(
				MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
	}

	/**
	 * 将预览大小设置为屏幕大小
	 * @param parameters
	 * @return
	 */
	private Camera.Size findPreviewSizeByScreen(Camera.Parameters parameters) {
		if (viewWidth != 0 && viewHeight != 0) {
			return mCamera.new Size(Math.max(viewWidth, viewHeight),
					Math.min(viewWidth, viewHeight));
		} else {
			return mCamera.new Size(Utils.getScreenWH(getContext()).heightPixels,
					Utils.getScreenWH(getContext()).widthPixels);
		}
	}

	/**
	 * 找到最合适的显示分辨率 （防止预览图像变形）
	 * @param parameters
	 * @return
	 */
	private Camera.Size findBestPreviewSize(Camera.Parameters parameters) {

		// 系统支持的所有预览分辨率
		String previewSizeValueString = null;
		previewSizeValueString = parameters.get("preview-size-values");

		if (previewSizeValueString == null) {
			previewSizeValueString = parameters.get("preview-size-value");
		}

		if (previewSizeValueString == null) { // 有些手机例如m9获取不到支持的预览大小 就直接返回屏幕大小
			return mCamera.new Size(Utils.getScreenWH(getContext()).widthPixels,
					Utils.getScreenWH(getContext()).heightPixels);
		}
		float bestX = 0;
		float bestY = 0;

		float tmpRadio = 0;
		float viewRadio = 0;

		if (viewWidth != 0 && viewHeight != 0) {
			viewRadio = Math.min((float) viewWidth, (float) viewHeight)
					/ Math.max((float) viewWidth, (float) viewHeight);
		}

		String[] COMMA_PATTERN = previewSizeValueString.split(",");
		for (String prewsizeString : COMMA_PATTERN) {
			prewsizeString = prewsizeString.trim();

			int dimPosition = prewsizeString.indexOf('x');
			if (dimPosition == -1) {
				continue;
			}

			float newX = 0;
			float newY = 0;

			try {
				newX = Float.parseFloat(prewsizeString.substring(0, dimPosition));
				newY = Float.parseFloat(prewsizeString.substring(dimPosition + 1));
			} catch (NumberFormatException e) {
				continue;
			}

			float radio = Math.min(newX, newY) / Math.max(newX, newY);
			if (tmpRadio == 0) {
				tmpRadio = radio;
				bestX = newX;
				bestY = newY;
			} else if (tmpRadio != 0 && (Math.abs(radio - viewRadio)) < (Math.abs(tmpRadio - viewRadio))) {
				tmpRadio = radio;
				bestX = newX;
				bestY = newY;
			}
		}

		if (bestX > 0 && bestY > 0) {
			return mCamera.new Size((int) bestX, (int) bestY);
		}
		return null;
	}
}