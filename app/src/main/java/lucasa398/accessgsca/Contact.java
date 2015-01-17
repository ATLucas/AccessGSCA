package lucasa398.accessgsca;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by lucasa398 on 11/28/2014.
 */
public class Contact extends Fragment implements BackHandledFragment{
    private static String ContactRequestURL = "https://app.sycamoreeducation.com/api/v1/School/1335/Directory/";
    private String name, ID, address1, address2, city, state, zip, homePhone;
    private int type;

    public static Contact newInstance(String name, String ID, int category) {
        Contact contact = new Contact();
        contact.type = category;
        contact.name = name;
        contact.ID = ID;
        return contact;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contact_fragment, container, false);

        new HttpGetTask().execute(view);

        final Button button = (Button) view.findViewById(R.id.contact_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Compose compose = Compose.newInstance(name, ID);
                Main main = (Main)getActivity();
                main.setFragment(compose, "Compose");
            }
        });

        return view;
    }

    public boolean onBackPressed() {
        Main main = (Main)getActivity();
        if(type==1) {
            main.setFragment(main.studentContacts, "Student Contacts");
        }
        else if(type==2) {
            main.setFragment(main.familyContacts, "Family Contacts");
        }
        else {
            main.setFragment(main.facultyContacts, "Employee Contacts");
        }
        return true;
    }

    private void setTextViews(View view) {
        TextView textView = (TextView) view.findViewById(R.id.contact_textView);
        textView.setText("contact info available soon");
    }

    // TODO show info
    private class HttpGetTask extends AsyncTask<View, Integer, Boolean> {
        private View view;
        private ProgressDialog dialog;

        protected void onPreExecute() {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Loading...");
            dialog.show();
        }

        protected Boolean doInBackground(View... view) {
            try {
                this.view = view[0];
                String token = Main.token;
                HttpGet get = new HttpGet(ContactRequestURL + ID);
                get.addHeader("Authorization", "Bearer " + token);

                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(get);
                // TODO null pointer
                /*InputStream inputStream = response.getEntity().getContent();
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                String reply = total.toString();
                JSONTokener jsonTokener = new JSONTokener(reply);
                JSONObject jObject = (JSONObject) jsonTokener.nextValue();

                try{
                    address1 = jObject.getString("Address1");
                }
                catch(Exception e){
                    e.printStackTrace();
                }*/
            }
            catch(Exception e){
                e.printStackTrace();
                return false;
            }
            return true;
        }

        protected void onPostExecute(Boolean response) {
            setTextViews(view);
            dialog.dismiss();
        }
    }
}
