package com.xinglan.mgit.ui.repolist;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.core.view.MenuItemCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.xinglan.android.MGitApplication;
import com.xinglan.mgit.R;
import com.xinglan.mgit.common.OnActionClickListener;
import com.xinglan.mgit.database.RepoDbManager;
import com.xinglan.mgit.database.models.Repo;
import com.xinglan.mgit.databinding.ActivityMainBinding;
import com.xinglan.mgit.tasks.clone.CloneViewModel;
import com.xinglan.mgit.tasks.repo.CloneTask;
import com.xinglan.mgit.transport.MGitHttpConnectionFactory;
import com.xinglan.mgit.transport.ssh.PrivateKeyUtils;
import com.xinglan.mgit.ui.RepoDetailActivity;
import com.xinglan.mgit.ui.SheimiFragmentActivity;
import com.xinglan.mgit.ui.UserSettingsActivity;
import com.xinglan.mgit.ui.adapters.RepoListAdapter;
import com.xinglan.mgit.ui.dialogs.DummyDialogListener;
import com.xinglan.mgit.ui.dialogs.ImportLocalRepoDialog;
import com.xinglan.mgit.ui.explorer.ExploreFileActivity;
import com.xinglan.mgit.ui.explorer.ImportRepositoryActivity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import timber.log.Timber;

public class RepoListActivity extends SheimiFragmentActivity {

    private static final int REQUEST_IMPORT_REPO = 0;
    private Context mContext;
    private RepoListAdapter mRepoListAdapter;
    private ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RepoListViewModel viewModel = new ViewModelProvider(this).get(RepoListViewModel.class);
        CloneViewModel cloneViewModel = new ViewModelProvider(this).get(CloneViewModel.class);

        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        activityMainBinding.setLifecycleOwner(this);
        activityMainBinding.setCloneViewModel(cloneViewModel);
        activityMainBinding.setViewModel(viewModel);
        activityMainBinding.setClickHandler(new OnActionClickListener() {
            @Override
            public void onActionClick(String action) {
                if (ClickActions.CLONE.name().equals(action)) {
                    cloneRepo();
                } else {
                    hideCloneView();
                }
            }
        });

        PrivateKeyUtils.migratePrivateKeys();

        initUpdatedSSL();

        mRepoListAdapter = new RepoListAdapter(this);
        activityMainBinding.repoList.setAdapter(mRepoListAdapter);
        mRepoListAdapter.queryAllRepo();
        activityMainBinding.repoList.setOnItemClickListener(mRepoListAdapter);
        activityMainBinding.repoList.setOnItemLongClickListener(mRepoListAdapter);
        mContext = getApplicationContext();

        Uri uri = this.getIntent().getData();
        if (uri != null) {
            URL mRemoteRepoUrl = null;
            try {
                mRemoteRepoUrl = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
            } catch (MalformedURLException e) {
                Toast.makeText(mContext, R.string.invalid_url, Toast.LENGTH_LONG).show();
                Timber.e(e);
            }

            if (mRemoteRepoUrl != null) {
                String remoteUrl = mRemoteRepoUrl.toString();
                String repoName = remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1);
                StringBuilder repoUrlBuilder = new StringBuilder(remoteUrl);

                //need git extension to clone some repos
                if (!remoteUrl.toLowerCase().endsWith(getString(R.string.git_extension))) {
                    repoUrlBuilder.append(getString(R.string.git_extension));
                } else { //if has git extension remove it from repository name
                    repoName = repoName.substring(0, repoName.lastIndexOf('.'));
                }
                //Check if there are others repositories with same remote
                List<Repo> repositoriesWithSameRemote = Repo.getRepoList(mContext, RepoDbManager.searchRepo(remoteUrl));

                //if so, just open it
                if (repositoriesWithSameRemote.size() > 0) {
                    Toast.makeText(mContext, R.string.repository_already_present, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(mContext, RepoDetailActivity.class);
                    intent.putExtra(Repo.TAG, repositoriesWithSameRemote.get(0));
                    startActivity(intent);
                } else if (Repo.getDir(((MGitApplication) getApplicationContext()).getPrefenceHelper(), repoName).exists()) {
                    // Repository with name end already exists, see https://github.com/maks/MGit/issues/289
                    cloneViewModel.setRemoteUrl(repoUrlBuilder.toString());
                    showCloneView();
                } else {
                    final String cloningStatus = getString(R.string.cloning);
                    Repo mRepo = Repo.createRepo(repoName, repoUrlBuilder.toString(), cloningStatus);
                    Boolean isRecursive = true;
                    CloneTask task = new CloneTask(mRepo, true, cloningStatus, null);
                    task.executeTask();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check everytime on the repo list activity that we still have file access permission
        checkAndRequestRequiredPermissions(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        configSearchAction(searchItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.action_new) {

            showCloneView();
            return true;
        } else if (item.getItemId() == R.id.action_import_repo) {
            intent = new Intent(this, ImportRepositoryActivity.class);
            startActivityForResult(intent, REQUEST_IMPORT_REPO);
            forwardTransition();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            intent = new Intent(this, UserSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void configSearchAction(MenuItem searchItem) {
        SearchView searchView = (SearchView) searchItem.getActionView();
        if (searchView == null)
            return;
        SearchListener searchListener = new SearchListener();
        MenuItemCompat.setOnActionExpandListener(searchItem, searchListener);
        searchView.setIconifiedByDefault(true);
        searchView.setOnQueryTextListener(searchListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_IMPORT_REPO:
                final String path = data.getExtras().getString(
                    ExploreFileActivity.RESULT_PATH);
                File file = new File(path);
                File dotGit = new File(file, Repo.DOT_GIT_DIR);
                if (!dotGit.exists()) {
                    showToastMessage(getString(R.string.error_no_repository));
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(
                    this);
                builder.setTitle(R.string.dialog_comfirm_import_repo_title);
                builder.setMessage(R.string.dialog_comfirm_import_repo_msg);
                builder.setNegativeButton(R.string.label_cancel,
                    new DummyDialogListener());
                builder.setPositiveButton(R.string.label_import,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                            DialogInterface dialogInterface, int i) {
                            Bundle args = new Bundle();
                            args.putString(ImportLocalRepoDialog.FROM_PATH, path);
                            ImportLocalRepoDialog rld = new ImportLocalRepoDialog();
                            rld.setArguments(args);
                            rld.show(getSupportFragmentManager(), "import-local-dialog");
                        }
                    });
                builder.show();
                break;
        }
    }

    public void finish() {
        rawfinish();
    }

    private void initUpdatedSSL() {
        MGitHttpConnectionFactory.install();
        Timber.i("Installed custom HTTPS factory");
    }

    private void cloneRepo() {
        if (activityMainBinding.getCloneViewModel().validate()) {
            hideCloneView();
            activityMainBinding.getCloneViewModel().cloneRepo();
        }
    }

    private void showCloneView() {
        activityMainBinding.getCloneViewModel().show(true);
    }

    private void hideCloneView() {
        activityMainBinding.getCloneViewModel().show(false);
        // hideKeyboard
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public enum ClickActions {
        CLONE, CANCEL
    }

    public class SearchListener implements SearchView.OnQueryTextListener,
        MenuItemCompat.OnActionExpandListener {

        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            mRepoListAdapter.searchRepo(s);
            return false;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            mRepoListAdapter.queryAllRepo();
            return true;
        }

    }
}
