
package com.github.mummyding.ymsecurity.lib_filemanager.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mummyding.ymbase.base.BaseFragment;

import com.github.mummyding.ymsecurity.lib_filemanager.FileManagerActivity;
import com.github.mummyding.ymsecurity.lib_filemanager.R;
import com.github.mummyding.ymsecurity.lib_filemanager.model.FileInfoModel;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileCategoryHelper;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileCategoryHelper.CategoryInfo;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileCategoryHelper.FileCategory;
import com.github.mummyding.ymsecurity.lib_filemanager.FileManagerActivity.IBackPressedListener;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileIconHelper;
import com.github.mummyding.ymsecurity.lib_filemanager.adapter.FileHomeAdapter;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileSortHelper;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileViewInteractionHub;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileViewInteractionHub.Mode;
import com.github.mummyding.ymsecurity.lib_filemanager.model.GlobalConsts;
import com.github.mummyding.ymsecurity.lib_filemanager.IFileInteractionListener;
import com.github.mummyding.ymsecurity.lib_filemanager.util.Util;
import com.github.mummyding.ymsecurity.lib_filemanager.util.Util.SDCardInfo;
import com.github.mummyding.ymsecurity.lib_filemanager.widget.CategoryBar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class FileHomeFragment extends BaseFragment implements IFileInteractionListener,
        IBackPressedListener {

    public static final String EXT_FILETER_KEY = "ext_filter";

    private static final String LOG_TAG = "FileHomeFragment";

    private static HashMap<Integer, FileCategory> button2Category = new HashMap<Integer, FileCategory>();

    private HashMap<FileCategory, Integer> categoryIndex = new HashMap<FileCategory, Integer>();

    private FileHomeAdapter mAdapter;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileCategoryHelper mFileCagetoryHelper;

    private FileIconHelper mFileIconHelper;

    private CategoryBar mCategoryBar;

    private ScannerReceiver mScannerReceiver;

    private ViewPage curViewPage = ViewPage.Invalid;

    private ViewPage preViewPage = ViewPage.Invalid;

    private Activity mActivity;

    private View mRootView;

    private View mLoadingView;

    static {
        button2Category.put(R.id.category_music, FileCategory.Music);
        button2Category.put(R.id.category_video, FileCategory.Video);
        button2Category.put(R.id.category_picture, FileCategory.Picture);
        button2Category.put(R.id.category_document, FileCategory.Doc);
        button2Category.put(R.id.category_zip, FileCategory.Zip);
        button2Category.put(R.id.category_apk, FileCategory.Apk);
    }

    private ListView mFileListView;
    private FileListFragment mFileListFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        mFileListFragment = (FileListFragment) ((FileManagerActivity) mActivity)
                .getFragment(Util.SDCARD_TAB_INDEX);
        mRootView = inflater.inflate(R.layout.file_explorer_category, container, false);
        curViewPage = ViewPage.Invalid;
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        mFileViewInteractionHub.setMode(Mode.View);
        mFileViewInteractionHub.setRootPath("/");
        mFileIconHelper = new FileIconHelper(mActivity);
        mLoadingView = mRootView.findViewById(R.id.loading_view);
        registerScannerReceiver();
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter = new FileHomeAdapter(mActivity, null, mFileViewInteractionHub, mFileIconHelper);
                mFileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
                mFileListView.setAdapter(mAdapter);
                setupClick();
                setupCategoryInfo();
                updateUI();
                mLoadingView.setVisibility(View.GONE);
            }
        }, 500);
    }

    private void registerScannerReceiver() {
        mScannerReceiver = new ScannerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        mActivity.registerReceiver(mScannerReceiver, intentFilter);
    }

    private void setupCategoryInfo() {
        mFileCagetoryHelper = new FileCategoryHelper(mActivity);

        mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        int[] imgs = new int[] {
                R.drawable.category_bar_music, R.drawable.category_bar_video,
                R.drawable.category_bar_picture,R.drawable.category_bar_document,
                R.drawable.category_bar_zip,
                R.drawable.category_bar_apk, R.drawable.category_bar_other
        };

        for (int i = 0; i < imgs.length; i++) {
            mCategoryBar.addCategory(imgs[i]);
        }

        for (int i = 0; i < FileCategoryHelper.sCategories.length; i++) {
            categoryIndex.put(FileCategoryHelper.sCategories[i], i);
        }
    }

    public void refreshCategoryInfo() {
        SDCardInfo sdCardInfo = Util.getSDCardInfo();
        if (sdCardInfo != null) {
            mCategoryBar.setFullValue(sdCardInfo.total);
            setTextView(R.id.sd_card_capacity, getString(R.string.sd_card_size, Util.convertStorage(sdCardInfo.total)));
            setTextView(R.id.sd_card_available, getString(R.string.sd_card_available, Util.convertStorage(sdCardInfo.free)));
        }

        mFileCagetoryHelper.refreshCategoryInfo();

        // the other category size should include those files didn't get scanned.
        long size = 0;
        for (FileCategory fc : FileCategoryHelper.sCategories) {
            CategoryInfo categoryInfo = mFileCagetoryHelper.getCategoryInfos().get(fc);
            setCategoryCount(fc, categoryInfo.count);

            // other category size should be set separately with calibration
            if(fc == FileCategory.Other)
                continue;

            setCategorySize(fc, categoryInfo.size);
            setCategoryBarValue(fc, categoryInfo.size);
            size += categoryInfo.size;
        }

        if (sdCardInfo != null) {
            long otherSize = sdCardInfo.total - sdCardInfo.free - size;
            setCategorySize(FileCategory.Other, otherSize);
            setCategoryBarValue(FileCategory.Other, otherSize);
        }

        if (mCategoryBar.getVisibility() == View.VISIBLE) {
            mCategoryBar.startAnimation();
        }
    }

    public enum ViewPage {
        Home, Category, NoSD, Invalid
    }

    private void showPage(ViewPage p) {
        if (curViewPage == p) return;

        curViewPage = p;

        showView(R.id.file_path_list, false);
        showView(R.id.navigation_bar, false);
        showView(R.id.category_page, false);
        showView(R.id.operation_bar, false);
        showView(R.id.sd_not_available_page, false);
        showEmptyView(false);

        switch (p) {
            case Home:
                showView(R.id.category_page, true);
                break;
            case Category:
                showView(R.id.navigation_bar, true);
                showView(R.id.file_path_list, true);
                showEmptyView(mAdapter.getCount() == 0);
                break;
            case NoSD:
                showView(R.id.sd_not_available_page, true);
                break;
        }
    }

    private void showEmptyView(boolean show) {
        View emptyView = mActivity.findViewById(R.id.empty_view);
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showView(int id, boolean show) {
        View view = mRootView.findViewById(id);
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileCategory f = button2Category.get(v.getId());
            if (f != null) {
                onCategorySelected(f);
            }
        }

    };

    private void setCategoryCount(FileCategory fc, long count) {
        int id = getCategoryCountId(fc);
        if (id == 0)
            return;

        setTextView(id, "(" + count + ")");
    }

    private void setTextView(int id, String t) {
        TextView text = (TextView) mRootView.findViewById(id);
        text.setText(t);
    }

    private void onCategorySelected(FileCategory f) {
        if (mFileCagetoryHelper.getCurCategory() != f) {
            mFileCagetoryHelper.setCurCategory(f);
            mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath()
                    + getString(mFileCagetoryHelper.getCurCategoryNameResId()));
            mFileViewInteractionHub.refreshFileList();
        }

        showPage(ViewPage.Category);

    }

    private void setupClick(int id) {
        View button = mRootView.findViewById(id);
        button.setOnClickListener(onClickListener);
    }

    private void setupClick() {
        setupClick(R.id.category_music);
        setupClick(R.id.category_video);
        setupClick(R.id.category_picture);
        setupClick(R.id.category_document);
        setupClick(R.id.category_zip);
        setupClick(R.id.category_apk);
    }

    @Override
    public boolean onBack() {
        if (isHomePage() || curViewPage == ViewPage.NoSD || mFileViewInteractionHub == null) {
            return false;
        }

        return mFileViewInteractionHub.onBackPressed();
    }

    public boolean isHomePage() {
        return curViewPage == ViewPage.Home;
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (curViewPage != ViewPage.Category) {
            return;
        }
        mFileViewInteractionHub.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!isHomePage()) {
            mFileViewInteractionHub.onPrepareOptionsMenu(menu);
        }
    }*/

    public boolean onRefreshFileList(String path, FileSortHelper sort) {
        FileCategory curCategory = mFileCagetoryHelper.getCurCategory();
        if (curCategory == FileCategory.All)
            return false;

        Cursor c = mFileCagetoryHelper.query(curCategory, sort.getSortMethod());
        showEmptyView(c == null || c.getCount() == 0);
        mAdapter.changeCursor(c);

        return true;
    }

    @Override
    public View getViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                showEmptyView(mAdapter.getCount() == 0);
            }

        });
    }

    @Override
    public void onPick(FileInfoModel f) {
        // do nothing
    }

    @Override
    public boolean shouldShowOperationPane() {
        return true;
    }

    @Override
    public boolean onOperation(int id) {
        mFileViewInteractionHub.addContextMenuSelectedItem();
        switch (id) {
            case GlobalConsts.MENU_COPY:
                copyFileInFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case GlobalConsts.MENU_MOVE:
                startMoveToFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case GlobalConsts.OPERATION_UP_LEVEL:
                setHasOptionsMenu(false);
                showPage(ViewPage.Home);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public String getDisplayPath(String path) {
        return getString(R.string.tab_category) + path;
    }

    @Override
    public String getRealPath(String displayPath) {
        return "";
    }

    @Override
    public boolean onNavigation(String path) {
        showPage(ViewPage.Home);
        return true;
    }

    @Override
    public boolean shouldHideMenu(int menu) {
        return (menu == GlobalConsts.MENU_NEW_FOLDER
                || menu == GlobalConsts.MENU_PASTE || menu == GlobalConsts.MENU_SHOWHIDE);
    }

    @Override
    public void addSingleFile(FileInfoModel file) {
        refreshList();
    }

    @Override
    public Collection<FileInfoModel> getAllFiles() {
        return mAdapter.getAllFiles();
    }

    @Override
    public FileInfoModel getItem(int pos) {
        return mAdapter.getFileItem(pos);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getCount();
    }

    @Override
    public void sortCurrentList(FileSortHelper sort) {
        refreshList();
    }

    private void refreshList() {
        mFileViewInteractionHub.refreshFileList();
    }

    private void copyFileInFileView(ArrayList<FileInfoModel> files) {
        if (files.size() == 0) return;
        mFileListFragment.copyFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    }

    private void startMoveToFileView(ArrayList<FileInfoModel> files) {
        if (files.size() == 0) return;
        mFileListFragment.moveToFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    private static int getCategoryCountId(FileCategory fc) {
        switch (fc) {
            case Music:
                return R.id.category_music_count;
            case Video:
                return R.id.category_video_count;
            case Picture:
                return R.id.category_picture_count;
            case Doc:
                return R.id.category_document_count;
            case Zip:
                return R.id.category_zip_count;
            case Apk:
                return R.id.category_apk_count;
        }

        return 0;
    }

    private void setCategorySize(FileCategory fc, long size) {
        int txtId = 0;
        int resId = 0;
        switch (fc) {
            case Music:
                txtId = R.id.category_legend_music;
                resId = R.string.category_music;
                break;
            case Video:
                txtId = R.id.category_legend_video;
                resId = R.string.category_video;
                break;
            case Picture:
                txtId = R.id.category_legend_picture;
                resId = R.string.category_picture;
                break;
            case Doc:
                txtId = R.id.category_legend_document;
                resId = R.string.category_document;
                break;
            case Zip:
                txtId = R.id.category_legend_zip;
                resId = R.string.category_zip;
                break;
            case Apk:
                txtId = R.id.category_legend_apk;
                resId = R.string.category_apk;
                break;
            case Other:
                txtId = R.id.category_legend_other;
                resId = R.string.category_other;
                break;
        }

        if (txtId == 0 || resId == 0)
            return;

        setTextView(txtId, getString(resId) + ":" + Util.convertStorage(size));
    }

    private void setCategoryBarValue(FileCategory f, long size) {
        if (mCategoryBar == null) {
            mCategoryBar = (CategoryBar) mRootView.findViewById(R.id.category_bar);
        }
        mCategoryBar.setCategoryValue(categoryIndex.get(f), size);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mActivity != null) {
            mActivity.unregisterReceiver(mScannerReceiver);
        }
    }

    private class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast: " + action.toString());
            // handle intents related to external storage
            if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED) || action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                notifyFileChanged();
            }
        }
    }

    private void updateUI() {
        boolean sdCardReady = Util.isSDCardReady();
        if (sdCardReady) {
            if (preViewPage != ViewPage.Invalid) {
                showPage(preViewPage);
                preViewPage = ViewPage.Invalid;
            } else if (curViewPage == ViewPage.Invalid || curViewPage == ViewPage.NoSD) {
                showPage(ViewPage.Home);
            }
            refreshCategoryInfo();
            // refresh file list
            mFileViewInteractionHub.refreshFileList();
            // refresh file list view in another tab
        } else {
            preViewPage = curViewPage;
            showPage(ViewPage.NoSD);
        }
    }

    // process file changed notification, using a timer to avoid frequent
    // refreshing due to batch changing on file system
    synchronized public void notifyFileChanged() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                timer = null;
                Message message = new Message();
                message.what = MSG_FILE_CHANGED_TIMER;
                handler.sendMessage(message);
            }

        }, 1000);
    }

    private static final int MSG_FILE_CHANGED_TIMER = 100;

    private Timer timer;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FILE_CHANGED_TIMER:
                    updateUI();
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    public void runOnUiThread(Runnable r) {
        mActivity.runOnUiThread(r);
    }
}
