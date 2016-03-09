package me.grantland.autofittextview.sample;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

public class SampleActivity extends Activity {

    private TextView mOutput, mAutofitOutput,mAutofitOutput2;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mOutput = (TextView)findViewById(R.id.output);
        mAutofitOutput = (TextView)findViewById(R.id.output_autofit);
        mAutofitOutput2 = (TextView)findViewById(R.id.output_autofit2);


        ((EditText)findViewById(R.id.input)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mOutput.setText(charSequence);
                mAutofitOutput.setText(charSequence);
                mAutofitOutput2.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // do nothing
            }
        });
    }
}