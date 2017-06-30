package com.telenor.possumexample.dialogs;

import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.telenor.possumexample.MainActivity;
import com.telenor.possumexample.R;

public class DefineIdDialog extends AppCompatDialogFragment {
    private EditText uniqueId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        return inflater.inflate(R.layout.dialog_define_id, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        uniqueId = (EditText)view.findViewById(R.id.uniqueId);
        uniqueId.setText(((MainActivity) getActivity()).myId());
        uniqueId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePreferences();
            }
        });

    }

    private void updatePreferences() {
        String suggestedId = uniqueId.getText().toString();
        ((MainActivity)getActivity()).preferences().edit().putString("storedId", suggestedId).apply();
    }
}