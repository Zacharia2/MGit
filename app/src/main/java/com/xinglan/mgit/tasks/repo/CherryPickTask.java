package com.xinglan.mgit.tasks.repo;

import com.xinglan.mgit.R;
import com.xinglan.mgit.database.models.Repo;
import com.xinglan.mgit.common.exceptions.StopTaskException;

import org.eclipse.jgit.lib.ObjectId;

public class CherryPickTask extends RepoOpTask {

    public String mCommitStr;
    private final AsyncTaskPostCallback mCallback;

    public CherryPickTask(Repo repo, String commit,
                          AsyncTaskPostCallback callback) {
        super(repo);
        mCommitStr = commit;
        mCallback = callback;
        setSuccessMsg(R.string.success_cherry_pick);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return cherrypick();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.onPostExecute(isSuccess);
        }
    }

    public boolean cherrypick() {
        try {
            ObjectId commit = mRepo.getGit().getRepository()
                .resolve(mCommitStr);
            mRepo.getGit().cherryPick().include(commit).call();
        } catch (StopTaskException e) {
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }
}
