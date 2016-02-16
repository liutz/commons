package com.huami.commons.sharing;

import android.text.TextUtils;

import java.io.Serializable;

public class ShareContentItem implements Serializable {

    public String content;
    public String title;
    public String bitmapUrl;
    public String topic;
    public String url;
    public ShareContentType mShareType;
    public ShareContentItem() {
        mShareType = ShareContentType.IMAGE;
    }

    public ShareContentItem(ShareContentType shareType) {
        mShareType = shareType;
    }

    public boolean showUrl() {
        if (TextUtils.isEmpty(url))
            return false;

        return mShareType == ShareContentType.TEXT_IMAGE_URL;
    }

    public enum ShareContentType {IMAGE, TEXT_IMAGE_URL}

}