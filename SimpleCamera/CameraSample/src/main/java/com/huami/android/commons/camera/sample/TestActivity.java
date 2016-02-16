package com.huami.android.commons.camera.sample;


import com.huami.commons.camera.SimpleCamera;
import com.huami.commons.camera.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

public class TestActivity extends Activity {

	private SimpleCamera mSimpleCamera;
	private RelativeLayout mHead;
	private RelativeLayout mbottom;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_camera_container);
        // 设置SimpleCamera为全屏模式
        mSimpleCamera = (SimpleCamera) findViewById(R.id.camera);
        mHead = (RelativeLayout) findViewById(R.id.head);
        mbottom = (RelativeLayout) findViewById(R.id.bottom);
        
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHead.getLayoutParams();
        lp.height = 400;
        mHead.setLayoutParams(lp);
        
        RelativeLayout.LayoutParams lp1 = (RelativeLayout.LayoutParams) mbottom.getLayoutParams();
        lp1.height = Utils.getHeightInPx(this)-400-Utils.getWidthInPx(this);
        lp1.topMargin=400+Utils.getWidthInPx(this);
        mbottom.setLayoutParams(lp1);
        
        // 设置照相机预览窗口距上边距大小
        mSimpleCamera.setPreviewTopMargin(400);
	}
	
	public void onTakePhoto(View view){
		// 调用拍照接口
		mSimpleCamera.takePicture();
	}
	
	public void onFlashLamp(View view){
		// 开关闪关灯
		switchFlash(!mLampState);
	}
	
	public void onSwitchCamera(View view){
		// 切换前后景
		mSimpleCamera.switchCamera();
	}
	
	private boolean mLampState;

    private void switchFlash(boolean state) {

        mLampState = state;
        if (state) {
        	mSimpleCamera.setFlashMode("torch");
        } else {
        	mSimpleCamera.setFlashMode("off");
        }
    }
    
    // 获取拍照照片
    public void screenShot(){
    	View view = mSimpleCamera.getCameraImageView();
    	// ToDo 通过view截图    	
    }

	@Override
	public void onBackPressed() {
		if(!mSimpleCamera.isPreViewState()){
			// 更新照相机为拍照状态
			mSimpleCamera.updateCameraState(SimpleCamera.INIT_STATE);
		}else{
			super.onBackPressed();
		}
	}	
}
