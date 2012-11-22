package net.sistemasc.mysecrets;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class MySecretsEditActivity extends Activity {
  // views
  private Spinner             spCategory;
  private EditText            etName;
  private EditText            etUrl;
  private EditText            etUser;
  private EditText            etPassw;
  private EditText            etMemo;
  // fields
  private Intent              intent;
  private Uri                 data;
  // static edit data helpers
  public static MySecretsItem staticItem;
  public static int           action;
  // const
  public final static int     EDITING   = 0;
  public final static int     INSERTING = 1;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    // bind visual to code
    setContentView(R.layout.mysecrets_edit_activity);
    intent = getIntent();
    data = intent.getData();
    // // manera de passar valors primitius
    // editing = intent.getBooleanExtra("edit", false);

    doGetLayoutViews();
    doPrepareClickables();

    // fill editing fields
    if (action == EDITING) {
      spCategory.setSelection(staticItem.getCategory());
      etName.setText(staticItem.getName());
      etUrl.setText(staticItem.getUrl());
      etUser.setText(staticItem.getUser());
      etPassw.setText(staticItem.getClearPassw());
      etMemo.setText(staticItem.getMemo());
    } else {
      staticItem = new MySecretsItem();
    }
  }

  /**
   * Initialize buttons and other clickable controls
   */
  private void doPrepareClickables() {
    Button buGoUrl = (Button) findViewById(R.id.buGoUrl);
    buGoUrl.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Uri uri = Uri.parse(etUrl.getText().toString());
        if (!uri.isAbsolute()) {
          uri = Uri.parse("http://" + etUrl.getText().toString());
        }
        if (uri.isAbsolute()) {
          Intent i = new Intent(Intent.ACTION_VIEW);
          i.setData(uri);
          startActivity(i);
        }
      }
    });

    Button buOk = (Button) findViewById(R.id.buOk);
    buOk.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        staticItem.setCategory((int) spCategory.getSelectedItemId());
        staticItem.setName(etName.getText().toString());
        staticItem.setUrl(etUrl.getText().toString());
        staticItem.setUser(etUser.getText().toString());
        staticItem.setClearPassw(etPassw.getText().toString());
        staticItem.setMemo(etMemo.getText().toString());

        Intent result = new Intent(null, data);
        setResult(RESULT_OK, result);
        finish();
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

    ToggleButton tbShowPassw = (ToggleButton) findViewById(R.id.tbShowPassw);
    tbShowPassw.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (((ToggleButton) v).isChecked()) {
          etPassw.setInputType(InputType.TYPE_CLASS_TEXT
              | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
          etPassw.setInputType(InputType.TYPE_CLASS_TEXT
              | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
      }
    });
  }

  /**
   * Inflate editing views for this activity
   */
  private void doGetLayoutViews() {
    spCategory = (Spinner) findViewById(R.id.spCategory);
    etName = (EditText) findViewById(R.id.etName);
    etUrl = (EditText) findViewById(R.id.etUrl);
    etUser = (EditText) findViewById(R.id.etUser);
    etPassw = (EditText) findViewById(R.id.etPassw);
    etMemo = (EditText) findViewById(R.id.etMemo);
  }

}
