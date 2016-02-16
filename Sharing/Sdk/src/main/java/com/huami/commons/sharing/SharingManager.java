package com.huami.commons.sharing;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler.Response;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SharingManager implements IWXAPIEventHandler, Response {

    private final static String TAG = "Sharing";
    public static boolean DEBUG = Log.isLoggable(TAG, Log.VERBOSE);

    // 定义分享提供者
    public static final int PROVIDER_WECHAT = 1;
    public static final int PROVIDER_WECHAT_FRIEND = 2;
    public static final int PROVIDER_QQ = 3;
    public static final int PROVIDER_QZONE = 4;
    public static final int PROVIDER_WEIBO = 5;
    public static final int PROVIDER_FACEBOOK = 6;

    // 定义最大图片100k
    private static final float MAX_BITMAP_SIZE = 100 * 1024f;
    private static final int THUMB_SIZE = 300;

    /*---应用名字-----------------包名------------------------------------activity名-------------------------------
     * 	微信好友		:		com.tencent.mm			:			com.tencent.mm.ui.tools.ShareImgUI
       	微信朋友圈	:		com.tencent.mm			:			com.tencent.mm.ui.tools.ShareToTimeLineUI
       	新浪微博		:		com.sina.weibo			:			com.sina.weibo.EditActivity
       	QQ			:		com.tencent.mobileqq	:			com.tencent.mobileqq.activity.JumpActivity
       	QQ空间		：		com.qzone				：			com.qzone.ui.operation.QZonePublishMoodActivity
     */
    private static final String PACKAGE_WEIXIN = "com.tencent.mm";
    private static final String PACKAGE_WEIBO = "com.sina.weibo";
    private static final String PACKAGE_QQ_ZONE = "com.qzone";
    private static final String PACKAGE_QQ = "com.tencent.mobileqq";
    private static final String PACKAGE_FACEBOOK = "com.facebook.katana";
    private static final String PACKAGE_TWITTER = "com.twitter.android";
    private static final String ACTIVITY_QQ = "com.tencent.mobileqq.activity.JumpActivity";
    private static final String ACTIVITY_WEIXIN = "com.tencent.mm.ui.tools.ShareImgUI";
    private static final String ACTIVITY_PENGYOUQUAN = "com.tencent.mm.ui.tools.ShareToTimeLineUI";

    private final Activity mContext;
    private final IUiListener mShareToQQListener = new IUiListener() {

        @Override
        public void onComplete(Object o) {
            if(DEBUG){
                Log.i(TAG, "share_qq_selector");
            }
        }

        @Override
        public void onError(UiError uiError) {
            if(DEBUG){
                Log.e(TAG, uiError.errorMessage + "|" + uiError.errorCode);
            }

        }

        @Override
        public void onCancel() {}
    };

    private final IUiListener mShareToQQzone = new IUiListener() {

        @Override
        public void onComplete(Object o) {
            if(DEBUG) {
                Log.i(TAG,"onComplete = " + o);
            }
        }

        @Override
        public void onError(UiError uiError) {
            if(DEBUG) {
                Log.i(TAG, "onError = " + uiError.errorMessage);
            }
        }

        @Override
        public void onCancel() {
            if(DEBUG){
                Log.i(TAG, "onCancel = ");
            }
        }
    };

    private IWeiboShareAPI mWeiboShareAPI = null;
    private IWXAPI mWeixinApi = null;
    private Tencent mTencent = null;
    private ShareListener mSharingListener;
    private CallbackManager mFacebookCallback;

    public SharingManager(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("activity is null");
        }
        mContext = activity;

        Intent intent = activity.getIntent();
        mWeixinApi = WXAPIFactory.createWXAPI(activity, ShareConstant.WX_APP_ID);
        mWeixinApi.registerApp(ShareConstant.WX_APP_ID);
        mWeixinApi.handleIntent(intent, this);

        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(activity, ShareConstant.WEIBO_APPKEY);
        mWeiboShareAPI.registerApp();
        mWeiboShareAPI.handleWeiboResponse(intent, this);

        mTencent = Tencent.createInstance(ShareConstant.QQ_APP_ID, activity);
        FacebookSdk.sdkInitialize(mContext.getApplicationContext());
        mFacebookCallback = CallbackManager.Factory.create();

    }

    public void setSharingListener(SharingListener sharingListener) {
        this.mSharingListener = sharingListener;
    }

    public List<ShareAppItem> getAllShareItems() {
        List<ShareAppItem> mItems = new ArrayList<ShareAppItem>();
        SparseArray<ShareAppItem> indexItems = new SparseArray<ShareAppItem>();

        ShareAppItem item = new ShareAppItem(R.drawable.share_weixin_selector_hm, R.string.share_weixin_label);
        mItems.add(item);
        indexItems.put(R.drawable.share_weixin_selector_hm, item);

        item = new ShareAppItem(R.drawable.share_pengyouquan_selector_hm, R.string.share_pengyouquan_label);
        mItems.add(item);
        indexItems.put(R.drawable.share_pengyouquan_selector_hm, item);

        item = new ShareAppItem(R.drawable.share_weibo_selector_hm, R.string.share_weibo_label);
        mItems.add(item);
        indexItems.put(R.drawable.share_weibo_selector_hm, item);

        item = new ShareAppItem(R.drawable.share_qq_selector_hm, R.string.share_qq_label);
        mItems.add(item);
        indexItems.put(R.drawable.share_qq_selector_hm, item);

        item = new ShareAppItem(R.drawable.share_qq_zone_selector_hm, R.string.share_qq_zone_label);
        mItems.add(item);
        indexItems.put(R.drawable.share_qq_zone_selector_hm, item);

        item = new ShareAppItem(R.drawable.share_facebook_selector_hm, R.string.share_facebook_label);
        mItems.add(item);
        indexItems.put(R.drawable.share_facebook_selector_hm, item);

        checkShareTargetAvailable(indexItems);

        // check QQZone：如果QQ安装，QQZone未安装，而且targetUrl不为空，则设置QQZone可点击状态。
        ShareAppItem qqApp = indexItems.get(R.drawable.share_qq_selector_hm);
        ShareAppItem qqZoneApp = indexItems.get(R.drawable.share_qq_zone_selector_hm);
        if (!qqZoneApp.enable && qqApp.enable && ShareConstant.QQ_ZONE_TARGET_URL != null) {
            qqZoneApp.enable = true;
        }
        return mItems;
    }

    private List<ResolveInfo> getShareTargets() {
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("image/*");
        PackageManager pm = mContext.getPackageManager();
        return pm.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    }

    private void checkShareTargetAvailable(SparseArray<ShareAppItem> indexItems) {
        List<ResolveInfo> shareList = getShareTargets();

        ShareAppItem item = null;
        for (ResolveInfo resolveInfo : shareList) {
            ApplicationInfo appInfo = resolveInfo.activityInfo.applicationInfo;
            String packageName = appInfo.packageName;
            Log.i(TAG, "packageName: " + packageName);
            String activityName = resolveInfo.activityInfo.name;

            if (PACKAGE_WEIBO.equalsIgnoreCase(packageName)) {

                item = indexItems.get(R.drawable.share_weibo_selector_hm);

            } else if (PACKAGE_WEIXIN.equalsIgnoreCase(packageName)) {

                if (ACTIVITY_PENGYOUQUAN.equalsIgnoreCase(activityName)) {
                    item = indexItems.get(R.drawable.share_pengyouquan_selector_hm);
                } else if (ACTIVITY_WEIXIN.equalsIgnoreCase(activityName)) {
                    item = indexItems.get(R.drawable.share_weixin_selector_hm);
                }

            } else if (PACKAGE_QQ_ZONE.equalsIgnoreCase(packageName)) {

                item = indexItems.get(R.drawable.share_qq_zone_selector_hm);

            } else if (PACKAGE_QQ.equalsIgnoreCase(packageName)) {

                if (ACTIVITY_QQ.equalsIgnoreCase(activityName)) {
                    item = indexItems.get(R.drawable.share_qq_selector_hm);
                }

            } else if(PACKAGE_FACEBOOK.equalsIgnoreCase(packageName)){
                item = indexItems.get(R.drawable.share_facebook_selector_hm);
            }else {
                continue;
            }

            if (item != null) {
                item.resolveInfo = resolveInfo;
                item.enable = true;
            }
        }
    }

    @Override
    public void onReq(BaseReq arg0) {}

    @Override
    public void onResp(BaseResp arg0) {}

    @Override
    public void onResponse(BaseResponse arg0) {}

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data){
        mFacebookCallback.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 用户选择分享以后，分享到不同的平台
     *
     * @param item         分享到的目标平台
     * @param shareContent 分享的内容参数
     */
    public void shareTo(ShareAppItem item, ShareContentItem shareContent) {
        if (item == null)
            return;

        if (item.icon == R.drawable.share_weixin_selector_hm) {
            if (shareToWeixin(shareContent, false)) {

                if (mShareListener != null) {
                    mShareListener.onComplete(ShareConstant.SHARE_TYPE_WECHAT_FRIEND);
                }
            }
        } else if (item.icon == R.drawable.share_pengyouquan_selector_hm) {
            if (shareToWeixin(shareContent, true)) {
                if (mShareListener != null) {
                    mShareListener.onComplete(ShareConstant.SHARE_TYPE_WECHAT_FRIENDS);
                }
            }
        } else if (item.icon == R.drawable.share_weibo_selector_hm) {
            if (shareToWeibo(shareContent)) {
                Log.fi(TAG, "share_weibo_selector_hm");
                if (mShareListener != null) {
                    mShareListener.onComplete(ShareConstant.SHARE_TYPE_WEIBO);
                }
            }
        } else if (item.icon == R.drawable.share_qq_selector_hm) {
            if (shareToQQ(shareContent)) {
                Log.fi(TAG, "share_qq_selector_hm");
                if (mShareListener != null) {
                    mShareListener.onComplete(ShareConstant.SHARE_TYPE_QQ);
                }
            }
        } else if (item.icon == R.drawable.share_qq_zone_selector_hm) {
            String targetUrl = ShareConstant.QQ_ZONE_TARGET_URL;
            boolean isQQInstalled = checkAppInstalled(PACKAGE_QQ);
            if (isQQInstalled && targetUrl != null && !"".equals(targetUrl)) {
                shareToQQZone(shareContent, targetUrl);
            } else {
                shareByIntent(item, shareContent);
            }

            if (mShareListener != null) {
                mShareListener.onComplete(ShareConstant.SHARE_TYPE_QZONE);
            }
        } else if(item.icon == R.drawable.share_facebook_selector_hm){
            shareToFacebook(shareContent);
        }
    }

    private boolean shareToWeixin(ShareContentItem shareContent, boolean isTimeLine) {
        if (!mWeixinApi.isWXAppInstalled())
            return false;

        WXMediaMessage wxMediaMessage;
        if (shareContent.showUrl()) {
            WXWebpageObject webpage = new WXWebpageObject();
            webpage.webpageUrl = shareContent.url;
            wxMediaMessage = new WXMediaMessage(webpage);
        } else {
            wxMediaMessage = new WXMediaMessage();
        }

        if (shareContent.mShareType == ShareContentItem.ShareContentType.IMAGE) {
            WXImageObject wxImageObject = new WXImageObject();
            wxImageObject.setImagePath(shareContent.bitmapUrl);
            wxMediaMessage.mediaObject = wxImageObject;

            Bitmap thumbBitmap = getThumbBitmap(new File(shareContent.bitmapUrl));
            wxMediaMessage.setThumbImage(thumbBitmap);
            wxMediaMessage.mediaObject = wxImageObject;
        } else if (shareContent.mShareType == ShareContentItem.ShareContentType.TEXT_IMAGE_URL) {
            Bitmap thumbBitmap = getThumbBitmap(new File(shareContent.bitmapUrl));
            wxMediaMessage.setThumbImage(thumbBitmap);
        }

        wxMediaMessage.title = shareContent.title;
        wxMediaMessage.description = shareContent.content;

        SendMessageToWX.Req sendReq = new SendMessageToWX.Req();
        sendReq.transaction = String.valueOf(System.currentTimeMillis());
        sendReq.message = wxMediaMessage;
        sendReq.scene = isTimeLine ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        boolean success = false;
        try {
            success = mWeixinApi.sendReq(sendReq);
        } catch (Exception e) {
            Log.i(TAG, e.toString());
        }
        return success;
    }

    private Bitmap getThumbBitmap(File path) {
        if (path == null)
            return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path.getPath(), options);
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null;
        }

        int bitmapSize = options.outHeight * options.outWidth * 4;// /Config.8888
        if (bitmapSize > MAX_BITMAP_SIZE) {
            options.inSampleSize = Math.round(bitmapSize / MAX_BITMAP_SIZE);
        }

        boolean sizeIsTooBig = options.inSampleSize > 4;
        if (sizeIsTooBig) {
            options.inSampleSize = 4;
        }

        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path.getPath(), options);
        int thumbWidth = THUMB_SIZE;
        int thumbHeight = THUMB_SIZE;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            thumbHeight = (int) ((float) bitmap.getHeight() / bitmap.getWidth() * THUMB_SIZE);
        } else {
            thumbWidth = (int) ((float) bitmap.getWidth() / bitmap.getHeight() * THUMB_SIZE);
        }
        if (sizeIsTooBig) {
            return ThumbnailUtils.extractThumbnail(bitmap, thumbWidth, thumbHeight);
        } else {
            return bitmap;
        }
    }

    private boolean shareToWeibo(ShareContentItem shareContent) {
        try {
            if (!mWeiboShareAPI.isWeiboAppSupportAPI()) {
                if(DEBUG){
                    Log.i(TAG, "shareToWeibo isWeiboAppSupportAPI false");
                }
                return false;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(shareContent.bitmapUrl);
            String content;
            if (("#" + mContext.getString(R.string.app_name) + "#").equals(shareContent.content)) {
                content = "#" + mContext.getString(R.string.app_name) + "# ";
            } else {
                content = "#" + mContext.getString(R.string.app_name) + "# " + shareContent.content;
            }

            if (shareContent.showUrl())
                content = content + " " + shareContent.url;

            int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
            if (supportApi >= 10351 /*ApiUtils.BUILD_INT_VER_2_2*/) {
                return sendWeiboMultiMessage(content, bitmap);
            } else {
                return sendWeiboSingleMessage(content, bitmap);
            }
        } catch (Exception e) {
            if(DEBUG) {
                Log.i(TAG, "shareToWeibo Exception:" + e.getMessage());
            }
        }
        return false;
    }

    private boolean sendWeiboMultiMessage(String text, Bitmap bitmap) {
        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(bitmap);

        TextObject textObject = new TextObject();
        textObject.text = text;

        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = textObject;
        weiboMessage.imageObject = imageObject;

        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        return mWeiboShareAPI.sendRequest(mContext, request);
    }

    private boolean sendWeiboSingleMessage(String text, Bitmap bitmap) {
        WeiboMessage weiboMessage = new WeiboMessage();

        ImageObject imageObject = new ImageObject();
        imageObject.setImageObject(bitmap);

        TextObject textObject = new TextObject();
        textObject.text = text;

        weiboMessage.mediaObject = textObject;
        weiboMessage.mediaObject = imageObject;

        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        return mWeiboShareAPI.sendRequest(mContext, request);
    }

    private boolean shareToQQ(ShareContentItem shareContent) {
        String content = shareContent.content;

        final Bundle params = new Bundle();
        if (shareContent.showUrl()) {
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            //// TODO: 2015/11/2
            if (TextUtils.isEmpty(content))
                content = ("#" + mContext.getString(R.string.app_name) + "#") + shareContent.url;
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareContent.url);
        } else {
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        }

        params.putString(QQShare.SHARE_TO_QQ_TITLE, shareContent.title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, content);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mContext.getString(R.string.app_name));
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, shareContent.bitmapUrl);

        mTencent.shareToQQ(mContext, params, mShareToQQListener);
        return true;
    }

    private void shareToFacebook(ShareContentItem shareContent) {
        ShareDialog shareDialog = new ShareDialog(mContext);
        // this part is optional
        shareDialog.registerCallback(mFacebookCallback, new FacebookCallback<Sharer.Result>() {
            public void onSuccess(Sharer.Result result) {
                if (mShareListener != null) {
                    mShareListener.onComplete(ShareConstant.SHARE_TYPE_FACEBOOK);
                }
            }

            public void onCancel() {
                if (mShareListener != null) {
                    mShareListener.onCancel(ShareConstant.SHARE_TYPE_FACEBOOK);
                }
            }

            public void onError(FacebookException error) {
                error.printStackTrace();
                if (mShareListener != null) {
                    mShareListener.onError(ShareConstant.SHARE_TYPE_FACEBOOK, ErrorCode.THIRD_PARTY_SDK_ERROR, "Tripartite sdk returns an error");
                }
            }
        });

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareContent content = null;

            if (shareContent.showUrl()) {
                content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(shareContent.url))
                        .setContentTitle(shareContent.title)
                        .setContentDescription(shareContent.content)
                        .build();
            } else {
                Uri uri = Uri.fromFile(new File(shareContent.bitmapUrl));
                String file = uri.getPath();
                Log.i(TAG, "facebook share file path:"+file);
                SharePhoto photo = new SharePhoto.Builder()
                        .setCaption(shareContent.content)
                        .setImageUrl(uri)
                        .build();
                content = new SharePhotoContent.Builder().addPhoto(photo).build();
            }

            shareDialog.show(content);
        } else if (mShareListener != null) {
            mShareListener.onError(ShareConstant.SHARE_TYPE_FACEBOOK, ErrorCode.THIRD_PARTY_SDK_ERROR, "Tripartite sdk returns an error");
        }
    }

    private void shareToQQZone(ShareContentItem shareContent, String targetUrl) {
        Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, shareContent.title);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareContent.content);
        params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mContext.getString(R.string.app_name));
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, targetUrl);

        // TODO multiple image
        ArrayList<String> value = new ArrayList<String>();
        value.add(shareContent.bitmapUrl);
        params.putStringArrayList(QQShare.SHARE_TO_QQ_IMAGE_URL, value);

        mTencent.shareToQzone(mContext, params, mShareToQQzone);
    }

    private void shareByIntent(ShareAppItem appItem, ShareContentItem shareContent) {
        if (appItem == null || appItem.resolveInfo == null)
            return;

        ResolveInfo info = appItem.resolveInfo;
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));

            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareContent.title);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent.content);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(shareContent.bitmapUrl)));
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mContext.startActivity(shareIntent);
        } catch (Exception e) {
            if(DEBUG){
                e.printStackTrace();
            }
        }
    }

    private boolean checkAppInstalled(String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;

        try {
            ApplicationInfo info = mContext.getPackageManager()
                    .getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // 定义分享监听器接口
    public static interface SharingListener {

        void onComplete(int provider);

        void onError(int provider, int errorCode);

        void onCancel(int provider);
    }
}