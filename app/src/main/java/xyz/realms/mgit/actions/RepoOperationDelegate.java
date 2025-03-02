package xyz.realms.mgit.actions;

import org.eclipse.jgit.lib.Ref;

import java.io.File;
import java.util.ArrayList;

import xyz.realms.mgit.database.Repo;
import xyz.realms.mgit.tasks.repo.AddToStageTask;
import xyz.realms.mgit.tasks.repo.CheckoutFileTask;
import xyz.realms.mgit.tasks.repo.CheckoutTask;
import xyz.realms.mgit.tasks.repo.DeleteFileFromRepoTask;
import xyz.realms.mgit.tasks.repo.MergeTask;
import xyz.realms.mgit.tasks.repo.UpdateIndexTask;
import xyz.realms.mgit.ui.explorer.RepoDetailActivity;
import xyz.realms.mgit.ui.utils.FsUtils;

public class RepoOperationDelegate {
    private final Repo mRepo;
    private final RepoDetailActivity mActivity;
    private final ArrayList<RepoAction> mActions = new ArrayList<>();

    public RepoOperationDelegate(Repo repo, RepoDetailActivity activity) {
        mRepo = repo;
        mActivity = activity;
        initActions();
    }

    private void initActions() {
        mActions.add(new SyncRepoAction(mRepo, mActivity));
        mActions.add(new NewBranchAction(mRepo, mActivity));
        mActions.add(new PullAction(mRepo, mActivity));
        mActions.add(new PushAction(mRepo, mActivity));
        mActions.add(new AddAllAction(mRepo, mActivity));
        mActions.add(new CommitAction(mRepo, mActivity));
        mActions.add(new UndoAction(mRepo, mActivity));
        mActions.add(new ResetAction(mRepo, mActivity));
        mActions.add(new MergeAction(mRepo, mActivity));
        mActions.add(new FetchAction(mRepo, mActivity));
        mActions.add(new RebaseAction(mRepo, mActivity));
        mActions.add(new CherryPickAction(mRepo, mActivity));
        mActions.add(new DiffAction(mRepo, mActivity));
        mActions.add(new NewFileAction(mRepo, mActivity));
        mActions.add(new NewDirAction(mRepo, mActivity));
        mActions.add(new AddRemoteAction(mRepo, mActivity));
        mActions.add(new RemoveRemoteAction(mRepo, mActivity));
        mActions.add(new RawConfigAction(mRepo, mActivity));
        mActions.add(new ConfigAction(mRepo, mActivity));
    }

    public void executeAction(int key) {
        RepoAction action = mActions.get(key);
        if (action == null) return;
        action.execute();
    }

    public void checkoutCommit(final String commitName) {
        CheckoutTask checkoutTask = new CheckoutTask(mRepo, commitName, null,
            isSuccess -> mActivity.reset(commitName));
        checkoutTask.executeTask();
    }

    public void checkoutCommit(final String commitName, final String branch) {
        CheckoutTask checkoutTask = new CheckoutTask(mRepo, commitName, branch,
            isSuccess -> mActivity.reset(branch));
        checkoutTask.executeTask();
    }

    public void mergeBranch(final Ref commit, final String ffModeStr, final boolean autoCommit) {
        MergeTask mergeTask = new MergeTask(mRepo, commit, ffModeStr, autoCommit,
            isSuccess -> mActivity.reset());
        mergeTask.executeTask();
    }

    public void addToStage(String filepath) {
        String relative = getRelativePath(filepath);
        AddToStageTask addToStageTask = new AddToStageTask(mRepo, relative, null);
        addToStageTask.executeTask();
    }

    public void checkoutFile(String filepath) {
        String relative = getRelativePath(filepath);
        CheckoutFileTask task = new CheckoutFileTask(mRepo, relative, null);
        task.executeTask();
    }

    public void deleteFileFromRepo(String filepath,
                                   DeleteFileFromRepoTask.DeleteOperationType deleteOperationType) {
        String relative = getRelativePath(filepath);
        DeleteFileFromRepoTask task = new DeleteFileFromRepoTask(mRepo, relative,
            deleteOperationType, isSuccess -> {
            // TODO Auto-generated method stub
            mActivity.getFilesFragment().reset();
        });
        task.executeTask();
    }

    private String getRelativePath(String filepath) {
        File base = mRepo.getDir();
        return FsUtils.getRelativePath(new File(filepath), base);
    }


    public void updateIndex(final String mFilePath, final int newMode) {
        String relative = getRelativePath(mFilePath);
        UpdateIndexTask task = new UpdateIndexTask(mRepo, relative, newMode);
        task.executeTask();
    }
}
