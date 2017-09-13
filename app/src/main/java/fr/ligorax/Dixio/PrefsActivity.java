package fr.ligorax.Dixio;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class PrefsActivity extends PreferenceActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      addPreferencesFromResource(R.xml.preferences);
   }

   //***********************************************************************
   // onOptionsItemSelected()
   //***********************************************************************
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      // Manage the action bar
      switch (item.getItemId())
      {
         case android.R.id.home:
            navigateUpToFromChild(this, new Intent(this, MainActivity.class));
            return true;

         default:
            return super.onOptionsItemSelected(item);
      }
   }
}
