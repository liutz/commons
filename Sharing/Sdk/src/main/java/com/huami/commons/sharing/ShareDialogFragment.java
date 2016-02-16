package com.huami.commons.sharing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.huami.android.widget.AlertDialog;
import com.huami.android.widget.BottomDialog;
import com.huami.libs.Analytics;
import com.huami.midong.social.R;
import com.huami.midong.social.share.SocialShareManager.ShareListener;

import java.util.Collections;
import java.util.List;


public class ShareDialogFragment extends BottomDialog implements AdapterView.OnItemClickListener, AlertDialog.OnItemClickListener {

    public static final String ARG_SHARE_ITEM = "shareItem";
    public static final String ARG_TYPE = "share_type";
    public static final String ARG_FILTER = "share_filter"; // 开启后将只显示通讯录，不显示社交圈子的分享入口
    public static final String ARG_IS_ASYN = "is_asyn";
    private final String mActivityType = "GPSResultActivity";
    private ShareAdapter mAdapter;
    private SocialShareManager mShareManager;
    private ShareContentItem mShareItem;
    private boolean mOnlyImage = false;
    private List<ShareAppItem> mItems;
    private boolean mIsAsyn = false;
    private ShareListener mShareListener;
    private OnClickListener mOnClickListener;
    private ShareAppItem mShareTarget = null;
    private Status mCurrentStatus = Status.Init;
    private String mType;

    public ShareDialogFragment() {
        super(R.layout.view_share_dialog);
    }

    /**
     * 构建ShareDialog 对话框
     */
    @SuppressLint("NewApi")
    public static ShareDialogFragment createDialog(Context context, Bundle bundle) {
        Fragment fragment = instantiate(context, ShareDialogFragment.class.getName());
        fragment.setArguments(bundle);
        return (ShareDialogFragment) fragment;
    }

    public void setShareListener(ShareListener listener) {
        mShareListener = listener;
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public synchronized void notifyShareItemIsReady(ShareContentItem item) {
        if (mIsAsyn == false || isShareItemAvailable(item) == false)
            return;

        mShareItem = item;
        if (mCurrentStatus == Status.ClickedNotShare) {
            if (mShareTarget == null) {
                throw new IllegalStateException();
            }
            mShareManager.shareTo(mShareTarget, mShareItem);
            mCurrentStatus = Status.Init;
            return;
        }
        if (mCurrentStatus != Status.Init) {
            throw new IllegalStateException();
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle arguments = getArguments();

        boolean addFitler = arguments.getBoolean(ARG_FILTER, false);
        mIsAsyn = arguments.getBoolean(ARG_IS_ASYN, false);

        mShareItem = (ShareContentItem) arguments.getSerializable(ARG_SHARE_ITEM);
        mOnlyImage = arguments.getBoolean(ARG_TYPE, false);

        mShareManager = new SocialShareManager(activity);
        mShareManager.setShareListener(mShareListener);
        mItems = mShareManager.getAllShareItems();
        mAdapter = new ShareAdapter(mItems);

        if (mIsAsyn == false && !isShareItemAvailable(mShareItem)) {
            throw new IllegalArgumentException("shareitem is unavailable while ARG_IS_ASYN is not set");
        }
    }

    public String getAnalyticsKey(int position) {
        int label = mItems.get(position).label;
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mType != null && mType.equals(mActivityType)) {
            Analytics.event(getActivity(), Analytics.EventId.RUN_DETAILED_PAGE_SHARE_WIDGET, getAnalyticsKey(position));

        }
        if (mOnClickListener != null) {
            mOnClickListener.onClick(view);
        }
        ShareAppItem target = mAdapter.getItem(position);
        if (isShareItemAvailable(mShareItem)) {
            shareItem(target);
            mCurrentStatus = Status.Init;
            return;
        }
        if (mIsAsyn) {
            mShareTarget = target;
            mCurrentStatus = Status.ClickedNotShare;
            return;
        }
        throw new IllegalStateException("share item is not available");
    }

    @Override
    protected void onCreateSubView(View subView) {
        GridView gridView = (GridView) subView.findViewById(R.id.share_list);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

        subView.findViewById(R.id.dismiss_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private boolean isShareItemAvailable(ShareContentItem item) {
        return item != null;
    }

    public void setType(String type) {
        mType = type;
    }

    private void shareItem(ShareAppItem itemApp) {
        mShareManager.shareTo(itemApp, mShareItem);
        if (itemApp.enable) {
            mShareManager.showToast(R.string.share_prepare_tips);
            dismiss();
        } else {
            int msgId = R.string.share_uninstall_client;
            if (R.string.share_weixin_label == itemApp.label || R.string.share_pengyouquan_label == itemApp.label) {
                msgId = R.string.share_uninstall_weixin;
            } else if (R.string.share_qq_label == itemApp.label || R.string.share_qq_zone_label == itemApp.label) {
                msgId = R.string.share_uninstall_qq;
            } else if (R.string.share_weibo_label == itemApp.label) {
                msgId = R.string.share_uninstall_weibo;
            }
            mShareManager.showToast(msgId);
        }
    }

    private enum Status {
        Init, ClickedNotShare
    }

    private final class ShareAdapter extends BaseAdapter {
        private final List<ShareAppItem> ITEMS_EMPTY = Collections.emptyList();

        private List<ShareAppItem> mItems = null;

        public ShareAdapter(List<ShareAppItem> items) {
            mItems = items;
            if (mItems == null) mItems = ITEMS_EMPTY;
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
            return 0;
        }

        @SuppressLint("NewApi")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.view_share_dialog_item, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.label);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);

            ShareAppItem shareTarget = getItem(position);

            textView.setText(shareTarget.label);
            icon.setBackgroundResource(shareTarget.icon);
            icon.setEnabled(shareTarget.enable);

            return convertView;
        }
    }

}
