package com.xinglan.mgit.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xinglan.mgit.ui.SheimiFragmentActivity.OnBackClickListener;
import com.xinglan.mgit.R;
import com.xinglan.mgit.ui.CommitDiffActivity;
import com.xinglan.mgit.database.models.Repo;
import com.xinglan.mgit.tasks.repo.StatusTask;

/**
 * Created by sheimi on 8/5/13.
 */
public class StatusFragment extends RepoDetailFragment {

    private Repo mRepo;
    private ProgressBar mLoadding;
    private TextView mStatus;
    private Button mUnstagedDiff;
    private Button mStagedDiff;

    public static StatusFragment newInstance(Repo mRepo) {
        StatusFragment fragment = new StatusFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Repo.TAG, mRepo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_status, container, false);
        getRawActivity().setStatusFragment(this);

        Bundle bundle = getArguments();
        mRepo = (Repo) bundle.getSerializable(Repo.TAG);
        if (mRepo == null && savedInstanceState != null) {
            mRepo = (Repo) savedInstanceState.getSerializable(Repo.TAG);
        }
        if (mRepo == null) {
            return v;
        }
        mLoadding = v.findViewById(R.id.loading);
        mStatus = v.findViewById(R.id.status);
        mStagedDiff = v.findViewById(R.id.button_staged_diff);
        mUnstagedDiff = v.findViewById(R.id.button_unstaged_diff);
        mStagedDiff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiff("HEAD", "dircache");
            }
        });
        mUnstagedDiff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDiff("dircache", "filetree");
            }
        });
        reset();
        return v;
    }

    private void showDiff(String oldCommit, String newCommit) {
        Intent intent = new Intent(getRawActivity(),
            CommitDiffActivity.class);
        intent.putExtra(CommitDiffActivity.OLD_COMMIT, oldCommit);
        intent.putExtra(CommitDiffActivity.NEW_COMMIT, newCommit);
        intent.putExtra(CommitDiffActivity.SHOW_DESCRIPTION, false);
        intent.putExtra(Repo.TAG, mRepo);
        getRawActivity().startActivity(intent);
    }

    @Override
    public void reset() {
        if (mLoadding == null || mStatus == null)
            return;
        mLoadding.setVisibility(View.VISIBLE);
        mStatus.setVisibility(View.GONE);
        StatusTask task = new StatusTask(mRepo, new StatusTask.GetStatusCallback() {
            @Override
            public void postStatus(String result) {
                mStatus.setText(result);
                mLoadding.setVisibility(View.GONE);
                mStatus.setVisibility(View.VISIBLE);
            }
        });
        task.executeTask();
    }

    @Override
    public OnBackClickListener getOnBackClickListener() {
        return null;
    }
}
