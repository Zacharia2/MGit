package com.xinglan.mgit.activities.delegate.actions;

import android.content.Intent;

import com.xinglan.mgit.activities.RepoDetailActivity;
import com.xinglan.mgit.activities.ViewFileActivity;
import com.xinglan.mgit.database.models.Repo;

/**
 * Created by phcoder on 05.12.15.
 */
public class RawConfigAction extends RepoAction {

    public RawConfigAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {
        Intent intent = new Intent(mActivity, ViewFileActivity.class);
        intent.putExtra(ViewFileActivity.TAG_FILE_NAME,
                mRepo.getDir().getAbsoluteFile() + "/.git/config");
        mActivity.startActivity(intent);
    }
}
