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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ActionMode;

import com.github.mummyding.ymbase.base.BaseSwipeBackActivity;

import com.github.mummyding.ymsecurity.lib_filemanager.fragment.FileHomeFragment;
import com.github.mummyding.ymsecurity.lib_filemanager.fragment.FileListFragment;


public class FileManagerActivity extends BaseSwipeBackActivity {
    ActionMode mActionMode;
    private FileHomeFragment mFileHomeFragment;
    private FileListFragment mFileListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pager);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setLogo(getResources().getDrawable(R.drawable.transparent));
        mFileListFragment = new FileListFragment();
        mFileHomeFragment = new FileHomeFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_layout, mFileHomeFragment);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = mFileHomeFragment;
        if (!backPressedListener.onBack()) {
            super.onBackPressed();
        }
    }

    public interface IBackPressedListener {
        /**
         * 处理back事件。
         *
         * @return True: 表示已经处理; False: 没有处理，让基类处理。
         */
        boolean onBack();
    }

    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public Fragment getFragment(int tabIndex) {
        if (tabIndex == 0) {
            return mFileHomeFragment;
        } else {
            return mFileListFragment;
        }
    }
}
