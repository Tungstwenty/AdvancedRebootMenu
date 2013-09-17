package hk.kennethso168.xposed.advancedrebootmenu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import de.robv.android.xposed.XposedBridge;

public class Settings extends Activity {
	private final String CLASS = "Settings.java";
	private void log(String message) {
    	if(Main.writeLogs()) XposedBridge.log(CLASS + ": " + message);
    }
    
    private void log(Throwable t){
    	if(Main.writeLogs()){
    		XposedBridge.log(CLASS + ": ");
    		XposedBridge.log(t);
    	}    	
    }
	
	private static String versionName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);

        
		// Display the fragment as the main content.
        if (savedInstanceState == null)
			getFragmentManager().beginTransaction().replace(android.R.id.content,
	                new PrefsFragment()).commit();
        try {
        	PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			versionName = info.versionName;
			
		} catch (NameNotFoundException e) {
			log(e);
		}
        
	}

	public static class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
		private void setIconVis() {
			SharedPreferences sharedPref = getPreferenceManager().getSharedPreferences();
			boolean show = !(sharedPref.getBoolean(KEY_PREF_HIDE_IC_LAUNCHER, false));
		    Context ctx = getActivity();
		    PackageManager pm = getActivity().getPackageManager();
		    
		    // Enable/disable activity-aliases
		    pm.setComponentEnabledSetting(
		            new ComponentName(ctx, "hk.kennethso168.xposed.advancedrebootmenu.Settings-ShowIcon"), 
		            show ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
		            PackageManager.DONT_KILL_APP
		    );
		}
		
		public static final String KEY_PREF_CONFIRM_DIALOG = "pref_confirm_dialog";
		public static final String KEY_PREF_APP_INFO = "pref_app_info";
		public static final String KEY_PREF_HIDE_IC_LAUNCHER = "pref_hide_ic_launcher";
		
		public void updateListPrefSumm(String key, int r_array){
			Resources res = getResources();
			String[] prefDescs = res.getStringArray(r_array);
			int prefValue = Integer.parseInt(getPreferenceScreen().getSharedPreferences().getString(key, "0"));
			findPreference(key).setSummary(prefDescs[prefValue]);		
		}
		
		public static class HideIconDialogFragment extends DialogFragment {
		    @Override
		    public Dialog onCreateDialog(Bundle savedInstanceState) {
		        // Use the Builder class for convenient dialog construction
		        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		        builder.setMessage(R.string.hide_ic_launcher_dialog)
		        	.setTitle(R.string.note_title)
		        	.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // do nothing
                   }
		        });
		        // Create the AlertDialog object and return it
		        return builder.create();
		    }
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// this is important because although the handler classes that read these settings
			// are in the same package, they are executed in the context of the hooked package
			getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
			
			addPreferencesFromResource(R.xml.preferences);
			
			updateListPrefSumm(KEY_PREF_CONFIRM_DIALOG, R.array.confirm_dialog);
			
			String aboutBefore = getResources().getString(R.string.app_info_before);
			String aboutAfter = getResources().getString(R.string.app_info_after);
			findPreference(KEY_PREF_APP_INFO).setSummary(aboutBefore + versionName + aboutAfter);
			setIconVis();
		}
		
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(KEY_PREF_CONFIRM_DIALOG))
	        {
				updateListPrefSumm(key, R.array.confirm_dialog);
	        }
			if (key.equals(KEY_PREF_HIDE_IC_LAUNCHER))
	        {
				setIconVis();
				DialogFragment df = new HideIconDialogFragment();
				df.show(getFragmentManager(), "APM_hide_icon_dialog");
	        }
	    }
		
		@Override
		public void onResume() {
		    super.onResume();
		    getPreferenceScreen().getSharedPreferences()
		            .registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
		    super.onPause();
		    getPreferenceScreen().getSharedPreferences()
		            .unregisterOnSharedPreferenceChangeListener(this);
		}
	}
}
