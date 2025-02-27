package xyz.realms.android.permissions;

import android.content.Context;
import android.os.Build.VERSION;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import kotlin.jvm.internal.Intrinsics;

public final class PermissionsHelper {
    public static final Companion Companion = new Companion();

    public static final class Companion {
        private Companion() {
        }

        public boolean isExternalStorageManager() {
            return VERSION.SDK_INT >= 30 && Environment.isExternalStorageManager();
        }

        public boolean canReadStorage(Context context) {
            Intrinsics.checkNotNullParameter(context, "context");
            return VERSION.SDK_INT <= 23 || ContextCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE") == 0 || this.isExternalStorageManager();
        }
    }
}
