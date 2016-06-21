package au.com.dylanaird.android.s3249319assignment2.controller.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.UUID;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.activities.AttendeesView;
import au.com.dylanaird.android.s3249319assignment2.controller.activities.PartyManager;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.ContactSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.PartySQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.model.objects.ContactObject;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/**
 * Created by Dylan on 23/08/2015.
 * <p/>
 * This adapter handles the adapters views, removing items and and editing them, this adapter has a
 * key set which comes from the partyhashmap
 */
public class PartyAdapter extends BaseAdapter {
    private ArrayList<UUID> pKeys = null;
    private Context context;
    private String movieId;
    private PartySQLAdapter partySQLAdapter;
    private ContactSQLAdapter contactSQLAdapter;
    private Activity activity;
    private HashMapSingleton hInstance;
    private Firebase partyRef = new Firebase("https://social-movie-club.firebaseio.com/parties");
    private Firebase singleParty;

    public PartyAdapter(String m, Activity a) {
        pKeys = new ArrayList<UUID>(HashMapSingleton.getINSTANCE().getPartyMapFromMovieId(m).keySet());
        this.activity = a;
        this.context = a;
        this.movieId = m;
        partySQLAdapter = new PartySQLAdapter(activity);
        contactSQLAdapter = new ContactSQLAdapter(activity);
        hInstance = HashMapSingleton.getINSTANCE();
        Firebase.setAndroidContext(context);
    }

    @Override
    public int getCount() {
        return hInstance.getPartyMapFromMovieId(movieId).size();
    }

    @Override
    public PartyObject getItem(int position) {
        return hInstance.getPartyMapFromMovieId(movieId).get(pKeys.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        //get the movie object position
        final PartyObject p = getItem(position);
        final View result;
        singleParty = partyRef.child(p.getPartyId().toString());

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_party, parent, false);
        } else {
            result = convertView;
        }

        //Text Views
        TextView partyLocation = (TextView) result.findViewById(R.id.tvpartyLocation);
        TextView partyVenue = (TextView) result.findViewById(R.id.tvpartyVenue);
        TextView partyDate = (TextView) result.findViewById(R.id.tvpartyDate);
        TextView partyTime = (TextView) result.findViewById(R.id.tvPartyTime);

        //attendees count and view
        TextView attendeesCount = (TextView) result.findViewById(R.id.tvAttendeesCount);

        //set the text fields with the objects details
        partyLocation.setText(p.getPartyLocation());
        partyVenue.setText(p.getPartyVenue());
        partyDate.setText(p.getPartyDate());
        partyTime.setText(p.getPartyTime());

        for (ContactObject c : p.getAttendees()) {
            Log.d("Contact", c.getContactDisplayName());
        }
        Log.d("Count", Integer.toString(p.getAttendeesCount()));

        attendeesCount.setText(Integer.toString(p.getAttendeesCount()));

        Button viewAttendees = (Button) result.findViewById(R.id.bViewAttendees);
        Button deleteParty = (Button) result.findViewById(R.id.bDeleteParty);
        Button editParty = (Button) result.findViewById(R.id.bEditParty);

        // onclick to view the party attendees
        viewAttendees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, AttendeesView.class);
                i.putExtra("movieId", p.getPartyMovieId());
                i.putExtra("partyId", p.getPartyId().toString());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        //This button deletes the party from the list
        deleteParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog(p, position);
            }
        });
        editParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PartyManager.class);
                i.putExtra("movieId", movieId);
                i.putExtra("CRUDMODE", "edit");
                i.putExtra("partyId", p.getPartyId().toString());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });
        return result;
    }

    public void alertDialog(final PartyObject p, final int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:


                        if (hInstance.deleteParty(p.getPartyId())) {

                            singleParty.removeValue(new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError != null) {
                                        Toast.makeText(context, "An Error Has Occured", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Successfully Removed From Firebase", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                            if (partySQLAdapter.deletePartyFromDB(p.getPartyId())) {
                                Log.d("Party Deleted", p.getPartyId().toString());
                                deleteAttendees(p);
                            }
                            Log.d("Key Delete: ", pKeys.get(position).toString());
                            pKeys.remove(position);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Party Removed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, R.string.tPartyNotDeleted, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:

                        Toast.makeText(context, "No Changes Made", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

    }

    private void deleteAttendees(PartyObject p) {
        Firebase fRef = new Firebase("https://social-movie-club.firebaseio.com/contacts/");
        Log.d("Attendees for delete", Integer.toString(p.getAttendeesCount()));
        for (final ContactObject c : p.getAttendees()) {
            contactSQLAdapter.deleteContactFromDB(c.getContactId());
            fRef.child(c.getContactId().toString()).removeValue(new Firebase.CompletionListener() {
                @Override
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    if (firebaseError == null) {
                        Log.d("contact", "Contact: " + c.getContactDisplayName() + " Removed");
                    } else {
                        Log.e("Firebase Error", firebase.toString());
                    }
                }
            });
        }
    }
}