package lucasa398.accessgsca;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by lucasa398 on 11/27/2014.
 */
public class Login extends Fragment implements BackHandledFragment{
    private static String AuthenticationURL = "https://app.sycamoreeducation.com/oauth/authorize?response_type=code&client_id=54584d61c4d96&scope=open general individual&redirect_uri=intent://oauthresponse";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        final Button button = (Button) view.findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                Main.refreshToken = sharedPref.getString(getString(R.string.saved_refresh_token), null);
                Main.token = sharedPref.getString(getString(R.string.saved_token), null);
                if(Main.token!=null)//@TO-DO: make this refresh every ten years
                {
                    // TODO refreshToken();
                }
                else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AuthenticationURL));
                    startActivity(intent);
                }
            }});

        return view;
    }

    public boolean onBackPressed() {
        return false;
    }
}
