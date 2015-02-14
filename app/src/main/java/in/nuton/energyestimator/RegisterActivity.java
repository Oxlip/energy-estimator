package in.nuton.energyestimator;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends Activity {
    protected static final int PICK_ACCOUNT_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        String accountSelectionTitle = getResources().getString(R.string.reg_account_title);

        Intent googlePicker = AccountPicker.newChooseAccountIntent(null, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, true, accountSelectionTitle, null, null, null) ;
        startActivityForResult(googlePicker, PICK_ACCOUNT_REQUEST);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        if (requestCode == PICK_ACCOUNT_REQUEST && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            EditText txtEmail = (EditText) this.findViewById(R.id.txt_email);
            txtEmail.setText(accountName);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        protected ArrayList<String> getElectricityProviders() {
            ArrayList<String> result = new ArrayList<String>();
            List<DatabaseHelper.ElectricityProvider> providers = DatabaseHelper.getElectricityProviders(null);

            for(DatabaseHelper.ElectricityProvider provider: providers) {
                result.add(provider.stateName + " - " + provider.name);
            }

            return result;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_register, container, false);

            Button btnRegister = (Button) rootView.findViewById(R.id.btn_register);
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = getActivity();
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
                    boolean alreadyRegistered = pref.getBoolean("registered", false);

                    SharedPreferences.Editor ed = pref.edit();
                    ed.putBoolean("registered", true);
                    ed.commit();
                    /**
                     * If this is the first time the app is launched then relauch the main activity
                     * since it would called finish() when launching registerActicity.
                     */
                    if (!alreadyRegistered) {
                        startActivity(new Intent(activity,  HomeActivity.class));
                    }
                    activity.finish();
                }
            });

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, getElectricityProviders());
            Spinner spinnerProvider= (Spinner) rootView.findViewById(R.id.txt_electricity_provider);
            spinnerProvider.setAdapter(adapter);

            return rootView;
        }

    }
}
