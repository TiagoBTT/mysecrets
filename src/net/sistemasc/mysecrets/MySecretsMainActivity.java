package net.sistemasc.mysecrets;

// TODO strings to resource
// TODO localize to ES
// TODO create project main web page and update about activity literals

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class MySecretsMainActivity extends Activity {

  // Visual controls
  private ListView                 myListView;
  private EditText                 etFilterText;
  private Button                   buFilter;
  private Button                   buClearFilter;
  private Spinner                  spShowCategory;
  private TextView                 tvRecCount;
  // misc
  private int                      filterCategory      = 0;
  private MySecretsDbAdapter       dbAdapter;
  // dialogs
  static final private int         DIALOG_ABOUT        = 1;
  static final private int         DIALOG_PREFERENCES  = 2;
  // static final private int DIALOG_CONFIRM_DELETE = 2;
  // other app's activities
  static final private int         ACTIVITY_EDIT       = 3;
  static final private int         ACTIVITY_MANANGE_DB = 4;
  static final private int         ACTIVITY_MAIN_PASSWORD = 5;
  // menu related
  static final private int         MENU_ADD_ITEM       = Menu.FIRST;
  static final private int         MENU_EDIT_ITEM      = Menu.FIRST + 1;
  static final private int         MENU_DELETE_ITEM    = Menu.FIRST + 2;
  private static final int         MENU_PREFERENCES    = Menu.FIRST + 3;
  private static final int         MENU_MANAGE_DB      = Menu.FIRST + 4;
  private static final int         MENU_ABOUT          = Menu.FIRST + 5;
  // Save/Restore state
  private static final String      SAVE_ITEM_IDX       = "SAVE_ITEM_IDX";
  private static final String      SAVE_FILTER_TEXT    = "SAVE_FILTER_TEXT";
  private static final String      SAVE_CATEGORY       = "SAVE_CATEGORY";
  private int                      mSelectedItemIdx;
  private MySecretsItem            mSelectedItem;

  private boolean                  sortByName          = true;
  private Cursor                   cursor;
  private MySecretsArrayAdapter    arrayAdapter;
  private ArrayList<MySecretsItem> itemsArrayList;

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    // bind visual to code
    setContentView(R.layout.mysecrets_main);
    // preparing static helper Application class
    MySecretsApplication.setContext(this.getApplicationContext());
    // load preferences
    updateFromPreferences();
    
    if (askForMainPassword()) {
      Intent intent = new Intent(this, MySecretsMainPassw.class);
      // use Item.staticItem to pass to edit activity current item instance
      intent.putExtra(MySecretsPreferences.PREF_MAIN_PASSWORD,MySecretsApplication.getSharedPreferences().getString(
          MySecretsPreferences.PREF_MAIN_PASSWORD, ""));
      startActivityForResult(intent, ACTIVITY_MAIN_PASSWORD);
    }
    
    dbAdapter = MySecretsApplication.getDbAdapter();
    // sortByName = mSharedPreferences.getBoolean(arg0, arg1)
    etFilterText = (EditText) findViewById(R.id.etFilterText);
    buFilter = (Button) findViewById(R.id.buFilter);
    buFilter.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etFilterText.getWindowToken(), 0);
        doFilter();
      }
    });

    buClearFilter = (Button) findViewById(R.id.buClearFilter);
    buClearFilter.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        etFilterText.setText(null);
        populateArrayList(-1);
      }
    });

    spShowCategory = (Spinner) findViewById(R.id.spShowCategory);
    spShowCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int _position,
          long _id) {
        filterCategory = _position;
        doFilter();
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
        filterCategory = 0;
        doFilter();
      }
    });

    myListView = (ListView) findViewById(R.id.myListView);
    myListView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> _av, View _v, int _index, long arg3) {
        mSelectedItem = itemsArrayList.get(_index);
        doEdit(mSelectedItem, MySecretsEditActivity.EDITING);
      }
    });

    itemsArrayList = new ArrayList<MySecretsItem>();
    arrayAdapter = new MySecretsArrayAdapter(this,
        R.layout.mysecrets_list_item, // binds layout for viewing every item in
                                      // ListView
        itemsArrayList);
    myListView.setAdapter(arrayAdapter);
    registerForContextMenu(myListView);

    tvRecCount = (TextView) findViewById(R.id.tvRecCount);

    // open database
    dbAdapter.open();
    // copy database data to stored ArrayList
    populateArrayList(-1);
  }

  @Override
  protected void onResume() {
    super.onResume();
    populateArrayList(-1);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    // Create the menu item and keep a reference to it.
    MenuItem menuAddItem = menu.add(0, MENU_ADD_ITEM, 0, R.string.add);
    menuAddItem.setIcon(android.R.drawable.ic_menu_add);

    MenuItem menuPrefer = menu.add(1, MENU_PREFERENCES, 0, R.string.preferences);
    menuPrefer.setIcon(android.R.drawable.ic_menu_preferences);

    MenuItem menuManageDB = menu.add(0, MENU_MANAGE_DB, 0, R.string.import_export);
    menuManageDB.setIcon(android.R.drawable.ic_menu_manage);

    MenuItem menuAbout = menu.add(1, MENU_ABOUT, 1, R.string.about_menu);
    menuAbout.setIcon(android.R.drawable.ic_menu_info_details);
    return true;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);

    menu.setHeaderTitle(R.string.action_with_selected_item);
    menu.add(0, MENU_DELETE_ITEM, Menu.NONE, R.string.delete);
    menu.add(0, MENU_EDIT_ITEM, Menu.NONE, R.string.edit);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    super.onContextItemSelected(item);
    switch (item.getItemId()) {
    case (MENU_DELETE_ITEM): {
      AdapterView.AdapterContextMenuInfo menuInfo;
      menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
      int idx = menuInfo.position;
      mSelectedItem = itemsArrayList.get(idx);
      doDelete(mSelectedItem);
      return true;
    }
    case (MENU_EDIT_ITEM): {
      AdapterView.AdapterContextMenuInfo menuInfo;
      menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
      int idx = menuInfo.position;
      mSelectedItem = itemsArrayList.get(idx);
      doEdit(mSelectedItem, MySecretsEditActivity.EDITING);
      return true;
    }
    } // switch
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    // Find which menu item has been selected
    switch (item.getItemId()) {
    // Check for each known menu item
    case (MENU_ADD_ITEM): {
      mSelectedItem = null;
      doEdit(mSelectedItem, MySecretsEditActivity.INSERTING);
      return true;
    }
    case MENU_PREFERENCES: {
      Intent intent = new Intent(this, MySecretsPreferences.class);
      startActivityForResult(intent, DIALOG_PREFERENCES);
      return true;
    }
    case MENU_MANAGE_DB: {
      Intent intent = new Intent(this, MySecretsImportExport.class);
      startActivity(intent);
      return true;
    }
    case (MENU_ABOUT): {
      showDialog(DIALOG_ABOUT);
      return true;
    }
    } // switch
    // Return false if you have not handled the menu item.
    return false;
  }

  /**
   * This method is called for menu actions first time (fromDialog = false),
   * then starts a confirmation dialog. Positive button on this dialog fires
   * this method again, fromDialog = true, and completes deletion job.
   * 
   * @param _item
   */
  private void doDelete(MySecretsItem _item) {
    new AlertDialog.Builder(MySecretsMainActivity.this)
        .setMessage(R.string.confirm_deletion_).setTitle(R.string.warning)
        .setIcon(android.R.drawable.ic_menu_delete)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface arg0, int arg1) {
            dbAdapter.deleteItem(mSelectedItem);
            // try to select neighbor item
            int idx = itemsArrayList.indexOf(mSelectedItem) - 1;
            if (idx < 0) {
              idx = idx + 2; // deleted was first, try next
            }
            long id = -1;
            if (idx < itemsArrayList.size()) {
              id = itemsArrayList.get(idx).getId();
            }
            populateArrayList(id);
          }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface arg0, int arg1) {
          }
        }).show();
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    // restore selected item in ListView
    mSelectedItemIdx = -1;
    if (savedInstanceState != null) {
      mSelectedItemIdx = savedInstanceState.getInt(SAVE_ITEM_IDX, -1);
      spShowCategory.setSelection(savedInstanceState.getInt(SAVE_CATEGORY, 0));
      etFilterText.setText(savedInstanceState.getString(SAVE_FILTER_TEXT));
    }
    myListView.setSelection(mSelectedItemIdx);
    if (mSelectedItemIdx >= 0) {
      mSelectedItem = itemsArrayList.get(mSelectedItemIdx);
    }
    super.onRestoreInstanceState(savedInstanceState);
    doFilter();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    int idx = itemsArrayList.indexOf(mSelectedItem);
    outState.putInt(SAVE_ITEM_IDX, idx);
    outState.putInt(SAVE_CATEGORY, filterCategory);
    outState.putString(SAVE_FILTER_TEXT, etFilterText.getText().toString());
    super.onSaveInstanceState(outState);
  }

  /**
   * Incremental search using filter text
   */
  private void doFilter() {
    populateArrayList(-1);
    if (itemsArrayList.size() == 0) {
      Context context = getApplicationContext();
      CharSequence text = getString(R.string.error_nothing_found);
      int duration = Toast.LENGTH_SHORT;

      Toast toast = Toast.makeText(context, text, duration);
      toast.show();
    }
  }

  private void doEdit(MySecretsItem _item, int _action) {
    Intent intent = new Intent(this, MySecretsEditActivity.class);
    // use Item.staticItem to pass to edit activity current item instance
    MySecretsEditActivity.staticItem = _item;
    MySecretsEditActivity.action = _action;
    startActivityForResult(intent, ACTIVITY_EDIT);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
    case ACTIVITY_EDIT: {
      if (resultCode == Activity.RESULT_OK) {
        if (MySecretsEditActivity.action == MySecretsEditActivity.EDITING) {
          // use MySecretsEditActivity.staticItem to pass to edit activity
          // current item
          // instance
          dbAdapter.updateItem(MySecretsEditActivity.staticItem);
          // select last edited or inserted item
          populateArrayList(MySecretsEditActivity.staticItem.getId());
        } else {
          long id = dbAdapter.createItem(MySecretsEditActivity.staticItem);
          populateArrayList(id);
        }
      }
      break;
    }
    case DIALOG_PREFERENCES: {
      // not ok/cancel result, just know we are returning from preferences form
      updateFromPreferences();
      populateArrayList(-1);
      break;
    }
    case ACTIVITY_MANANGE_DB: {
      populateArrayList(-1);
      break;
    }
    case ACTIVITY_MAIN_PASSWORD: {
      if (resultCode != Activity.RESULT_OK) {
        finish();
      }
      break;
    }
    } // switch
  }

  /**
   * Querys database all rows
   * 
   * @param _itemId
   *          optional. if > -1 sets selection on listview upon this idx
   */
  private void populateArrayList(long _itemId) {
    String filter = etFilterText.getText().toString();
    int category = (filterCategory > 0) ? filterCategory - 1 : -1;
    cursor = dbAdapter.getAllRows(filter, category,
        (sortByName) ? MySecretsDbAdapter.FLD_NAME : MySecretsDbAdapter.KEY_ID);
    try {
      updateArrayList(_itemId);
    } finally {
      cursor.close();
    }
  }

  /**
   * Refills array
   * 
   * @param _itemId
   *          optional. if > -1 sets selection on listview upon this idx
   */
  private void updateArrayList(long _itemId) {
    int idx = -1;
    cursor.requery();
    itemsArrayList.clear();
    if (cursor.moveToFirst()) {
      do {
        // declare fields to store every column in query
        int id = cursor
            .getInt(cursor.getColumnIndex(MySecretsDbAdapter.KEY_ID));
        String name = cursor.getString(cursor
            .getColumnIndex(MySecretsDbAdapter.FLD_NAME));
        String url = cursor.getString(cursor
            .getColumnIndex(MySecretsDbAdapter.FLD_URL));
        String user = cursor.getString(cursor
            .getColumnIndex(MySecretsDbAdapter.FLD_USER));
        String cryptPassw = cursor.getString(cursor
            .getColumnIndex(MySecretsDbAdapter.FLD_PASSW));
        String memo = cursor.getString(cursor
            .getColumnIndex(MySecretsDbAdapter.FLD_MEMO));
        int category = cursor.getInt(cursor
            .getColumnIndex(MySecretsDbAdapter.FLD_CATEGORY));
        MySecretsItem item = new MySecretsItem(id, name, url, user, "",
            memo, category);
        item.setCryptPassw(cryptPassw);
        itemsArrayList.add(item);
        if ((_itemId >= 0) && (id == _itemId)) {
          idx = itemsArrayList.size() - 1;
        }
      } while (cursor.moveToNext());
    }
    arrayAdapter.notifyDataSetChanged();
    if (itemsArrayList.size() > 0)
      mSelectedItem = itemsArrayList.get(0);
    if (idx >= 0) {
      myListView.setSelection(idx);
      // yet selected, center in middle of list view
      myListView.setSelectionFromTop(idx, myListView.getHeight() / 2);
    }
    tvRecCount.setText(String.format(getString(R.string._d_found), itemsArrayList.size()));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // close database
    dbAdapter.close();
  }

  @Override
  public Dialog onCreateDialog(int id) {
    switch (id) {
    case DIALOG_ABOUT: {
      LayoutInflater liAbout = LayoutInflater.from(this);
      View aboutView = liAbout.inflate(R.layout.mysecrets_about, null); // from
                                                                        // about.xml
      AlertDialog.Builder aboutDialog = new AlertDialog.Builder(this);
      aboutDialog.setTitle(getString(R.string.app_name) + " "
          + getApplicationVersion());
      // aboutDialog.setIcon(android.R.drawable.ic_menu_info_details);
      aboutDialog.setIcon(R.drawable.icon);
      aboutDialog.setView(aboutView); // binds xml visual definition to code
      return aboutDialog.create();
    }
    } // switch
    return null;
  }

  @Override
  public void onPrepareDialog(int id, Dialog _dialog) {
    switch (id) {
    case (DIALOG_ABOUT):
      AlertDialog aboutDialog = (AlertDialog) _dialog;
      TextView tvAbout = (TextView) aboutDialog.findViewById(R.id.tvAbout); // from
                                                                            // about.xml
      tvAbout.setText(Html.fromHtml(getString(R.string.more_info_text)));
      break;
    } // switch
  }
  

  private String getApplicationVersion() {
    String versionName = "";
    PackageManager manager = getPackageManager();
    PackageInfo info;
    try {
      info = manager.getPackageInfo(getPackageName(), 0);
      versionName = info.versionName;
    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    return versionName;
  }

  private void updateFromPreferences() {
    sortByName = MySecretsApplication.getSharedPreferences().getBoolean(
        MySecretsPreferences.PREF_SORT_BY_NAME, true);
  }
  
  /**
   * 
   * @return true if must ask for main password
   */
  private boolean askForMainPassword() {
    if (!MySecretsApplication.getSharedPreferences().getBoolean(
        MySecretsPreferences.PREF_ASK_MAIN_KEY, false)) {
      return false;
    }
    final String passw = MySecretsApplication.getSharedPreferences().getString(
        MySecretsPreferences.PREF_MAIN_PASSWORD, "");
    if (passw.compareTo("") == 0) {
      return false;
    }
    return true;
  }  
}