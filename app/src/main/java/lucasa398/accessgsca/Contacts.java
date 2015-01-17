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
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by lucasa398 on 11/8/2014.
 */
public class Contacts extends Fragment implements BackHandledFragment{
    private static String ContactsRequestURL = "https://app.sycamoreeducation.com/api/v1/User/" + Main.userID + "/PAN/Search?type=";
    private ArrayList<String> userIDs;
    private ArrayList<String> contactTitles;
    private ListView contactList;
    public int type;

    private ProgressDialog dialog;


    public static Fragment newInstance(int i) {
        Contacts contacts = new Contacts();
        contacts.type = i;
        return contacts;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts_fragment, container, false);
        new HttpGetFamiliesTask().execute(view);
        return view;
    }

    public boolean onBackPressed() {
        Main main = (Main)getActivity();
        main.setFragment(main.inbox, "Inbox");
        return true;
    }

    private class ContactItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // Create a new fragment and specify what to show based on position
        Fragment fragment = Contact.newInstance(contactTitles.get(position), userIDs.get(position), type);
        Main main = (Main) getActivity();
        main.setFragment(fragment, contactTitles.get(position));

        // Highlight the selected item
        contactList.setItemChecked(position, true);
    }

    private class HttpGetFamiliesTask extends AsyncTask<View, Integer, Boolean> {
        View view;

        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading...");
            dialog.show();
        }

        protected Boolean doInBackground(View... view) {
            try {
                this.view = view[0];
                userIDs = new ArrayList<String>();
                contactTitles = new ArrayList<String>();
                String token = Main.token;

                HttpGet get = new HttpGet(ContactsRequestURL + type);
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
                /*JSONObject jObject = (JSONObject)jsonTokener.nextValue();
                Log.i("AccessGSCA", jObject.toString());*/

                JSONArray jArray = (JSONArray)jsonTokener.nextValue();
                for (int i = 0; i < jArray.length(); i++) {
                    try {
                        userIDs.add(jArray.getJSONObject(i).getString("ID"));
                        contactTitles.add(jArray.getJSONObject(i).getString("LastName") + ", " + jArray.getJSONObject(i).getString("FirstName"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean response) {
            contactList = (ListView) view.findViewById(R.id.contacts_listView);
            contactList.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.contacts_list_item, R.id.contacts_list_item_text, contactTitles));
            contactList.setOnItemClickListener(new ContactItemClickListener());
            dialog.dismiss();
        }
    }
}