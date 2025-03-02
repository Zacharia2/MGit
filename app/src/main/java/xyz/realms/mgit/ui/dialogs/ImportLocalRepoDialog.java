package xyz.realms.mgit.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import java.io.File;

import xyz.realms.mgit.MGitApplication;
import xyz.realms.mgit.R;
import xyz.realms.mgit.database.Repo;
import xyz.realms.mgit.database.RepoContract;
import xyz.realms.mgit.ui.fragments.SheimiDialogFragment;
import xyz.realms.mgit.ui.preference.PreferenceHelper;
import xyz.realms.mgit.ui.utils.FsUtils;

/**
 * Created by sheimi on 8/24/13.
 */

public class ImportLocalRepoDialog extends SheimiDialogFragment implements
    View.OnClickListener {

    public static final String FROM_PATH = "from path";
    private File mFile;
    private String mFromPath;
    private Activity mActivity;
    private EditText mLocalPath;
    private CheckBox mImportAsExternal;
    private PreferenceHelper mPrefsHelper;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mActivity = getActivity();

        mPrefsHelper = ((MGitApplication) mActivity.getApplicationContext()).getPrefenceHelper();

        Bundle args = getArguments();
        if (args != null && args.containsKey(FROM_PATH)) {
            mFromPath = args.getString(FROM_PATH);
        }
        mFile = new File(mFromPath);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.dialog_import_set_local_repo_title));
        View view = getActivity().getLayoutInflater().inflate(
            R.layout.dialog_import_repo, null);

        builder.setView(view);
        mLocalPath = view.findViewById(R.id.localPath);
        mLocalPath.setText(mFile.getName());
        mImportAsExternal = view.findViewById(R.id.importAsExternal);
        mImportAsExternal
            .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    if (isChecked) {
                        mLocalPath.setText(Repo.EXTERNAL_PREFIX
                            + mFile.getAbsolutePath());
                    } else {
                        mLocalPath.setText(mFile.getName());
                    }
                    mLocalPath.setEnabled(isChecked);
                }
            });

        // set button listener
        builder.setNegativeButton(R.string.label_cancel,
            new DummyDialogListener());
        builder.setPositiveButton(R.string.label_import,
            new DummyDialogListener());

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;
        Button positiveButton = dialog
            .getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        final String localPath = mLocalPath.getText().toString().trim();
        if (!mImportAsExternal.isChecked()) {
            if (localPath.equals("")) {
                showToastMessage(R.string.alert_field_not_empty);
                mLocalPath.setError(getString(R.string.alert_field_not_empty));
                return;
            }

            if (localPath.contains("/")) {
                showToastMessage(R.string.alert_localpath_format);
                mLocalPath.setError(getString(R.string.alert_localpath_format));
                return;
            }

            File file = Repo.getDir(mPrefsHelper, localPath);
            if (file.exists()) {
                showToastMessage(R.string.alert_file_exists);
                mLocalPath.setError(getString(R.string.alert_file_exists));
                return;
            }
        }

        final Repo repo = Repo.importRepo(localPath, getString(R.string.importing));

        if (mImportAsExternal.isChecked()) {
            updateRepoInformation(repo);
            dismiss();
            return;
        }
        final File repoFile = Repo.getDir(mPrefsHelper, localPath);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FsUtils.copyDirectory(mFile, repoFile);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateRepoInformation(repo);
                    }
                });
            }
        });
        thread.start();
        dismiss();
    }

    private void updateRepoInformation(Repo repo) {
        repo.updateLatestCommitInfo();
        repo.updateRemote();
        repo.updateStatus(RepoContract.REPO_STATUS_NULL);
    }
}
