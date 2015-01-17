package lucasa398.accessgsca;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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

/**
 * Created by lucasa398 on 11/8/2014.
 */
public class Inbox extends Fragment implements BackHandledFragment{
    private static String InboxRequestURL = "https://app.sycamoreeducation.com/api/v1/User/";
    private static String userID;
    private static String token;
    private JSONArray jsonArray;
    private ListView inboxList;
    private int offset;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        offset = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.inbox_fragment, container, false);
        new RetrievePANsTask().execute(view);

        final Button button = (Button) view.findViewById(R.id.load_more_messages_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new RetrievePANsTask().execute(v);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        jsonArray = null;
        offset = 0;
    }

    public boolean onBackPressed() {
        return false;
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

        PAN fragment = PAN.newInstance(jsonObject);
        Main main = (Main)getActivity();
        main.setFragment(fragment, subject);

        // Highlight the selected item
        inboxList.setItemChecked(position, true);
    }

    private class RetrievePANsTask extends AsyncTask<View, Integer, Boolean> {
        private ProgressDialog dialog;

        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading...");
            dialog.show();
        }

        protected Boolean doInBackground(View... view) {
            try {
                token = Main.token;
                userID = Main.userID;
                HttpGet get = new HttpGet(InboxRequestURL + userID + "/PAN?offset=" + offset + "&preview=1");
                get.addHeader("Authorization", "Bearer " + token);

                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(get);

                InputStream inputStream = response.getEntity().getContent();
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                String reply = total.toString();
                JSONTokener jsonTokener = new JSONTokener(reply);
                if(jsonArray == null) {
                    jsonArray = (JSONArray) jsonTokener.nextValue();
                    Log.i("AccessGSCA",jsonArray.toString());
                }
                else {
                    JSONArray newJSONArray = (JSONArray) jsonTokener.nextValue();
                    for (int i = 0; i < newJSONArray.length(); i++) {
                        jsonArray.put(newJSONArray.getJSONObject(i));
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
            inboxList = (ListView) getActivity().findViewById(R.id.inbox_listview);
            inboxList.setAdapter(new InboxAdapter(getActivity()));
            inboxList.setOnItemClickListener(new InboxItemClickListener());
            offset = offset + 10;
            dialog.dismiss();
        }
    }

    private class InboxAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private class ViewHolder {
            TextView senderTextView;
            TextView dateTextView;
            TextView subjectTextView;
            TextView previewTextView;
        }

        public InboxAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public int getCount() {
            if(jsonArray!=null) {
                return jsonArray.length();
            }
            return 0;
        }

        public JSONObject getItem(int position) {
            try {
                return jsonArray.getJSONObject(position);
            }
            catch(Exception e){e.printStackTrace();}
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.inbox_list_item, null);
                holder.senderTextView = (TextView) convertView.findViewById(R.id.inbox_sender_textview);
                holder.dateTextView = (TextView) convertView.findViewById(R.id.inbox_date_textview);
                holder.subjectTextView = (TextView) convertView.findViewById(R.id.inbox_subject_textview);
                holder.previewTextView = (TextView) convertView.findViewById(R.id.inbox_preview_textview);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                holder.senderTextView.setText(jsonArray.getJSONObject(position).getString("From"));
                holder.dateTextView.setText(jsonArray.getJSONObject(position).getString("Date"));
                holder.subjectTextView.setText(jsonArray.getJSONObject(position).getString("Subject"));
                holder.previewTextView.setText(jsonArray.getJSONObject(position).getString("Content"));
            }
            catch(Exception e){e.printStackTrace();}

            return convertView;
        }
    }
}
