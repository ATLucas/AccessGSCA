package lucasa398.accessgsca;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by lucasa398 on 11/28/2014.
 */
public class Compose extends Fragment implements BackHandledFragment{
    private static String ComposeURL = "https://app.sycamoreeducation.com/api/v1/User/" + Main.userID + "/PAN";

    String toName, toUserID;
    boolean sendSuccessful;


    public static Compose newInstance(String name, String ID) {
        Compose compose = new Compose();
        compose.toName = name;
        compose.toUserID = ID;
        return compose;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.compose_fragment, container, false);

        TextView textView = (TextView) view.findViewById(R.id.compose_to);
        textView.setText(toName);

        final Button button = (Button) view.findViewById(R.id.send_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                hideKeyboard();
                new HttpPostTask().execute(view);
            }
        });

        return view;
    }

    public boolean onBackPressed() {
        Main main = (Main)getActivity();
        main.setFragment(main.inbox, "Inbox");
        return true;
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //used to retrieve access token from server
    private class HttpPostTask extends AsyncTask<View, Integer, Boolean> {
        protected Boolean doInBackground(View... view) {
            try {
                HttpPost post = new HttpPost(ComposeURL);
                EditText subjectEditText = (EditText)view[0].findViewById(R.id.compose_subject);
                EditText messageEditText = (EditText)view[0].findViewById(R.id.compose_message);
                JSONObject object = new JSONObject();
                object.put("Subject", subjectEditText.getText().toString());
                object.put("Message", messageEditText.getText().toString());
                object.put("ToUID", toUserID);
                StringEntity se = new StringEntity(object.toString());
                Log.i("AccessGSCA", object.toString());
                post.addHeader("Authorization", "Bearer " + Main.token);
                post.setEntity(se);

                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(post);

                InputStream inputStream = response.getEntity().getContent();
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                String input = total.toString();

                JSONObject responseObj = (JSONObject) new JSONTokener(input).nextValue();
                int success = responseObj.getInt("Success");
                if(success==1) {
                    sendSuccessful = true;
                }
                else {
                    sendSuccessful = false;
                }
            }
            catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if(sendSuccessful) {
                Toast.makeText(getActivity().getApplicationContext(), "message sent successfully!", Toast.LENGTH_SHORT).show();
                Main main = (Main)getActivity();
                main.setFragment(main.inbox, "Inbox");
            }
            else {
                Toast.makeText(getActivity().getApplicationContext(), "unfortunately the message didn't go through", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
