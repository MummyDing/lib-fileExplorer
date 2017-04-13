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

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.github.mummyding.ymsecurity.lib_filemanager.R;
import com.github.mummyding.ymsecurity.lib_filemanager.model.FileInfoModel;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileIconHelper;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileViewInteractionHub;
import com.github.mummyding.ymsecurity.lib_filemanager.util.Util;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileCategoryHelper;

import java.util.Collection;
import java.util.HashMap;

public class FileHomeAdapter extends CursorAdapter {

    private final LayoutInflater mFactory;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileIconHelper mFileIcon;

    private HashMap<Integer, FileInfoModel> mFileNameList = new HashMap<Integer, FileInfoModel>();

    private Context mContext;

    public FileHomeAdapter(Context context, Cursor cursor,
                           FileViewInteractionHub f, FileIconHelper fileIcon) {
        super(context, cursor, false /* auto-requery */);
        mFactory = LayoutInflater.from(context);
        mFileViewInteractionHub = f;
        mFileIcon = fileIcon;
        mContext = context;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        FileInfoModel fileInfoModel = getFileItem(cursor.getPosition());
        if (fileInfoModel == null) {
            // file is not existing, create a fake info
            fileInfoModel = new FileInfoModel();
            fileInfoModel.dbId = cursor.getLong(FileCategoryHelper.COLUMN_ID);
            fileInfoModel.filePath = cursor.getString(FileCategoryHelper.COLUMN_PATH);
            fileInfoModel.fileName = Util.getNameFromFilepath(fileInfoModel.filePath);
            fileInfoModel.fileSize = cursor.getLong(FileCategoryHelper.COLUMN_SIZE);
            fileInfoModel.ModifiedDate = cursor.getLong(FileCategoryHelper.COLUMN_DATE);
        }
        FileListItem.setupFileListItemInfo(mContext, view, fileInfoModel, mFileIcon,
                mFileViewInteractionHub);
        view.findViewById(R.id.category_file_checkbox_area).setOnClickListener(
                new FileListItem.FileItemOnClickListener(mContext, mFileViewInteractionHub));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mFactory.inflate(R.layout.category_file_browser_item, parent, false);
    }

    @Override
    public void changeCursor(Cursor cursor) {
        mFileNameList.clear();
        super.changeCursor(cursor);
    }

    public Collection<FileInfoModel> getAllFiles() {
        if (mFileNameList.size() == getCount())
            return mFileNameList.values();

        Cursor cursor = getCursor();
        if (cursor.moveToFirst()) {
            do {
                Integer position = Integer.valueOf(cursor.getPosition());
                if (mFileNameList.containsKey(position))
                    continue;
                FileInfoModel fileInfoModel = getFileInfo(cursor);
                if (fileInfoModel != null) {
                    mFileNameList.put(position, fileInfoModel);
                }
            } while (cursor.moveToNext());
        }

        return mFileNameList.values();
    }

    public FileInfoModel getFileItem(int pos) {
        Integer position = Integer.valueOf(pos);
        if (mFileNameList.containsKey(position))
            return mFileNameList.get(position);

        Cursor cursor = (Cursor) getItem(pos);
        FileInfoModel fileInfoModel = getFileInfo(cursor);
        if (fileInfoModel == null)
            return null;

        fileInfoModel.dbId = cursor.getLong(FileCategoryHelper.COLUMN_ID);
        mFileNameList.put(position, fileInfoModel);
        return fileInfoModel;
    }

    private FileInfoModel getFileInfo(Cursor cursor) {
        return (cursor == null || cursor.getCount() == 0) ? null : Util
                .GetFileInfo(cursor.getString(FileCategoryHelper.COLUMN_PATH));
    }
}
