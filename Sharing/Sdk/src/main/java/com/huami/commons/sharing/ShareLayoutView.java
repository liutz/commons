package com.huami.commons.sharing;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.huami.libs.Analytics;
import com.huami.libs.logs.Debug;
import com.huami.midong.social.R;

import java.util.ArrayList;
import java.util.List;

public class ShareLayoutView extends LinearLayout {
    private static final String TAG = "ShareLayoutView";

    private Context mContext;
    private SocialShareManager mShareManager;
    private List<ShareAppItem> mItems;
    private ShareAdapter mAdapter;
    private ShareAppItem mSharingItem;
    private boolean mIsSharing = false;
    private PreShareListener mShareListener;
    private int mPosition;
    private SocialShareManager.ShareListener mShareResulteListener = null;

    public ShareLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initDatas();
        initViews();
    }

    public void setShareResultListener(SocialShareManager.ShareListener listener) {
        mShareResulteListener = listener;
    }

    private void initDatas() {
        mShareManager = new SocialShareManager((Activity) mContext);
        mItems = mShareManager.getAllShareItems();
        mAdapter = new ShareAdapter(mItems);
    }

    private void initViews() {
        inflate(getContext(), R.layout.share_layout, this);
        GridView gridView = (GridView) findViewById(R.id.share_grid_view);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new MyOnItemClickListener());
    }

    public void setShareListener(PreShareListener listener) {
        mShareListener = listener;
    }

    public void onSharePrepared(ShareContentItem content) {
        if (content == null || mSharingItem == null) {
            return;
        }

        mShareManager.setShareListener(mShareResulteListener);
        mShareManager.shareTo(mSharingItem, content);
        mIsSharing = false;
        mSharingItem = null;
    }

    public String getAnalyticsKey() {
        int label = mItems.get(mPosition).label;
        if (R.string.share_weixin_label == label) {
            return Analytics.Key.WECHAT;
        }

        if (R.string.share_pengyouquan_label == label) {
            return Analytics.Key.MOMENT;
        }

        if (R.string.share_qq_label == label) {
            return Analytics.Key.QQ;
        }

        if (R.string.share_qq_zone_label == label) {
            return Analytics.Key.QZONE;
        }

        if (R.string.share_weibo_label == label) {
            return Analytics.Key.WEIBO;
        }

        return null;
    }

    public interface PreShareListener {
        void onPreShare(int position);
    }

    private final class ShareAdapter extends BaseAdapter {

        private List<ShareAppItem> mItems = null;

        public ShareAdapter(List<ShareAppItem> items) {
            mItems = items;
            if (mItems == null) {
                mItems = new ArrayList<ShareAppItem>();
            }
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public ShareAppItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.share_item_view, parent, false);
            }

            ShareAppItem shareTarget = getItem(position);
            ImageView icon = (ImageView) convertView.findViewById(R.id.share_item_iv);
            icon.setImageResource(shareTarget.icon);
            icon.setEnabled(shareTarget.enable);

            return convertView;
        }
    }

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mIsSharing) {
                Debug.fi(TAG, "preitem is shareing");
                return;
            }
            mIsSharing = true;
            mPosition = position;
            //点击分享布局时再去获取一下当前条目信息 更新是否可用
            List<ShareAppItem> allShareItems = mShareManager.getAllShareItems();
            mSharingItem = allShareItems.get(position);
            if (mShareListener != null && allShareItems.get(position).enable) {
                mShareListener.onPreShare(position);
            }

            if (allShareItems.get(position).enable) {
                mShareManager.showToast(R.string.share_prepare_tips);
            } else {
                mIsSharing = false;
                int msgId = R.string.share_uninstall_client;
                ShareAppItem saItem = allShareItems.get(position);
                if (R.string.share_weixin_label == saItem.label || R.string.share_pengyouquan_label == saItem.label) {
                    msgId = R.string.share_uninstall_weixin;
                } else if (R.string.share_qq_label == saItem.label || R.string.share_qq_zone_label == saItem.label) {
                    msgId = R.string.share_uninstall_qq;
                } else if (R.string.share_weibo_label == saItem.label) {
                    msgId = R.string.share_uninstall_weibo;
                }
                mShareManager.showToast(msgId);
            }
        }
    }

}
