package com.xinglan.mgit.tasks.repo;

import com.xinglan.mgit.database.models.Repo;
import com.xinglan.mgit.common.exceptions.StopTaskException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;

import java.util.Set;

public class StatusTask extends RepoOpTask {

    private final GetStatusCallback mCallback;
    private final StringBuffer mResult = new StringBuffer();
    public StatusTask(Repo repo, GetStatusCallback callback) {
        super(repo);
        mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return status();
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null && isSuccess) {
            mCallback.postStatus(mResult.toString());
        }
    }

    public void executeTask() {
        execute();
    }

    private boolean status() {
        try {
            org.eclipse.jgit.api.Status status = mRepo.getGit().status().call();
            convertStatus(status);
        } catch (NoWorkTreeException e) {
            setException(e);
            return false;
        } catch (GitAPIException e) {
            setException(e);
            return false;
        } catch (StopTaskException e) {
            return false;
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }

    private void convertStatus(org.eclipse.jgit.api.Status status) {
        if (!status.hasUncommittedChanges() && status.isClean()) {
            mResult.append("Nothing to commit, working directory clean");
            return;
        }
        // TODO if working dir not clean
        convertStatusSet("Added files:", status.getAdded());
        convertStatusSet("Changed files:", status.getChanged());
        convertStatusSet("Removed files:", status.getRemoved());
        convertStatusSet("Missing files:", status.getMissing());
        convertStatusSet("Modified files:", status.getModified());
        convertStatusSet("Conflicting files:", status.getConflicting());
        convertStatusSet("Untracked files:", status.getUntracked());

    }

    private void convertStatusSet(String type, Set<String> status) {
        if (status.isEmpty())
            return;
        mResult.append(type);
        mResult.append("\n\n");
        for (String s : status) {
            mResult.append('\t');
            mResult.append(s);
            mResult.append('\n');
        }
        mResult.append("\n");
    }

    public interface GetStatusCallback {
        void postStatus(String result);
    }

}
