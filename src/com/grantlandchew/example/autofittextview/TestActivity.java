package com.grantlandchew.example.autofittextview;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TestActivity extends Activity {

    private EditText input;
    private Button button;
    private TextView output, output_autofit;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        input = (EditText)findViewById(R.id.input);
        button = (Button)findViewById(R.id.button);
        output = (TextView)findViewById(R.id.output);
        output_autofit = (TextView)findViewById(R.id.output_autofit);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                output.setText(input.getText());
                output_autofit.setText(input.getText());
            }
        });
    }
}