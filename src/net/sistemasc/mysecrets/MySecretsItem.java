package net.sistemasc.mysecrets;

import android.util.Log;
import android.widget.Toast;

/**
 * Simple class to store every field
 * 
 */
public class MySecretsItem {
  private long   id;
  private String name;
  private String url;
  private String user;
  private String clearPassw;
  private String cryptPassw;
  private String memo;
  private int    category;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getClearPassw() {
    return clearPassw;
  }

  public void setClearPassw(String passw) {
    this.clearPassw = passw;
    if (passw.length() > 0) {
      try {
        this.cryptPassw = MySecretsApplication.getCrypto().encrypt(passw);
      } catch (Exception e) {
        Log.e(MySecretsApplication.LOG_TAG, "Error encrypting", e);
        Toast.makeText(MySecretsApplication.getContext(),
            e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG)
            .show();
      }
    } else {
      cryptPassw = "";
    }
  }

  public String getCryptPassw() {
    return cryptPassw;
  }

  public void setCryptPassw(String cryptPassw) {
    this.cryptPassw = cryptPassw;
    if (cryptPassw.length() > 0) {
      try {
        this.clearPassw = MySecretsApplication.getCrypto().decrypt(cryptPassw);
      } catch (Exception e) {
        Log.e(MySecretsApplication.LOG_TAG, "Error decrypting", e);
        Toast.makeText(MySecretsApplication.getContext(),
            e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG)
            .show();
      }
    } else {
      clearPassw = "";
    }
  }

  public int getCategory() {
    return category;
  }

  public void setCategory(int category) {
    this.category = category;
  }

  public long getId() {
    return id;
  }

  public void setId(long _id) {
    this.id = _id;
  }

  public String getName() {
    return name;
  }

  public void setName(String _name) {
    this.name = _name;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String _user) {
    this.user = _user;
  }

  public String getMemo() {
    return memo;
  }

  public void setMemo(String _memo) {
    this.memo = _memo;
  }

  public MySecretsItem(long _id, String _name, String _url, String _user,
      String _passw, String _memo, int _category) {
    super();
    this.id = _id;
    this.name = _name;
    this.url = _url;
    this.user = _user;
    this.cryptPassw = "";
    this.setClearPassw(_passw);
    this.memo = _memo;
    this.category = _category;
  }

  public MySecretsItem() {
    super();
    // null constructor
  }

  @Override
  public String toString() {
    String concat;
    concat = name + " " + url + " " + user + " " + memo;
    return concat.toUpperCase();
  }
}
