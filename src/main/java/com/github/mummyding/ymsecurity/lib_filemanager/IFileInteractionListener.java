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

package com.github.mummyding.ymsecurity.lib_filemanager;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.github.mummyding.ymsecurity.lib_filemanager.model.FileInfoModel;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileIconHelper;
import com.github.mummyding.ymsecurity.lib_filemanager.util.FileSortHelper;

import java.util.Collection;

public interface IFileInteractionListener {

    public View getViewById(int id);

    public Context getContext();

    public void startActivity(Intent intent);

    public void onDataChanged();

    public void onPick(FileInfoModel f);

    public boolean shouldShowOperationPane();

    /**
     * Handle operation listener.
     * @param id
     * @return true: indicate have operated it; false: otherwise.
     */
    public boolean onOperation(int id);

    public String getDisplayPath(String path);

    public String getRealPath(String displayPath);

    public void runOnUiThread(Runnable r);

    // return true indicates the navigation has been handled
    public boolean onNavigation(String path);

    public boolean shouldHideMenu(int menu);

    public FileIconHelper getFileIconHelper();

    public FileInfoModel getItem(int pos);

    public void sortCurrentList(FileSortHelper sort);

    public Collection<FileInfoModel> getAllFiles();

    public void addSingleFile(FileInfoModel file);

    public boolean onRefreshFileList(String path, FileSortHelper sort);

    public int getItemCount();
}
