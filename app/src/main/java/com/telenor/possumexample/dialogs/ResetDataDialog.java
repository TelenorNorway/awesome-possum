package com.telenor.possumexample.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.telenor.possumexample.R;


public class ResetDataDialog extends Dialog{
    private String uniqueUserId;
    private String resetUrl;
    private String apiKey;
    public ResetDataDialog(@NonNull Context context, String uniqueUserId, String resetUrl, String apiKey) {
        super(context);
        this.uniqueUserId = uniqueUserId;
        this.resetUrl = resetUrl;
        this.apiKey = apiKey;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.dialog_reset_data);
    }
}