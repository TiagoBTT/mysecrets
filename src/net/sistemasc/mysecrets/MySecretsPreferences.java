package net.sistemasc.mysecrets;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MySecretsPreferences extends PreferenceActivity {
  public static final String PREF_SORT_BY_NAME     = "PREF_SORT_BY_NAME";
  public static final String PREF_IMPEXP_SEPARATOR = "PREF_IMPEXP_SEPARATOR";
  public static final String PREF_ASK_MAIN_KEY     = "PREF_ASK_MAIN_KEY";
  public static final String PREF_MAIN_PASSWORD    = "PREF_MAIN_PASSWORD";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.mysecrets_preferences);
  }

}
