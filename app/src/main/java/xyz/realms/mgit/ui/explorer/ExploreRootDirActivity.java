package xyz.realms.mgit.ui.explorer;

import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import java.io.File;
import java.io.FileFilter;

import xyz.realms.mgit.R;
import xyz.realms.mgit.database.Repo;

public class ExploreRootDirActivity extends FileExplorerActivity {


    @Override
    protected File getRootFolder() {
        return Environment.getExternalStorageDirectory();
    }

    @Override
    protected FileFilter getExplorerFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                String filename = file.getName();
                return !filename.startsWith(".") && file.isDirectory();
            }
        };
    }

    @Override
    protected AdapterView.OnItemClickListener getOnListItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                File file = mFilesListAdapter.getItem(position);
                if (file.isDirectory()) {
                    setCurrentDir(file);
                }
            }
        };
    }

    @Override
    protected AdapterView.OnItemLongClickListener getOnListItemLongClickListener() {
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_root, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_select_root) {
            Repo.setLocalRepoRoot(this, getCurrentDir());
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
