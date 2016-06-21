package au.com.dylanaird.android.s3249319assignment2.controller.sqlLitePackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import au.com.dylanaird.android.s3249319assignment2.model.objects.ContactObject;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.DatabaseSingleton;
import au.com.dylanaird.android.s3249319assignment2.model.singletons.HashMapSingleton;

/**
 * Created by Dylan on 13/08/2015.
 */
public class ContactSQLAdapter {

    DatabaseSingleton mInstance;
    HashMapSingleton hInstance;
    Context context;
    Firebase contactRef;
    String contactId;

    public ContactSQLAdapter(Context context) {
        this.context = context;
        mInstance = DatabaseSingleton.getInstance(this.context);
        hInstance = HashMapSingleton.getINSTANCE();
    }

    public boolean syncWithFirebase() {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        String query = "SELECT * FROM " + DatabaseSingleton.TABLE_CONTACTS + " ;";
        Cursor c = db.rawQuery(query, null);
        contactRef = new Firebase("https://social-movie-club.firebaseio.com/contacts");

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            while ((!c.isAfterLast()) && c.getPosition() != c.getCount()) {

                /*
                * Check for each movie child in the cloud that a sqlite entry exists.
                * if not pull it down. then check if any new objects are in the sqlite database that
                * need to go to the cloud.
                * */
                contactId = c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_CONTACT_ID));
                contactRef.child(contactId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Log.d("Pushing to Firebase", contactId);
                            ContactObject m = getContact(contactId);
                            contactRef.child(contactId).setValue(m);
                            contactRef.child(contactId).child("lastUpdated").setValue(System.currentTimeMillis());
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                c.moveToNext();
            }
            c.close();
        }
        contactRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    if (searchForContactById(childSnapshot.getKey()) == 0) {

                        Log.d("Adding From Firebase", childSnapshot.getKey());
                        ContactObject contactObject = new ContactObject(childSnapshot.child(DatabaseSingleton.COLUMN_DISPLAYNAME).getValue().toString(),
                                childSnapshot.child(DatabaseSingleton.COLUMN_NUMBER).getValue().toString(),
                                childSnapshot.child(DatabaseSingleton.COLUMN_EMAIL).getValue().toString(),
                                UUID.fromString(childSnapshot.child(DatabaseSingleton.COLUMN_CONTACT_PARTY_ID).getValue().toString()),
                                UUID.fromString(childSnapshot.child(DatabaseSingleton.COLUMN_CONTACT_ID).getValue().toString()));
                        addContactToDb(contactObject);
                    } else {
                        Long lastUpdatedFirebase =
                                Long.parseLong(childSnapshot.child(DatabaseSingleton.COLUMN_CONTACT_LAST_UPDATED)
                                        .getValue().toString());
                        Long lastUpdatedSqlite = getLastUpdated(childSnapshot.getKey());

                        if (lastUpdatedSqlite > lastUpdatedFirebase) {
                            ContactObject contactObject = getContact(childSnapshot.getKey());
                            Log.d("Updating to Firebase", childSnapshot.getKey());
                            Map<String, Object> updates = new HashMap<String, Object>();
                            updates.put(DatabaseSingleton.COLUMN_DISPLAYNAME, contactObject.getContactDisplayName());
                            updates.put(DatabaseSingleton.COLUMN_NUMBER, contactObject.getContactPhoneNumber());
                            updates.put(DatabaseSingleton.COLUMN_EMAIL, contactObject.getContactEmailAddress());
                            updates.put(DatabaseSingleton.COLUMN_CONTACT_PARTY_ID, contactObject.getContactPartyId().toString());
                            updates.put(DatabaseSingleton.COLUMN_CONTACT_ID, contactObject.getContactId().toString());
                            updates.put(DatabaseSingleton.COLUMN_CONTACT_LAST_UPDATED, System.currentTimeMillis());
                            contactRef.child(childSnapshot.getKey()).updateChildren(updates);


                        } else if (lastUpdatedFirebase > lastUpdatedSqlite) {

                            Log.d("Updating From Firebase", childSnapshot.getKey());
                            ContactObject contactObject = new ContactObject(childSnapshot.child(DatabaseSingleton.COLUMN_DISPLAYNAME).getValue().toString(),
                                    childSnapshot.child(DatabaseSingleton.COLUMN_NUMBER).getValue().toString(),
                                    childSnapshot.child(DatabaseSingleton.COLUMN_EMAIL).getValue().toString(),
                                    UUID.fromString(childSnapshot.child(DatabaseSingleton.COLUMN_CONTACT_PARTY_ID).getValue().toString()),
                                    UUID.fromString(childSnapshot.child(DatabaseSingleton.COLUMN_CONTACT_ID).getValue().toString()));
                            updateContact(contactObject);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        return true;
    }

    public int searchForContactById(String cId) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_CONTACT_ID};
        String selection = DatabaseSingleton.COLUMN_CONTACT_ID + " =?";
        String[] selectionArgs = {cId}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_CONTACTS, columnsToReturn, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {
            c.close();
            return 1;
        } else {
            return 0;
        }
    }

    public long getLastUpdated(String cId) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_CONTACT_LAST_UPDATED};
        String selection = DatabaseSingleton.COLUMN_CONTACT_ID + " =?";
        String[] selectionArgs = {cId}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_CONTACTS, columnsToReturn, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            Long lastUpdatedSqlite = Long.parseLong(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_CONTACT_LAST_UPDATED)));

            c.close();
            return lastUpdatedSqlite;
        } else {
            return 0;
        }
    }


    public ArrayList<ContactObject> populateContactLists(UUID pId) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        ArrayList<ContactObject> list = new ArrayList<ContactObject>();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_CONTACT_ID,
                DatabaseSingleton.COLUMN_CONTACT_PARTY_ID,
                DatabaseSingleton.COLUMN_DISPLAYNAME,
                DatabaseSingleton.COLUMN_NUMBER,
                DatabaseSingleton.COLUMN_EMAIL};
        String selection = DatabaseSingleton.COLUMN_CONTACT_PARTY_ID + " =?";
        String[] selectionArgs = {pId.toString()}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_CONTACTS, columnsToReturn, selection, selectionArgs, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                ContactObject contact = new ContactObject(
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_DISPLAYNAME)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_NUMBER)),
                        c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_EMAIL)),
                        UUID.fromString(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_CONTACT_PARTY_ID))),
                        UUID.fromString(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_CONTACT_ID))));
                list.add(contact);
            }

            c.close();
        }
        return list;
    }

    public void addContactToDb(ContactObject c) {
        //get instance of Database.
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        //Values to insert.
        ContentValues cv = new ContentValues();
        cv.put(DatabaseSingleton.COLUMN_CONTACT_ID, c.getContactId().toString());
        cv.put(DatabaseSingleton.COLUMN_CONTACT_PARTY_ID, c.getContactPartyId().toString());
        cv.put(DatabaseSingleton.COLUMN_DISPLAYNAME, c.getContactDisplayName());
        cv.put(DatabaseSingleton.COLUMN_NUMBER, c.getContactPhoneNumber());
        cv.put(DatabaseSingleton.COLUMN_EMAIL, c.getContactEmailAddress());
        cv.put(DatabaseSingleton.COLUMN_CONTACT_LAST_UPDATED, System.currentTimeMillis());
        db.insert(DatabaseSingleton.TABLE_CONTACTS, null, cv);

    }

    public void deleteContactFromDB(UUID cId) {
        SQLiteDatabase db = mInstance.getMyWritableDatabase();
        db.delete(DatabaseSingleton.TABLE_CONTACTS, DatabaseSingleton.COLUMN_CONTACT_ID + " = ?", new String[]{cId.toString()});

    }

    public void updateContact(ContactObject c) {
        //get instance of Database.
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        //Values to insert.
        ContentValues cv = new ContentValues();
        cv.put(DatabaseSingleton.COLUMN_CONTACT_ID, c.getContactId().toString());
        cv.put(DatabaseSingleton.COLUMN_CONTACT_PARTY_ID, c.getContactPartyId().toString());
        cv.put(DatabaseSingleton.COLUMN_DISPLAYNAME, c.getContactDisplayName());
        cv.put(DatabaseSingleton.COLUMN_NUMBER, c.getContactPhoneNumber());
        cv.put(DatabaseSingleton.COLUMN_EMAIL, c.getContactEmailAddress());
        int i = db.update(DatabaseSingleton.TABLE_CONTACTS, cv, DatabaseSingleton.COLUMN_CONTACT_ID + " = '" + c.getContactId().toString() + "';", null);
        Log.d("Result edit", Integer.toString(i));
    }

    public ContactObject getContact(String cId) {
        ContactObject contactObject = null;
        SQLiteDatabase db = mInstance.getMyWritableDatabase();

        String[] columnsToReturn = {DatabaseSingleton.COLUMN_CONTACT_ID,
                DatabaseSingleton.COLUMN_DISPLAYNAME,
                DatabaseSingleton.COLUMN_NUMBER,
                DatabaseSingleton.COLUMN_CONTACT_LAST_UPDATED,
                DatabaseSingleton.COLUMN_CONTACT_PARTY_ID,
                DatabaseSingleton.COLUMN_EMAIL};
        String selection = DatabaseSingleton.COLUMN_CONTACT_ID + " =?";
        String[] selectionArgs = {cId}; // matched to "?" in selection
        Cursor c = db.query(DatabaseSingleton.TABLE_CONTACTS, columnsToReturn, selection, selectionArgs, null, null, null);
        if (c.moveToFirst()) {

            contactObject = new ContactObject(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_DISPLAYNAME)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_NUMBER)),
                    c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_EMAIL)),
                    UUID.fromString(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_CONTACT_PARTY_ID))),
                    UUID.fromString(c.getString(c.getColumnIndex(DatabaseSingleton.COLUMN_CONTACT_ID))));
        }

        c.close();
        return contactObject;
    }

    public void addContactListToDb(ArrayList<ContactObject> attendees) {
        for (ContactObject c : attendees) {
            addContactToDb(c);
        }
    }

    public void updateContactList(ArrayList<ContactObject> attendees) {
        for (ContactObject c : attendees) {
            Log.d("Contact", c.getContactDisplayName());
            if (searchForContactById(c.getContactId().toString()) > 0) {
                Log.d("Status", "Update");
                updateContact(c);
            } else {
                Log.d("Status", "Add");
                addContactToDb(c);
            }
        }
    }
}
