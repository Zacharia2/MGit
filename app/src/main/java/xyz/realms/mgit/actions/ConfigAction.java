package xyz.realms.mgit.actions;

import android.app.AlertDialog;
import android.view.LayoutInflater;

import androidx.databinding.DataBindingUtil;

import timber.log.Timber;
import xyz.realms.mgit.R;
import xyz.realms.mgit.database.GitConfig;
import xyz.realms.mgit.database.Repo;
import xyz.realms.mgit.databinding.DialogRepoConfigBinding;
import xyz.realms.mgit.errors.StopTaskException;
import xyz.realms.mgit.ui.explorer.RepoDetailActivity;

/**
 * Action to display configuration for a Repo
 */
public class ConfigAction extends RepoAction {


    public ConfigAction(Repo repo, RepoDetailActivity activity) {
        super(repo, activity);
    }

    @Override
    public void execute() {

        try {
            DialogRepoConfigBinding binding =
                DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.dialog_repo_config, null, false);
            GitConfig gitConfig = new GitConfig(mRepo);
            binding.setViewModel(gitConfig);

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setView(binding.getRoot()).setNeutralButton(R.string.label_done, null).create().show();

        } catch (StopTaskException e) {
            //FIXME: show error to user
            Timber.e(e);
        }
    }

}
