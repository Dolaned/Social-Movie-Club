package au.com.dylanaird.android.s3249319assignment2.controller.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.ContactSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.model.objects.ContactObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/**
 * Created by Dylan on 28/08/2015.
 * This class is a baseadapter that i have customly made for the attendees view,
 * it is responsible for handling all of the information and data manipulation.
 * i can dynamically remove attendees from the activities list view from this class.
 */
public class AttendeesAdapter extends BaseAdapter {

    private UUID partyId;
    private ArrayList<ContactObject> attendeesList;
    private HashMapSingleton hInstance;
    private Activity activity;
    private ContactSQLAdapter contactSQLAdapter;
    private boolean isHashmapParty;

    public AttendeesAdapter(UUID k, ArrayList<ContactObject> list, Activity activity) {
        this.partyId = k;
        this.hInstance = HashMapSingleton.getINSTANCE();
        this.activity = activity;
        this.contactSQLAdapter = new ContactSQLAdapter(activity);
        if (hInstance.getParty(k) != null) {
            attendeesList = hInstance.getParty(k).getAttendees();
            isHashmapParty = true;
        } else {
            this.attendeesList = list;
            isHashmapParty = false;
        }

    }

    @Override
    public int getCount() {
        return attendeesList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        final ContactObject c = attendeesList.get(position);
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendees, parent, false);
        } else {
            result = convertView;
        }
        TextView attendeesName = (TextView) result.findViewById(R.id.tvAttendeeName);
        TextView attendeesNumber = (TextView) result.findViewById(R.id.tvAttendeeNumber);
        Button removeAttendee = (Button) result.findViewById(R.id.bDeleteAttendee);


        attendeesName.setText(c.getContactDisplayName());
        attendeesNumber.setText(c.getContactPhoneNumber());
        removeAttendee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isHashmapParty) {
                    hInstance.getParty(partyId).removeAttendeeFromList(position);
                    contactSQLAdapter.deleteContactFromDB(c.getContactId());
                    notifyDataSetChanged();
                } else {
                    attendeesList.remove(position);
                    notifyDataSetChanged();
                }
            }
        });
        return result;
    }
}
