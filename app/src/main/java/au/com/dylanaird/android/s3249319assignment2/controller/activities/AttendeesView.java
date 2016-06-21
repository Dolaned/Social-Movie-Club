package au.com.dylanaird.android.s3249319assignment2.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.AttendeesAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.ContactPicker;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.ContactSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.PartySQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.threads.BackgroundThread;
import au.com.dylanaird.android.s3249319assignment2.model.objects.ContactObject;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

public class AttendeesView extends AppCompatActivity {
    //init private variables for the view.
    private static final int PICK_CONTACTS = 100;
    private static final String LOG_TAG = ContactPicker.class.getName();
    public int attendeesCount;
    private String movieID;
    private UUID partyId;
    private AttendeesAdapter adapter;
    private Button addAttendees;
    private Button sendSms;
    private ListView listView;
    private HashMapSingleton hInstance;
    private ContactSQLAdapter contactSQLAdapter;
    private ArrayList<ContactObject> contactList = new ArrayList<>();
    private BackgroundThread backgroundThread;
    private PartySQLAdapter partySQLAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendees_view);
        partySQLAdapter = new PartySQLAdapter(this);

        //set the activity title
        setTitle("People Attending The Party");
        //init all the layout views.
        initFields();

        //get the bundle passed from the last activity and the string attached to it.

        //load the attendees list size
        attendeesCount = hInstance.getParty(partyId).getAttendeesCount();

        //set the adapter to be my custom attendees adapter, grabbing information from my hashmap singleton
        adapter = new AttendeesAdapter(partyId, contactList, AttendeesView.this);
        listView.setAdapter(adapter);

    }

    /*This method is used in this instance to reload
     the list adapter with the new list of attendees
    */

    protected void onResume() {
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    /*This method is stopping the activity and freeing memory on the back button press.*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //Implement saving to Db Here.
        Runnable r = new Runnable() {
            @Override
            public void run() {
                hInstance.getParty(partyId).setAttendees(contactList);
                if (partySQLAdapter.updateParty(hInstance.getParty(partyId)) > 0) {
                    contactSQLAdapter.updateContactList(contactList);
                }
            }
        };
        backgroundThread = new BackgroundThread(r);
        r.run();

        this.finish();
    }

    /*This onActivityResult  is receiving the result from the contact intent, once the contact is
    * received it is added to the selected party this received method also has error checking
    * to make sure nothing is set to null. it also stopped duplicate entries being added to the
    * array list.*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACTS) {
            if (resultCode == RESULT_OK) {
                ContactPicker contactsManager = new ContactPicker(this, data);
                String name, phoneNumber;
                try {
                    name = contactsManager.getContactName();
                    phoneNumber = contactsManager.getContactPhoneNumber();
                    boolean exists = false;
                    attendeesCount = hInstance.getParty(partyId).getAttendeesCount();

                    if (phoneNumber == null) {
                        Toast.makeText(this, R.string.tNoNumber, Toast.LENGTH_SHORT).show();
                    } else {
                        if (attendeesCount > 0) {
                            for (int i = 0; i < attendeesCount; i++) {
                                if (hInstance.getParty(partyId).getAttendees().get(i).getContactDisplayName().equals(name)) {
                                    exists = true;
                                }
                            }
                        }
                        if (!exists) {
                            ContactObject c = new ContactObject(name, phoneNumber, partyId, UUID.randomUUID());
                            hInstance.getParty(partyId).addAttendeeToList(c);
                        } else {
                            Toast.makeText(this, R.string.tAlreadyInvited, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (ContactPicker.ContactQueryException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }

            }

        }
    }

    /*This method will init all the  view fields and sets all the onclick listeners*/
    private void initFields() {

        //init feilds
        addAttendees = (Button) findViewById(R.id.bAddAttendees);
        listView = (ListView) findViewById(R.id.lvAttendees);
        sendSms = (Button) findViewById(R.id.bSendSms);
        //get extras.
        Bundle bdl = getIntent().getExtras();
        movieID = bdl.getString("movieId");
        partyId = UUID.fromString(bdl.getString("partyId"));
        hInstance = HashMapSingleton.getINSTANCE();
        //contact stuff
        if (hInstance.getParty(partyId).getAttendees() != null) {
            contactList = hInstance.getParty(partyId).getAttendees();
        }

        contactSQLAdapter = new ContactSQLAdapter(AttendeesView.this);

        addAttendees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, PICK_CONTACTS);
            }
        });

        /*this onclick listener is responsible for send the SMS to the attendees, it also has error
        * checking in it, in this method i only use spilting of the arraylist with a new line
        * character which will extend to a posible class of attendees for the second assignment.*/
        sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendeesCount = HashMapSingleton.getINSTANCE().getParty(partyId).getAttendeesCount();
                if (attendeesCount > 0) {
                    for (int i = 0; i < attendeesCount; i++) {
                        ContactObject c = hInstance.getParty(partyId).getAttendees().get(i);
                        sendSMS(c.getContactDisplayName(), c.getContactPhoneNumber());
                    }
                    Toast.makeText(getApplicationContext(), R.string.tSendInvites, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.tNoInvites, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /*This method will send the message adding all the info to the message string from the movie
    * object and party object.
    * it uses the default smsManager setup in android.*/
    private void sendSMS(String name, String phoneNumber) {
        SmsManager sms = SmsManager.getDefault();
        String movieTitle = HashMapSingleton.getINSTANCE().getMovie(movieID).getMovieTitle();
        PartyObject p = HashMapSingleton.getINSTANCE().getParty(partyId);
        String message = "Hello " + name + " You have been invited to watch: " + movieTitle + "\n"
                + "Location: " + p.getPartyLocation() + "\n"
                + "Venue: " + p.getPartyVenue() + "\n"
                + "Date: " + p.getPartyDate() + "\n"
                + "Time: " + p.getPartyTime();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }


}
