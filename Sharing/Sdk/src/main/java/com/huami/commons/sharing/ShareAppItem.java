package com.huami.commons.sharing;

import android.content.pm.ResolveInfo;

public class ShareAppItem {

    public int icon;
    public int label;
    public ResolveInfo resolveInfo = null;
    public boolean enable = false;//是否安装此应用

    public ShareAppItem(int icon, int label) {
        this.icon = icon;
        this.label = label;
    }
}