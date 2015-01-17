package lucasa398.accessgsca;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by lucasa398 on 11/25/2014.
 */
public class PAN extends Fragment implements BackHandledFragment{
    private static String PANRequestURL = "https://app.sycamoreeducation.com/api/v1/User/";
    private String ID, fromID, from, subject, date, viewed, message;
    TextView senderTextView, dateTextView, messageTextView;

    public static PAN newInstance(JSONObject jsonObject) {
        PAN pan = new PAN();

        Bundle args = new Bundle();
        try {
            args.putString("ID", jsonObject.getString("ID"));
            args.putString("fromID", jsonObject.getString("FromID"));
            args.putString("from", jsonObject.getString("From"));
            args.putString("subject", jsonObject.getString("Subject"));
            args.putString("date", jsonObject.getString("Date"));
        }
        catch(Exception e) {e.printStackTrace();}
        pan.setArguments(args);

        return pan;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        ID = args.getString("ID");
        fromID = args.getString("fromID");
        from = args.getString("from");
        subject = args.getString("subject");
        date = args.getString("date");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pan_fragment, container, false);
        senderTextView = (TextView) view.findViewById(R.id.PAN_sender_textview);
        dateTextView = (TextView) view.findViewById(R.id.PAN_date_textview);
        messageTextView = (TextView) view.findViewById(R.id.PAN_message_textview);
        senderTextView.setText(from);
        dateTextView.setText(date);
        getMessage();

        final Button button = (Button) view.findViewById(R.id.reply_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Compose compose = Compose.newInstance(from, fromID);
                Main main = (Main)getActivity();
                main.setFragment(compose, "Compose");
            }
        });

        return view;
    }

    public boolean onBackPressed() {
        Main main = (Main)getActivity();
        main.setFragment(main.inbox, "Inbox");
        return true;
    }

    private void getMessage() {
        String token = Main.token;
        String userID = Main.userID;
        HttpGet get = new HttpGet(PANRequestURL + userID + "/PAN/" + ID);
        get.addHeader("Authorization", "Bearer " + token);
        new RetrieveMessageTask().execute(get);
    }

    private class RetrieveMessageTask extends AsyncTask<HttpGet, Integer, Boolean> {
        private ProgressDialog dialog;

        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading...");
            dialog.show();
        }

        protected Boolean doInBackground(HttpGet... get) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(get[0]);

                InputStream inputStream = response.getEntity().getContent();
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                String reply = total.toString();
                JSONTokener jsonTokener = new JSONTokener(reply);
                JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
                message = jsonObject.getString("Message");
            }
            catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean response) {
            messageTextView.setText(message);
            messageTextView.setMovementMethod(new ScrollingMovementMethod());
            dialog.dismiss();
        }
    }
}
