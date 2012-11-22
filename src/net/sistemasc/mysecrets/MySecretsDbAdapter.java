package net.sistemasc.mysecrets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

/**
 * TABLE: CRUD and LOCATE DATABASE: open, close and create/update database
 */
public class MySecretsDbAdapter {
  // database and tables names
  private static final String DATABASE_NAME    = "mysecrets.sqlite";
  private static final String TABLE_NAME       = "secrets";
  // change version to force call update
  private static final int    DATABASE_VERSION = 1;
  // field names
  public static final String  KEY_ID           = "_id";
  public static final String  FLD_NAME         = "name";
  public static final String  FLD_URL          = "url";
  public static final String  FLD_USER         = "user";
  public static final String  FLD_PASSW        = "passw";
  public static final String  FLD_MEMO         = "memo";
  public static final String  FLD_CATEGORY     = "category";

  // work elements
  private final Context       context;
  private SQLiteDatabase      db;
  private MyOpenHelper        openHelper;

  private class MyOpenHelper extends SQLiteOpenHelper {

    private Context        context;
    /*
     * 
     * CREATE TABLE "secrets" ("_id" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL
     * UNIQUE , "name" TEXT COLLATE NOCASE, "url" TEXT, "user" TEXT, "passw"
     * TEXT, "memo" TEXT, "category" INTEGER NOT NULL DEFAULT 0); CREATE INDEX
     * "ix_name" ON "secrets" ("name" COLLATE NOCASE ASC); CREATE INDEX
     * "ix_category" ON "secrets" ("category" ASC);
     */
    // private static final String DROP_TABLE = "DROP TABLE IF EXISTS "
    // + TABLE_NAME + ";";
    private final String[] CREATE_DB_1 = {
                                           "DROP TABLE IF EXISTS " + TABLE_NAME
                                               + "; ",
                                           "CREATE TABLE IF NOT EXISTS "
                                               + TABLE_NAME
                                               + " ("
                                               + KEY_ID
                                               + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL  UNIQUE, "
                                               + FLD_NAME
                                               + " TEXT COLLATE NOCASE, "
                                               + FLD_URL
                                               + " TEXT, "
                                               + FLD_USER
                                               + " TEXT, "
                                               + FLD_PASSW
                                               + " TEXT, "
                                               + FLD_MEMO
                                               + " TEXT, "
                                               + FLD_CATEGORY
                                               + " INTEGER NOT NULL DEFAULT 0); ",
                                           "CREATE INDEX ix_name ON "
                                               + TABLE_NAME + " (" + FLD_NAME
                                               + " COLLATE NOCASE ASC); ",
                                           "CREATE INDEX ix_category ON "
                                               + TABLE_NAME + " ("
                                               + FLD_CATEGORY + " ASC);",
                                           "INSERT INTO "
                                               + TABLE_NAME
                                               + " ("
                                               + FLD_NAME
                                               + ", "
                                               + FLD_URL
                                               + ", "
                                               + FLD_USER
                                               + ", "
                                               + FLD_PASSW
                                               + ", "
                                               + FLD_MEMO
                                               + ", "
                                               + FLD_CATEGORY
                                               + ") VALUES('Sample secret','www.sistemasc.net','you','','This is a sample of a secret, you can delete it and start adding your own secrets or import a .csv file created with another application',0);",
                                           "INSERT INTO "
                                               + TABLE_NAME
                                               + " ("
                                               + FLD_NAME
                                               + ", "
                                               + FLD_URL
                                               + ", "
                                               + FLD_USER
                                               + ", "
                                               + FLD_PASSW
                                               + ", "
                                               + FLD_MEMO
                                               + ", "
                                               + FLD_CATEGORY
                                               + ") VALUES('Secreto de ejemplo','www.sistemasc.net','tú','','Esto es un secreto de ejemplo. Puedes borrarlo y empezar a crear tus propios secretos o importar un fichero .csv creado con otra aplicación',0);" };

    // INSERT INTO "secrets" (name, url, user, passw, memo, category)
    // VALUES('Sample secret','www.sistemasc.net','you','your password','This is
    // a sample of a secret, you can delete it and start adding your own
    // secrets',0);
    // INSERT INTO "secrets" (name, url, user, passw, memo, category)
    // VALUES('Secreto de ejemplo','www.sistemasc.net','tú','tu
    // contraseña','Esto es un secreto de ejemplo. Puedes borrarlo y empezar a
    // crear tus propios secretos',0);

    public MyOpenHelper(Context _context, String _name, CursorFactory _factory,
        int _version) {
      super(_context, _name, _factory, _version);
      context = _context;
    }

    @Override
    public void onCreate(SQLiteDatabase _db) {
      Log.i(this.context.getString(R.string.app_name), "begin create database");
      int len = CREATE_DB_1.length;
      for (int i = 0; i < len; i++) {
        _db.execSQL(CREATE_DB_1[i]);
      }
      Log.i(this.context.getString(R.string.app_name), "end create database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
      Log.w(DATABASE_NAME, "Upgrading from version " + _oldVersion + " to "
          + _newVersion);
      // _db.execSQL(UPDATE_2);
    }
  }

  public MySecretsDbAdapter(Context _context) {
    this.context = _context;
    openHelper = new MyOpenHelper(context, DATABASE_NAME, null,
        DATABASE_VERSION);
  }

  public void close() {
    if ((db != null) && db.isOpen()) {
      db.close();
      db = null;
    }
  }

