package lucasa398.accessgsca;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by lucasa398 on 11/25/2014.
 */
public class Outbox extends Fragment{
    private static String OutboxRequestURL = "https://app.sycamoreeducation.com/api/v1/User/";
    private static String userID;
    private static String token;
    private ArrayList<String> outboxTitles;
    private JSONArray jsonArray;
    private ListView outboxList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshOutbox();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        return inflater.inflate(R.layout.outbox_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshOutbox();
    }

    private void refreshOutbox()
    {
        token = Main.token;
        userID = Main.userID;
        HttpGet get = new HttpGet(OutboxRequestURL + userID + "/PAN/Outbox");
        get.addHeader("Authorization", "Bearer " + token);
        new HttpGetTask().execute(get);
    }

    private class InboxItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        JSONObject jsonObject;
        String subject;
        // Create a new fragment and specify what to show based on position
        try{jsonObject = jsonArray.getJSONObject(position);subject = jsonObject.getString("Subject");}
        catch(Exception e){e.printStackTrace();return;}
        PANout fragment = PANout.newInstance(jsonObject);
        Main main = (Main)getActivity();
        main.setFragment(fragment, subject);

        // Highlight the selected item
        outboxList.setItemChecked(position, true);
    }

    // TODO read-in date & maybe recipients for outbox view
    private class HttpGetTask extends AsyncTask<HttpGet, Integer, Boolean> {
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
                jsonArray = (JSONArray) jsonTokener.nextValue();

                outboxTitles = new ArrayList<String>();
                for(int i=0;i<jsonArray.length();i++) {
                    try{
                        outboxTitles.add(jsonArray.getJSONObject(i).getString("Subject"));
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean response) {
            outboxList = (ListView) getView();
            outboxList.setAdapter(new ArrayAdapter<String>(getActivity(),R.layout.inbox_list_item, outboxTitles));
            outboxList.setOnItemClickListener(new InboxItemClickListener());
            dialog.dismiss();
        }
    }
}
