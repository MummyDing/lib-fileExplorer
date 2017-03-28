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

package net.micode.fileexplorer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ActionMode;

import com.github.mummyding.ymbase.base.BaseSwipeBackActivity;

import net.micode.fileexplorer.fragment.FileCategoryFragment;
import net.micode.fileexplorer.fragment.FileViewFragment;

public class FileManagerActivity extends BaseSwipeBackActivity {
    ActionMode mActionMode;
    private FileCategoryFragment mFileCategoryFragment;
    private FileViewFragment mFileViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pager);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        mFileViewFragment = new FileViewFragment();
        mFileCategoryFragment = new FileCategoryFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_layout, mFileCategoryFragment);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = mFileCategoryFragment;
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
            return mFileCategoryFragment;
        } else {
            return mFileViewFragment;
        }
    }
}