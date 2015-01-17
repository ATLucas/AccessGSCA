package lucasa398.accessgsca;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lucasa398 on 11/25/2014.
 */
public class PANout extends Fragment {
    private static String PANRequestURL = "https://app.sycamoreeducation.com/api/v1/User/";
    protected String ID, subject, date, message;
    private TextView textView;
    private static ArrayList<Recipient> recipients;

    public static PANout newInstance(JSONObject jsonObject) {
        PANout pan = new PANout();
        recipients = new ArrayList<Recipient>();

        Bundle args = new Bundle();
        try {
            JSONArray jArray = jsonObject.getJSONArray("Recipients");

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject o = jArray.getJSONObject(i);
                args.putString("toID:" + i, o.getString("ToID"));
                args.putString("toName:" + i, o.getString("ToName"));
                args.putString("delivered:" + i, o.getString("Delivered"));
                args.putString("closed:" + i, o.getString("Closed"));
                args.putString("deleted:" + i, o.getString("Deleted"));
            }
            args.putInt("numRecipients", jArray.length());

            args.putString("ID", jsonObject.getString("ID"));
            args.putString("subject", jsonObject.getString("Subject"));
            args.putString("date", jsonObject.getString("Date"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        pan.setArguments(args);

        return pan;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        ID = args.getString("ID");
        subject = args.getString("subject");
        date = args.getString("date");

        for (int i = 0; i < args.getInt("numRecipients"); i++) {
            String toID = args.getString("fromID");
            String toName = args.getString("from");
            String delivered = args.getString("delivered");
            String closed = args.getString("closed");
            String deleted = args.getString("deleted");
            recipients.add(new Recipient(toID, toName, delivered, closed, deleted));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pan_fragment, container, false);
        getMessage(view);
        return view;
    }

    private void getMessage(View view) {
        message = "Sycamore does not currently allow 3rd-party access to outgoing message bodies.";
        textView = (TextView) view;
        textView.setText(message);
    }

    private class Recipient {
        String toID, toName, delivered, closed, deleted;

        public Recipient(String a, String b,String c, String d, String e) {
            toID = a;
            toName = b;
            delivered = c;
            closed = d;
            deleted = e;
        }
    }
}