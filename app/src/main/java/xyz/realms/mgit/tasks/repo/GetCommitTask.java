package xyz.realms.mgit.tasks.repo;

import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

import xyz.realms.mgit.database.Repo;
import xyz.realms.mgit.errors.StopTaskException;
import xyz.realms.mgit.tasks.MGitAsyncTask;

public class GetCommitTask extends MGitAsyncTask implements MGitAsyncTask.MGitAsyncCallBack {

    private final GetCommitCallback mCallback;
    private final String mFile;
    private List<RevCommit> mResult;

    public GetCommitTask(Repo repo, String file, GetCommitCallback callback) {
        super(repo);
        mGitAsyncCallBack = this;
        mFile = file;
        mCallback = callback;
    }

    @Override
    public boolean doInBackground(Void... params) {
        return getCommitsList();
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onProgressUpdate(String... progress) {

    }

    @Override
    public void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        if (mCallback != null) {
            mCallback.postCommits(mResult);
        }
    }

    public boolean getCommitsList() {
        try {
            LogCommand cmd = mRepo.getGit().log();
            if (mFile != null) cmd.addPath(mFile);
            Iterable<RevCommit> commits = cmd.call();
            mResult = new ArrayList<RevCommit>();
            for (RevCommit commit : commits) {
                mResult.add(commit);
            }
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


    public interface GetCommitCallback {
        void postCommits(List<RevCommit> commits);
    }

}
