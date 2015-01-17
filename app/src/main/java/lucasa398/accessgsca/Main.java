package lucasa398.accessgsca;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lucasa398 on 11/8/2014.
 */
public class Main extends ActionBarActivity {
    private static String TokenRequestURL = "https://app.sycamoreeducation.com/oauth/token";
    private static String InfoRequestURL = "https://app.sycamoreeducation.com/api/v1/Me";
    private static String RedirectURI = "intent://oauthresponse";

    protected static String ClientID = "54584d61c4d96";
    protected static String ClientSecret = "f5ac51c14b26c28f44aacb470cf70430";

    public Fragment inbox, studentContacts, familyContacts, facultyContacts;
    private BackHandledFragment currentFragment;

    private String[] drawerTitles;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private CharSequence drawerTitle;
    private CharSequence title;
    private boolean drawerOpen;
    private ArrayList<Pair> drawerStrings;

    protected static String token,refreshToken, userID, level;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        // Create drawer strings
        drawerStrings = new ArrayList<>();
        drawerStrings.add(new Pair("PAN","Inbox"));
        drawerStrings.add(new Pair("Student","Contacts"));
        drawerStrings.add(new Pair("Family","Contacts"));
        drawerStrings.add(new Pair("Faculty","Contacts"));
        // Set the adapter for the list view
        drawerTitles = getResources().getStringArray(R.array.drawers_array);
        drawerList.setAdapter(new DrawerAdapter(this));
        // Set the list's click listener
        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        title = drawerTitle = getTitle();
        drawerOpen = false;
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(title);
                drawerOpen = false;
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(drawerTitle);
                drawerOpen = true;
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        //lock drawer until authenticated
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // drawer icon
        // Defer code dependent on restoration of previous instance state.
        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = sharedPref.getString(getString(R.string.saved_token), null);
        if(token==null){
            setFragment(new Login(), "Login");
        }
        else {
            initInfo();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        /*MenuItem item = menu.findItem(R.id.compose);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);*/
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            clearToken();
            return true;
        }
        if(item.getItemId() == android.R.id.home) {
            if(drawerOpen) {
                drawerLayout.closeDrawer(drawerList);
            }
            else {
                drawerLayout.openDrawer(drawerList);
            }
        }
        /*if(item.getItemId() == R.id.compose) {
            Fragment compose = new Compose();
            setFragment(compose, "Compose");
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(currentFragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    protected void setFragment(Fragment fragment, String title) {
        setTitle(title);
        currentFragment = (BackHandledFragment)fragment;
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    private void clearToken() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_token), null);
        editor.apply();
        token = null;
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        setFragment(new Login(), "Login");
    }

    public void initInfo() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = sharedPref.getString(getString(R.string.saved_token), null);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        HttpGet get = new HttpGet(InfoRequestURL);
        get.addHeader("Authorization", "Bearer " + token);
        new InitUserInfoTask().execute(get);
    }

    /** Swaps fragments in the main content view */
    private void selectItem(int position) {
        // Create a new fragment and specify what to show based on position
        Fragment fragment;
        if(position==0) {
            fragment = inbox;
        }
        /*else if(position==1) {
            fragment = outbox;
        }*/
        else if(position==1) {
            fragment = studentContacts;
        }
        else if(position==2) {
            fragment = familyContacts;
        }
        else {
            fragment = facultyContacts;
        }
        setFragment(fragment, drawerTitles[position]);

        // Highlight the selected item, update the title, and close the drawer
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
        getSupportActionBar().setTitle(title);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    //used to retrieve userID from server & initialize fragments
    private class InitUserInfoTask extends AsyncTask<HttpGet, Integer, Boolean> {
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
            String input = total.toString();

            JSONObject object = (JSONObject) new JSONTokener(input).nextValue();
            userID = object.getString("UserID");
            level = object.getString("Level");
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
            inbox = new Inbox();
            //outbox = new Outbox();
            studentContacts = Contacts.newInstance(1);
            familyContacts = Contacts.newInstance(2);
            facultyContacts = Contacts.newInstance(3);
            setFragment(inbox, "Inbox");
        }
    }

    //used to retrieve access token from server
    private class TokenRequestTask extends AsyncTask<HttpPost, Integer, Boolean> {
        protected Boolean doInBackground(HttpPost... post) {
            try {
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(post[0]);

                InputStream inputStream = response.getEntity().getContent();
                BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line);
                }
                String input = total.toString();

                JSONObject object = (JSONObject) new JSONTokener(input).nextValue();
                token = object.getString("access_token");
                refreshToken = object.getString("refresh_token");
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
            if(aBoolean) {
                saveToken();
                saveRefreshToken();
                initInfo();
            }
            else {
                Log.i("AccessGSCA", "failed to retrieve token");
            }
        }
    }

    @Override
    public void onNewIntent(Intent anIntent)
    {
        Uri uri = anIntent.getData();
            if (uri != null && uri.toString().startsWith("intent://oauthresponse"))
            {
                HttpPost post = new HttpPost(TokenRequestURL);
                String code = uri.getQueryParameter("code");
                List<NameValuePair> pairs = new ArrayList<NameValuePair>();
                pairs.add(new BasicNameValuePair("client_id", ClientID));
                pairs.add(new BasicNameValuePair("client_secret", ClientSecret));
                pairs.add(new BasicNameValuePair("code", code));
                pairs.add(new BasicNameValuePair("redirect_uri", RedirectURI));
                pairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
                try {
                    post.setEntity(new UrlEncodedFormEntity(pairs));
                    new TokenRequestTask().execute(post);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
    }

    private void saveToken()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_token), token);
        editor.apply();
    }

    private void saveRefreshToken()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_refresh_token), refreshToken);
        editor.apply();
    }

    private class DrawerAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private class ViewHolder {
            TextView textView1;
            TextView textView2;
        }

        public DrawerAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return drawerStrings.size();
        }

        public Pair getItem(int position) {
            return drawerStrings.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.drawer_list_item, null);
                holder.textView1 = (TextView) convertView.findViewById(R.id.drawer_textview1);
                holder.textView2= (TextView) convertView.findViewById(R.id.drawer_textview2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            try {
                holder.textView1.setText(drawerStrings.get(position).s1);
                holder.textView2.setText(drawerStrings.get(position).s2);
            }
            catch(Exception e){e.printStackTrace();}

            return convertView;
        }
    }

    public class Pair {
        String s1, s2;
         public Pair(String s1, String s2) {
             this.s1 = s1;
             this.s2 = s2;
         }
    }
}
