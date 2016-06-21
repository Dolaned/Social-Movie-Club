package au.com.dylanaird.android.s3249319assignment2.controller.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.AttendeesAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.adapters.ContactPicker;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.ContactSQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage.PartySQLAdapter;
import au.com.dylanaird.android.s3249319assignment2.model.objects.ContactObject;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/*This class is my main party manager / controller, it has strings passed to it from the movie view
* intent to handle what this class will do, this class can create new parties aswell as edit old ones.*/
public class PartyManager extends AppCompatActivity {

    //declare class varibles.
    private static final int PICK_CONTACTS = 100;
    private static final String LOG_TAG = ContactPicker.class.getName();
    private PartyObject p;
    private UUID partyId;
    private String movieId;
    private ArrayList<ContactObject> attendeesDetails;
    private Calendar dateTimeCalender;
    private DatePickerDialog.OnDateSetListener date;
    private TimePickerDialog.OnTimeSetListener time;
    private AttendeesAdapter adapter;
    private PartySQLAdapter partySQLAdapter;
    private ContactSQLAdapter contactSQLAdapter;
    private String activityMode;
    //Activity fields
    private EditText partyLocation;
    private EditText partyVenue;
    private EditText partyDate;
    private EditText partyTime;
    //Activity Buttons
    private Button createNewParty;
    private Button addAttendees;
    private ListView attendeesList;

    //booleans for party button
    private boolean partyFieldsBool, partyDateTimeBool, partyAttendeesBool = false;

