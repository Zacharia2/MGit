package xyz.realms.mgit.actions;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import xyz.realms.android.utils.Profile;
import xyz.realms.mgit.R;
import xyz.realms.mgit.database.Repo;
import xyz.realms.mgit.tasks.repo.RebaseTask;
import xyz.realms.mgit.ui.RepoDetailActivity;
import xyz.realms.mgit.ui.fragments.SheimiDialogFragment;

public class RebaseAction extends RepoAction {

    public RebaseAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    private static void rebase(Repo repo, String branch, final RepoDetailActivity activity) {
        RebaseTask rebaseTask = new RebaseTask(repo, branch,
            isSuccess -> activity.reset());
        rebaseTask.executeTask();
    }

    @Override
    public void execute() {
        RebaseDialog rd = new RebaseDialog();
        rd.setArguments(mRepo.getBundle());
        rd.show(mActivity.getSupportFragmentManager(), "rebase-dialog");
        mActivity.closeOperationDrawer();
    }

    public static class RebaseDialog extends SheimiDialogFragment {

        private Repo mRepo;
        private RepoDetailActivity mActivity;
        private ListView mBranchTagList;
        private BranchTagListAdapter mAdapter;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            Bundle args = getArguments();
            if (args != null && args.containsKey(Repo.TAG)) {
                mRepo = (Repo) args.getSerializable(Repo.TAG);
            }

            mActivity = (RepoDetailActivity) getActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

            mBranchTagList = new ListView(mActivity);

            mAdapter = new BranchTagListAdapter(mActivity);
            mBranchTagList.setAdapter(mAdapter);
            builder.setView(mBranchTagList);

            String[] branches = mRepo.getBranches();
            String currentBranchName = mRepo.getBranchName();
            for (String branch : branches) {
                if (branch.equals(currentBranchName)) continue;
                mAdapter.add(branch);
            }

            builder.setTitle(R.string.dialog_rebase_title);
            mBranchTagList.setOnItemClickListener((adapterView, view, position, id) -> {
                String commit = mAdapter.getItem(position);
                rebase(mRepo, commit, mActivity);
                getDialog().cancel();
            });

            return builder.create();
        }

        private static class BranchTagListAdapter extends ArrayAdapter<String> {

            public BranchTagListAdapter(Context context) {
                super(context, 0);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                ListItemHolder holder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.listitem_dialog_choose_commit, parent
                        , false);
                    holder = new ListItemHolder();
                    holder.commitTitle = convertView.findViewById(R.id.commitTitle);
                    holder.commitIcon = convertView.findViewById(R.id.commitIcon);
                    convertView.setTag(holder);
                } else {
                    holder = (ListItemHolder) convertView.getTag();
                }
                String commitName = getItem(position);
                String displayName = Repo.getCommitDisplayName(commitName);
                int commitType = Repo.getCommitType(commitName);
                switch (commitType) {
                    case Repo.COMMIT_TYPE_HEAD:
                        holder.commitIcon.setImageResource(Profile.getStyledResource(getContext()
                            , R.attr.ic_branch_l));
                        break;
                    case Repo.COMMIT_TYPE_TAG:
                        holder.commitIcon.setImageResource(Profile.getStyledResource(getContext()
                            , R.attr.ic_tag_l));
                        break;
                }
                holder.commitTitle.setText(displayName);
                return convertView;
            }

        }

        private static class ListItemHolder {
            public TextView commitTitle;
            public ImageView commitIcon;
        }

    }
}
