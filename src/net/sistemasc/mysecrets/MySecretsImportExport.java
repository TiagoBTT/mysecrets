package net.sistemasc.mysecrets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import android.database.Cursor;

public class MySecretsImportExport extends Activity {

  private static final int DIALOG_SAVE = 0;
  private static final int DIALOG_OPEN = 1;
  private Button           exportDbToSdButton;
  private Button           importDbFromSdButton;
  private TextView         tvImportExportHelp;
  private char             separator;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mysecrets_import_export);
    tvImportExportHelp = (TextView) findViewById(R.id.tvImportExportHelp);
    tvImportExportHelp.setText(Html
        .fromHtml(getString(R.string.import_export_help)));

    separator = prepareSeparator(MySecretsApplication.getSharedPreferences()
        .getString(MySecretsPreferences.PREF_IMPEXP_SEPARATOR, ";"));

    exportDbToSdButton = (Button) findViewById(R.id.exportdbtosdbutton);
    exportDbToSdButton.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        doExportCSV();
      }
    });

    importDbFromSdButton = (Button) findViewById(R.id.importdbfromsdbutton);
    importDbFromSdButton.setOnClickListener(new OnClickListener() {
      public void onClick(final View v) {
        doImportCSV();
      }
    });
  }

  private char prepareSeparator(String _sep) {
    if (_sep.length() == 1) {
      return _sep.charAt(0);
    } else {
      return '\t';
    }
  }

  private void doExportCSV() {
    Log.i(MySecretsApplication.LOG_TAG,
        "exporting database to external storage CSV format, asking path to user");
    Intent intent = new Intent(this, FileDialog.class);
    intent.putExtra(FileDialog.START_PATH, "/sdcard");
    startActivityForResult(intent, DIALOG_SAVE);
  }

  private void doImportCSV() {
    Log.i(MySecretsApplication.LOG_TAG,
        "importing database from external storage CSV format, asking path to user");
    Intent intent = new Intent(this, FileDialog.class);
    intent.putExtra(FileDialog.START_PATH, "/sdcard");
    intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);
    startActivityForResult(intent, DIALOG_OPEN);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
    case DIALOG_SAVE: {
      if (resultCode == RESULT_OK) {
        String fileName = data.getStringExtra(FileDialog.RESULT_PATH);
        Log.i(MySecretsApplication.LOG_TAG,
            "exporting database to external storage: " + fileName);
        ExportDatabaseTask export = new ExportDatabaseTask();
        export.setFileName(fileName);
        export.execute();
      }
      break;
    }
    case DIALOG_OPEN: {
      if (resultCode == RESULT_OK) {
        final String fileName = data.getStringExtra(FileDialog.RESULT_PATH);
        Log.i(MySecretsApplication.LOG_TAG,
            "importing database from external storage: " + fileName);
        new AlertDialog.Builder(this)
            .setTitle(
                String.format("Importing secrets from file: %s?", fileName))
            .setMessage(
                "WARNING! Do you want to erase all existing secrets before importing?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface arg0, int arg1) {
                doImport(fileName, true);
              }
            }).setNeutralButton("No", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface arg0, int arg1) {
                doImport(fileName, false);
              }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface arg0, int arg1) {
              }
            }).show();

      }
      break;
    }
    } // switch
  }

  private void doImport(String _fileName, boolean _clearBeforeImporting) {
    Log.i(MySecretsApplication.LOG_TAG, String.format(
        "importing database from external storage %s %s", _fileName,
        (_clearBeforeImporting) ? ". Clear before importing" : ""));
    ImportDatabaseTask importDatabaseTask = new ImportDatabaseTask();
    importDatabaseTask.setClearBeforeImporting(_clearBeforeImporting);
    importDatabaseTask.setFileName(_fileName);
    importDatabaseTask.execute();
  }

  // private boolean isExternalStorageAvail() {
  // return
  // Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
  // }

  private class ExportDatabaseTask extends AsyncTask<Void, Void, Boolean> {
    private final ProgressDialog dialog    = new ProgressDialog(
                                               MySecretsImportExport.this);
    private MySecretsDbAdapter   dbAdapter = MySecretsApplication
                                               .getDbAdapter();
    private String               fileName;

    // can use UI thread here
    @Override
    protected void onPreExecute() {
      dialog.setMessage("Exporting database to " + fileName);
      dialog.show();
    }

    public void setFileName(String _fileName) {
      fileName = _fileName;
    }

    // automatically done on worker thread (separate from UI thread)
    @Override
    protected Boolean doInBackground(final Void... args) {

      CSVWriter writer = null;
      try {
        File exporFile = new File(fileName);
        writer = new CSVWriter(new FileWriter(exporFile), separator);
        String[] header = { MySecretsDbAdapter.FLD_NAME,
            MySecretsDbAdapter.FLD_URL, MySecretsDbAdapter.FLD_USER,
            MySecretsDbAdapter.FLD_PASSW, MySecretsDbAdapter.FLD_MEMO,
            MySecretsDbAdapter.FLD_CATEGORY };
        writer.writeNext(header);

        Cursor cursor = dbAdapter.getAllRows("", -1, MySecretsDbAdapter.KEY_ID);
        if (cursor.moveToFirst()) {
          do {
            MySecretsItem item = new MySecretsItem(-1, cursor.getString(cursor
                .getColumnIndex(MySecretsDbAdapter.FLD_NAME)),
                cursor.getString(cursor
                    .getColumnIndex(MySecretsDbAdapter.FLD_URL)),
                cursor.getString(cursor
                    .getColumnIndex(MySecretsDbAdapter.FLD_USER)), "",
                cursor.getString(cursor
                    .getColumnIndex(MySecretsDbAdapter.FLD_MEMO)),
                cursor.getInt(cursor
                    .getColumnIndex(MySecretsDbAdapter.FLD_CATEGORY)));
            item.setCryptPassw(cursor.getString(cursor
                .getColumnIndex(MySecretsDbAdapter.FLD_PASSW)));
            String[] fields = { item.getName(), item.getUrl(), item.getUser(), 
                item.getClearPassw(), item.getMemo(),
                Integer.toString(item.getCategory()) };
            writer.writeNext(fields);
          } while (cursor.moveToNext());
        }
        cursor.close();
        writer.close();
        return true;
      } catch (IOException e) {
        Log.e(MySecretsApplication.LOG_TAG, e.getMessage(), e);
        return false;
      }
    }

    // can use UI thread here
    @Override
    protected void onPostExecute(final Boolean success) {
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      if (success) {
        Toast.makeText(MySecretsImportExport.this, "Export successful!",
            Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(MySecretsImportExport.this, "Export failed",
            Toast.LENGTH_LONG).show();
      }
    }
  }

  private class ImportDatabaseTask extends AsyncTask<Void, Void, String> {
    private final ProgressDialog dialog    = new ProgressDialog(
                                               MySecretsImportExport.this);
    private boolean              clearBeforeImporting;
    private MySecretsDbAdapter   dbAdapter = MySecretsApplication
                                               .getDbAdapter();
    private String               fileName;

    @Override
    protected void onPreExecute() {
      dialog.setMessage("Importing database...");
      dialog.show();
    }

    public void setFileName(String _fileName) {
      fileName = _fileName;
    }

    // could pass the params used here in AsyncTask<String, Void, String> - but
    // not being re-used
    @Override
    protected String doInBackground(final Void... args) {
      File dbBackupFile = new File(fileName);
      if (!dbBackupFile.exists()) {
        return String.format("File \'%s\' does not exist, cannot import.",
            fileName);
      } else if (!dbBackupFile.canRead()) {
        return String
            .format(
                "Database backup file \'%s\' exists, but is not readable, cannot import.",
                fileName);
      }
      // ////////////////
      dbAdapter.beginTransaction();
      // ///////////////
      try {
        if (isClearBeforeImporting()) {
          dbAdapter.clearAllRows();
        }
        CSVReader reader = null;
        try {
          reader = new CSVReader(new FileReader(fileName), separator, '\"', 1);
        } catch (FileNotFoundException e) {
          Log.e(MySecretsApplication.LOG_TAG, e.getMessage(), e);
        }
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
          if (nextLine.length != 6) {
            throw new Exception("Lines must contain 6 fields");
          }
          if (nextLine.length == 6) {
            MySecretsItem item = new MySecretsItem(-1, nextLine[0],
                nextLine[1], nextLine[2], nextLine[3], nextLine[4],
                Integer.parseInt(nextLine[5]));
            dbAdapter.createItem(item);
          }
        } // while
        dbAdapter.endTransaction(true);
      } catch (Exception e) {
        dbAdapter.endTransaction(false);
        Log.e(MySecretsApplication.LOG_TAG, e.getMessage(), e);
        return e.getMessage();
      }
      return null;
    }

    @Override
    protected void onPostExecute(final String errMsg) {
      if (dialog.isShowing()) {
        dialog.dismiss();
      }
      if (errMsg == null) {
        Toast.makeText(MySecretsImportExport.this, "Import successful!",
            Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(MySecretsImportExport.this, "Import failed - " + errMsg,
            Toast.LENGTH_LONG).show();
      }
    }

    public void setClearBeforeImporting(boolean clearBeforeImporting) {
      this.clearBeforeImporting = clearBeforeImporting;
    }

    public boolean isClearBeforeImporting() {
      return clearBeforeImporting;
    }
  }
}