package net.sistemasc.mysecrets;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MySecretsMainPassw extends Activity {

  private EditText etPassw;
  private Intent   intent;
  private Uri      data;
  private String   passw;
  private TextView tvRestoreKey;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    // bind visual to code
    setContentView(R.layout.mysecrets_main_passw);
    etPassw = (EditText) findViewById(R.id.etPassw);
    // tvRestoreKey.setText(MySecretsApplication.getAndroidId());
    tvRestoreKey = (TextView) findViewById(R.id.tvRestoreKey);
    tvRestoreKey.setText(Html.fromHtml(getString(R.string.forgot_password)));

    intent = getIntent();
    data = intent.getData();
    passw = intent.getStringExtra(MySecretsPreferences.PREF_MAIN_PASSWORD);

    Button buOk = (Button) findViewById(R.id.buOk);
    buOk.setOnClickListener(new View.OnClickListener() {

      private void doResultOk() {
        Intent result = new Intent(null, data);
        setResult(RESULT_OK, result);
        finish();
      }

      @Override
      public void onClick(View v) {
        String s = etPassw.getText().toString();
        if (s.compareTo(passw) != 0) {
          // let's try rescue key
          String ss = MySecretsApplication.getRescueKey();
          if (s.compareTo(ss) == 0) {
            doResultOk();
          }
        } else {
          doResultOk();
        }
      }
    });

    Button buCancel = (Button) findViewById(R.id.buCancel);
    buCancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent result = new Intent(null, data);
        setResult(RESULT_CANCELED, result);
        finish();
      }
    });
  }

}
