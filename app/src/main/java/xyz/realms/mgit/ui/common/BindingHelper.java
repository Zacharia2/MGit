package xyz.realms.mgit.ui.common;

import androidx.databinding.BindingAdapter;

import com.google.android.material.textfield.TextInputLayout;


public class BindingHelper {
    @BindingAdapter("errorText")
    public static void setErrorMessage(TextInputLayout view, String errorMessage) {
        if (errorMessage != null) {
            view.setError(errorMessage);
        }
    }
}

