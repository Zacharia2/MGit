package com.xinglan.mgit.database.models;

import com.xinglan.mgit.common.exceptions.StopTaskException;

import org.eclipse.jgit.lib.StoredConfig;

import java.io.IOException;

import timber.log.Timber;

/**
 * Model for Git configuration
 */

public class GitConfig {


    private final StoredConfig mConfig;

    private final String USER_SECTION = "user";
    private final String NAME_SUBSECTION = "name";
    private final String EMAIL_SUBSECTION = "email";


    /**
     * Create a Git Config for a specific repo
     *
     * @param repo
     */
    public GitConfig(Repo repo) throws StopTaskException {
        mConfig = repo.getStoredConfig();
    }

    public String getUserName() {
        return getSubsection(NAME_SUBSECTION);
    }

    public void setUserName(String name) {
        setSubsection(NAME_SUBSECTION, name);
    }

    public String getUserEmail() {
        return getSubsection(EMAIL_SUBSECTION);
    }

    public void setUserEmail(String email) {
        setSubsection(EMAIL_SUBSECTION, email);
    }

    private void setSubsection(String subsection, String value) {
        if (value == null || value.equals("")) {
            mConfig.unset(USER_SECTION, null, subsection);
        } else {
            mConfig.setString(USER_SECTION, null, subsection, value);
        }
        try {
            mConfig.save();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    private String getSubsection(String subsection) {
        return mConfig.getString(USER_SECTION, null, subsection);
    }
}
