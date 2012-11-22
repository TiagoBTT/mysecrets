package net.sistemasc.mysecrets;

import net.sistemasc.util.SimpleCrypto;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * A helper static class with common utilities
 * 
 * @author Tiago
 * 
 */
public final class MySecretsApplication {
  public static final String        LOG_TAG = "MySecrets";
  private static Context            context;
  private static SharedPreferences  sharedPreferences;
  private static MySecretsDbAdapter dbAdapter;
  private static SimpleCrypto       crypto;
  private static final String       PASSW   = "res/";

  public static void setContext(Context _context) {
    context = _context;
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    dbAdapter = new MySecretsDbAdapter(context);
    try {
      crypto = new SimpleCrypto(PASSW);
    } catch (Exception e) {
      Log.e(LOG_TAG, "Error initializating crypto system", e);
      Toast.makeText(context, e.getClass().getName() + " " + e.getMessage(),
          Toast.LENGTH_LONG).show();
    }
  }

  public static SimpleCrypto getCrypto() {
    return crypto;
  }

  public static Context getContext() {
    return context;
  }

  public static SharedPreferences getSharedPreferences() {
    return sharedPreferences;
  }

  public static MySecretsDbAdapter getDbAdapter() {
    return dbAdapter;
  }

  public static String getAndroidId() {
    String result = android.provider.Settings.Secure.getString(
        context.getContentResolver(),
        android.provider.Settings.Secure.ANDROID_ID);
    if (result == null) {
      result = "0123456789012345";
    }
    return result;
  }

  public static String getRescueKey() {
    String key = getAndroidId();
    if (key.length() > 15) {
      // 0123456789012345
      //      ^^^^^^
      return key.substring(5, 11);
    } else {
      return key;
    }
  }

}