    /*this is the onclick listener that handles all the clicks within the activity,
    * it uses a switch to choose what field is passed to it, also calling the enable party button
    * method to see if the fields are populated*/
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bCreateNewParty:
                    /* this creates a new party object, adds it to the singleton hashmap,
                     *it also tells the movieview adapter to update its dataset lastly finishing
                     * the activity.*/
                    PartyObject p = new PartyObject(movieId, partyVenue.getText().toString(),
                            partyLocation.getText().toString(),
                            partyDate.getText().toString(),
                            partyTime.getText().toString());
                    for (ContactObject c : attendeesDetails) {
                        c.setContactPartyId(p.getPartyId());
                    }
                    p.setAttendees(attendeesDetails);
                    HashMapSingleton.getINSTANCE().addParty(p.getPartyId(), p);
                    partySQLAdapter.addPartyToDb(p);
                    contactSQLAdapter.addContactListToDb(p.getAttendees());
                    finish();
                    break;
                case R.id.bAddAttendees:
                    // this calls the contact intent to add attendees to the listview arraylist.
                    enableNewPartyButton();
                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(contactPickerIntent, PICK_CONTACTS);
                    break;
                case R.id.etNewpartyLocation:
                    enableNewPartyButton();
                    break;
                case R.id.etNewPartyVenue:
                    enableNewPartyButton();
                    break;
                case R.id.etNewPartyDate:
                    //calls the datepicker dialog to set the date.
                    enableNewPartyButton();
                    new DatePickerDialog(PartyManager.this, date, dateTimeCalender
                            .get(Calendar.YEAR), dateTimeCalender.get(Calendar.MONTH),
                            dateTimeCalender.get(Calendar.DAY_OF_MONTH)).show();
                    break;
                case R.id.etNewPartyTime:
                    //calls the timepicker to set the time.
                    enableNewPartyButton();
                    new TimePickerDialog(PartyManager.this, time, Calendar.HOUR_OF_DAY,
                            Calendar.MINUTE, true).show();
                    break;
            }
        }
    };

    /*I used this function to make sure on first click of the time and date fields that the
    * data/ time pickers show up straight away, leaving no room to hardcode. i use the hasFocus boolean
    * to make sure it has focus before calling and i update the enableparty button regardless*/
    private View.OnFocusChangeListener onFocusChangedListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.etNewPartyDate:
                    if (hasFocus) {
                        enableNewPartyButton();
                        new DatePickerDialog(PartyManager.this, date, dateTimeCalender
                                .get(Calendar.YEAR), dateTimeCalender.get(Calendar.MONTH),
                                dateTimeCalender.get(Calendar.DAY_OF_MONTH)).show();
                    } else {
                        enableNewPartyButton();
                    }
                    break;
                case R.id.etNewPartyTime:
                    if (hasFocus) {
                        enableNewPartyButton();
                        new TimePickerDialog(PartyManager.this, time, Calendar.HOUR_OF_DAY,
                                Calendar.MINUTE, true).show();
                    } else {
                        enableNewPartyButton();
                    }
                    break;
            }
        }
    };

    /*This just calls the enablePartyButton function and updates the attendeeslist listview*/
    @Override
    protected void onResume() {
        super.onResume();
        //create new movie adapter using updated hashmap
        attendeesList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        enableNewPartyButton();

    }

    /*this saves the date on backbutton pressed and finishes the activity*/
    public void onBackPressed() {
        super.onBackPressed();
        saveData();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_party);
        //get Bundle of extras from previous activitys
        Bundle bdl = getIntent().getExtras();

        //set private varibles.
        movieId = bdl.getString("movieId");
        String movieTitle = bdl.getString("movieTitle");


        //create all view items.
        initFields();
        //check the bdl to see the edit mode
        if (bdl.getString("CRUDMODE").equals("edit")) {
            activityMode = "edit";
            partyId = UUID.fromString(bdl.getString("partyId"));
            adapter = new AttendeesAdapter(partyId, attendeesDetails, PartyManager.this);
            attendeesList.setAdapter(adapter);
            loadData();
        } else {
            activityMode = "new";
            setTitle(movieTitle + " New Party");
            adapter = new AttendeesAdapter(UUID.randomUUID(), attendeesDetails, PartyManager.this);
            attendeesList.setAdapter(adapter);
        }
    }

    /*This is the onActivityResult for getting a contact it checks for duplicates and whether the
    * attendees phone number is valid and adds themm to the list*/
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACTS) {
            if (resultCode == RESULT_OK) {
                ContactPicker contactsManager = new ContactPicker(this, data);
                String name, phoneNumber;
                try {
                    name = contactsManager.getContactName();
                    phoneNumber = contactsManager.getContactPhoneNumber();
                    boolean exists = false;

                    if (phoneNumber == null) {
                        Toast.makeText(this, R.string.tNoNumber, Toast.LENGTH_SHORT).show();
                    } else {
                        if (attendeesDetails.size() > 0) {
                            for (int i = 0; i < attendeesDetails.size(); i++) {
                                if (attendeesDetails.get(i).getContactDisplayName().equals(name)) {
                                    exists = true;
                                }
                            }
                        }
                        if (!exists) {
                            if (activityMode.equals("edit")) {
                                ContactObject c = new ContactObject(name, phoneNumber, p.getPartyId(), UUID.randomUUID());
                                attendeesDetails.add(c);
                            } else {
                                ContactObject c = new ContactObject(name, phoneNumber, UUID.randomUUID());
                                attendeesDetails.add(c);
                            }
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

    /*This function is self explanatory it just sets all the views and onclick buttons aswell as the
    * date and time picker dialogs*/
    private void initFields() {
        //instantiate edittext fields.
        partyLocation = (EditText) findViewById(R.id.etNewpartyLocation);
        partyVenue = (EditText) findViewById(R.id.etNewPartyVenue);
        partyDate = (EditText) findViewById(R.id.etNewPartyDate);
        partyTime = (EditText) findViewById(R.id.etNewPartyTime);
        //instantiate buttons
        addAttendees = (Button) findViewById(R.id.bAddAttendees);
        createNewParty = (Button) findViewById(R.id.bCreateNewParty);
        attendeesList = (ListView) findViewById(R.id.lvAttendees);
        attendeesDetails = new ArrayList<>();
        dateTimeCalender = Calendar.getInstance();
        partySQLAdapter = new PartySQLAdapter(PartyManager.this);
        contactSQLAdapter = new ContactSQLAdapter(PartyManager.this);


        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                dateTimeCalender.set(Calendar.YEAR, year);
                dateTimeCalender.set(Calendar.MONTH, monthOfYear);
                dateTimeCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                partyDate.setText(sdf.format(dateTimeCalender.getTime()));
            }
        };

        time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                dateTimeCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
                dateTimeCalender.set(Calendar.MINUTE, minute);
                SimpleDateFormat sfd = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH);
                partyTime.setText(sfd.format(dateTimeCalender.getTime()));
            }
        };

        addAttendees.setOnClickListener(onClickListener);
        createNewParty.setOnClickListener(onClickListener);
        partyLocation.setOnClickListener(onClickListener);
        partyVenue.setOnClickListener(onClickListener);
        partyDate.setOnClickListener(onClickListener);
        partyDate.setOnFocusChangeListener(onFocusChangedListener);
        partyTime.setOnClickListener(onClickListener);
        partyTime.setOnFocusChangeListener(onFocusChangedListener);

        createNewParty.setClickable(false);
        createNewParty.setVisibility(View.GONE);
    }

    /*This is my function to check all the text fields to see if they are populated and then sets
    * the schedule party accordingly*/
    private void enableNewPartyButton() {
        if (activityMode.equals("new")) {
            if (partyLocation.getText().length() > 1 && partyVenue.getText().length() > 1) {
                partyFieldsBool = true;
            }
            if (partyTime.getText().length() >= 1 && partyDate.getText().length() >= 1) {
                partyDateTimeBool = true;
            }
            if (attendeesDetails.size() >= 1) {
                partyAttendeesBool = true;
            }
            if (partyFieldsBool && partyDateTimeBool && partyAttendeesBool) {
                createNewParty.setVisibility(View.VISIBLE);
                createNewParty.setClickable(true);
            } else {
                createNewParty.setClickable(false);
            }
        }
    }

    /*This function saves all the data from the current activity state to the hashmap*/
    private void saveData() {
        if (p != null) {
            p.setPartyLocation(partyLocation.getText().toString());
            p.setPartyVenue(partyVenue.getText().toString());
            p.setPartyDate(partyDate.getText().toString());
            p.setPartyTime(partyTime.getText().toString());
            p.setAttendees(attendeesDetails);
            //add to hashmap
            HashMapSingleton.getINSTANCE().addParty(p.getPartyId(), p);
            //HashMapSingleton.getINSTANCE().editParty(p.getPartyId(), p);
            partySQLAdapter.updateParty(p);
            contactSQLAdapter.updateContactList(p.getAttendees());
        }
    }

    /*This method loads the info from the hashmap and sets the create new party button as invisible
    * as it is not needed in the edit mode, it also sets the title of the activity*/
    private void loadData() {
        createNewParty.setVisibility(View.GONE);
        p = HashMapSingleton.getINSTANCE().getParty(partyId);
        partyLocation.setText(p.getPartyLocation());
        partyVenue.setText(p.getPartyVenue());
        partyDate.setText(p.getPartyDate());
        partyTime.setText(p.getPartyTime());
        attendeesDetails = p.getAttendees();
        setTitle("Edit Party");
        adapter = new AttendeesAdapter(p.getPartyId(), attendeesDetails, PartyManager.this);
        attendeesList.setAdapter(adapter);
    }
}