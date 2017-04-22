/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.mummyding.ymsecurity.lib_filemanager.adapter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.github.mummyding.ymsecurity.lib_filemanager.FileManagerActivity;
import com.github.mummyding.ymsecurity.lib_filemanager.R;
import com.github.mummyding.ymsecurity.lib_filemanager.fragment.FileListFragment;
import com.github.mummyding.ymsecurity.lib_filemanager.model.FileInfoModel;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileViewInteractionHub;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileViewInteractionHub.Mode;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileIconHelper;
import com.github.mummyding.ymsecurity.lib_filemanager.util.Util;

public class FileListItem {
    public static void setupFileListItemInfo(Context context, View view,
                                             FileInfoModel fileInfoModel, FileIconHelper fileIcon,
                                             FileViewInteractionHub fileViewInteractionHub) {

        // if in moving mode, show selected file always
        if (fileViewInteractionHub.isMoveState()) {
            fileInfoModel.Selected = fileViewInteractionHub.isFileSelected(fileInfoModel.filePath);
        }

        RadioButton checkbox = (RadioButton) view.findViewById(R.id.file_checkbox);
        if (fileViewInteractionHub.getMode() == Mode.Pick) {
            checkbox.setVisibility(View.GONE);
        } else {
            checkbox.setVisibility(fileViewInteractionHub.canShowCheckBox() ? View.VISIBLE : View.GONE);
            checkbox.setChecked(fileInfoModel.Selected);
            checkbox.setTag(fileInfoModel);
            view.setSelected(fileInfoModel.Selected);
        }

        Util.setText(view, R.id.file_name, fileInfoModel.fileName);
        Util.setText(view, R.id.file_count, fileInfoModel.IsDir ? "(" + fileInfoModel.Count + ")" : "");
        Util.setText(view, R.id.modified_time, Util.formatDateString(context, fileInfoModel.ModifiedDate));
        Util.setText(view, R.id.file_size, (fileInfoModel.IsDir ? "" : Util.convertStorage(fileInfoModel.fileSize)));

        ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
        ImageView lFileImageFrame = (ImageView) view.findViewById(R.id.file_image_frame);

        if (fileInfoModel.IsDir) {
            lFileImageFrame.setVisibility(View.GONE);
            lFileImage.setImageResource(R.drawable.folder);
        } else {
            fileIcon.setIcon(fileInfoModel, lFileImage, lFileImageFrame);
        }
    }

    public static class FileItemOnClickListener implements OnClickListener {
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        public FileItemOnClickListener(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public void onClick(View v) {
            RadioButton img = (RadioButton) v.findViewById(R.id.file_checkbox);
            assert (img != null && img.getTag() != null);

            FileInfoModel tag = (FileInfoModel) img.getTag();
            tag.Selected = !tag.Selected;
            ActionMode actionMode = ((FileManagerActivity) mContext).getActionMode();
            if (actionMode == null) {
                actionMode = ((FileManagerActivity) mContext)
                        .startActionMode(new ModeCallback(mContext,
                                mFileViewInteractionHub));
                ((FileManagerActivity) mContext).setActionMode(actionMode);
            } else {
                actionMode.invalidate();
            }
            if (mFileViewInteractionHub.onCheckItem(tag, v)) {
                img.setChecked(tag.Selected);
            } else {
                tag.Selected = !tag.Selected;
            }
            Util.updateActionModeTitle(actionMode, mContext,
                    mFileViewInteractionHub.getSelectedFileList().size());
        }
    }

    public static class ModeCallback implements ActionMode.Callback {
        private Menu mMenu;
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        private void initMenuItemSelectAllOrCancel() {
            boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
            mMenu.findItem(R.id.action_cancel).setVisible(isSelectedAll);
            mMenu.findItem(R.id.action_select_all).setVisible(!isSelectedAll);
        }

        private void scrollToSDcardTab() {
            ActionBar bar = ((FileManagerActivity) mContext).getActionBar();
            if (bar.getSelectedNavigationIndex() != Util.SDCARD_TAB_INDEX) {
                bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
            }
        }

        public ModeCallback(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            mMenu = menu;
            inflater.inflate(R.menu.operation_menu, mMenu);
            initMenuItemSelectAllOrCancel();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mMenu.findItem(R.id.action_copy_path).setVisible(
                    mFileViewInteractionHub.getSelectedFileList().size() == 1);
            mMenu.findItem(R.id.action_cancel).setVisible(
            		mFileViewInteractionHub.isSelected());
            mMenu.findItem(R.id.action_select_all).setVisible(
            		!mFileViewInteractionHub.isSelectedAll());
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.action_delete) {
                mFileViewInteractionHub.onOperationDelete();
                mode.finish();
            } else if (id == R.id.action_send) {
                mFileViewInteractionHub.onOperationSend();
                mode.finish();
            } else if (id == R.id.action_copy_path) {
                mFileViewInteractionHub.onOperationCopyPath();
                mode.finish();
            } else if (id == R.id.action_cancel){
                mFileViewInteractionHub.clearSelection();
                initMenuItemSelectAllOrCancel();
                mode.finish();
            }  else if (id == R.id.action_select_all) {
                mFileViewInteractionHub.onOperationSelectAll();
                initMenuItemSelectAllOrCancel();
            }

            Util.updateActionModeTitle(mode, mContext, mFileViewInteractionHub
                    .getSelectedFileList().size());
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mFileViewInteractionHub.clearSelection();
            ((FileManagerActivity) mContext).setActionMode(null);
        }
    }
}