  public void open() throws SQLiteException {
    try {
      db = openHelper.getWritableDatabase();
    } catch (SQLiteException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Builds WHERE clause
   * "UPPER(Field1) LIKE "%filter%" OR UPPER(Field2) LIKE "%filter%" ...
   * 
   * @param _filter
   *          any case filter
   * @param _fields
   *          array of string fields
   * @return WHERE clause
   */
  private String makeFilterWhere(String _filter, String[] _fields) {
    String result = null;
    String filter = _filter.toUpperCase();
    // escape well known problem makers
    filter = filter.replace("'", "''");
    filter = filter.replace("%", "\\%");
    for (String item : _fields) {
      if (result == null) {
        result = String.format("UPPER(%s) LIKE '%%%s%%'", item, filter);
      } else {
        result = result
            + String.format("OR UPPER(%s) LIKE '%%%s%%'", item, filter);
      }
    } // for
    result = result + "ESCAPE '\\'"; // lets inform to sqlite our escape char is
                                     // \ (if used)
    return result;
  }

  /**
   * Builds WHERE "CATEGORY = n"
   * 
   * @param _category
   * @return WHERE clause
   */
  private String makeCategoryWhere(int _category) {
    return String.format("%s = %d", FLD_CATEGORY, _category);
  }

  /**
   * 
   * @param _filter
   *          optional filter applied to all fields
   * @param _category
   *          optional filter by category. -1 means no filtering
   * @param _orderBy
   *          field name
   * @return
   */
  public Cursor getAllRows(String _filter, int _category, String _orderBy) {
    String where = (_filter == "") ? null : makeFilterWhere(_filter,
        new String[] { FLD_NAME, FLD_URL, FLD_USER, FLD_PASSW, FLD_MEMO });
    if (_category >= 0) {
      if (where == null) {
        where = makeCategoryWhere(_category);
      } else {
        where = String.format("(%s) AND (%s)", where,
            makeCategoryWhere(_category));
      }
    }
    return db.query(TABLE_NAME, new String[] { KEY_ID, FLD_NAME, FLD_URL,
        FLD_USER, FLD_PASSW, FLD_MEMO, FLD_CATEGORY }, where, null, null, null,
        _orderBy);
  }

  public int clearAllRows() {
    return db.delete(TABLE_NAME, null, null);
  }

  // C Insert (create) a new row
  public long createItem(MySecretsItem _item) {
    // Create a new row of values to insert.
    ContentValues newValues = new ContentValues();
    // Assign values for each row.

    // ID is autoinc, don't send it
    // newValues.put(KEY_ID, _item.getId());
    newValues.put(FLD_NAME, _item.getName());
    newValues.put(FLD_URL, _item.getUrl());
    newValues.put(FLD_USER, _item.getUser());
    newValues.put(FLD_PASSW, _item.getCryptPassw());
    newValues.put(FLD_MEMO, _item.getMemo());
    newValues.put(FLD_CATEGORY, _item.getCategory());
    // Insert the row.
    return db.insert(TABLE_NAME, null, newValues);
  }

  // R Read a row
  public MySecretsItem getItem(MySecretsItem _item) throws SQLException {
    Cursor cursor = db.query(true, TABLE_NAME, new String[] { KEY_ID, FLD_NAME,
        FLD_URL, FLD_USER, FLD_PASSW, FLD_MEMO, FLD_CATEGORY }, KEY_ID + "="
        + _item.getId(), null, null, null, null, null);
    if ((cursor.getCount() == 0) || !cursor.moveToFirst()) {
      throw new SQLException("Item not found: " + _item.toString());
    }

    String name = cursor.getString(cursor.getColumnIndex(FLD_NAME));
    String url = cursor.getString(cursor.getColumnIndex(FLD_URL));
    String user = cursor.getString(cursor.getColumnIndex(FLD_USER));
    String cryptPassw = cursor.getString(cursor.getColumnIndex(FLD_PASSW));
    String memo = cursor.getString(cursor.getColumnIndex(FLD_MEMO));
    int category = cursor.getInt(cursor.getColumnIndex(FLD_CATEGORY));

    MySecretsItem result = new MySecretsItem(_item.getId(), name, url, user,
        "", memo, category);
    result.setCryptPassw(cryptPassw);
    return result;
  }

  // U update a row
  public boolean updateItem(MySecretsItem _item) {
    ContentValues newValues = new ContentValues();
    // ID field remains unchanged
    // newValues.put(KEY_ID, _item.getId());
    newValues.put(FLD_NAME, _item.getName());
    newValues.put(FLD_URL, _item.getUrl());
    newValues.put(FLD_USER, _item.getUser());
    newValues.put(FLD_PASSW, _item.getCryptPassw());
    newValues.put(FLD_MEMO, _item.getMemo());
    newValues.put(FLD_CATEGORY, _item.getCategory());
    return db.update(TABLE_NAME, newValues, KEY_ID + "=" + _item.getId(), null) > 0;
  }

  // D delete a row
  public boolean deleteItem(MySecretsItem _item) {
    return db.delete(TABLE_NAME, KEY_ID + "=" + _item.getId(), null) > 0;
  }

  // LOCATE
  public Cursor locateItem(MySecretsItem _item) throws SQLException {
    Cursor result = db.query(true, TABLE_NAME, new String[] { KEY_ID, FLD_NAME,
        FLD_URL, FLD_USER, FLD_PASSW, FLD_MEMO, FLD_CATEGORY }, KEY_ID + "="
        + _item.getId(), null, null, null, null, null);
    if ((result.getCount() == 0) || !result.moveToFirst()) {
      throw new SQLException("Item not found: " + _item.toString());
    }
    return result;
  }

  public void beginTransaction() {
    db.beginTransaction();
  }

  public void endTransaction(boolean _wasOk) {
    if (_wasOk) {
      db.setTransactionSuccessful();
    }
    db.endTransaction(); // forces roll back
  }

}
