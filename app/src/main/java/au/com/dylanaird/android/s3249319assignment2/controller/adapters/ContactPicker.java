package au.com.dylanaird.android.s3249319assignment2.controller.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

/**
 * Created by Dylan on 27/08/2015.
 * <p/>
 * this is a slighty modified version of the example code provided in the lab, i have changed it to
 * use name and number rather than email address because i have more contacts with numbers than emails
 * for the demo.
 */
public class ContactPicker {

    private static final String LOG_TAG = ContactPicker.class.getName();
    private Context context;
    private Intent intent;

    /**
     * @param aContext The context through which the Android Contacts Picker Activity
     *                 was launched
     * @param anIntent The intent returned from the Android Contacts Picker Activity
     */
    public ContactPicker(Context aContext, Intent anIntent) {
        this.context = aContext;
        this.intent = anIntent;
    }

    /**
     * Retrieves the display Name of a contact
     *
     * @return Name of the contact referred to by the URI specified through the
     * intent, {@link ContactPicker#intent}
     * @throws ContactQueryException if querying the Contact Details Fails
     */
    public String getContactName() throws ContactQueryException {
        Cursor cursor = null;
        String name = null;
        try {
            cursor = context.getContentResolver().query(intent.getData(), null,
                    null, null, null);
            if (cursor.moveToFirst())
                name = cursor.getString(cursor
                        .getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            throw new ContactQueryException(e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return name;
    }

    /**
     * Retrieves the email of a contact
     *
     * @return Email of the contact referred to by the URI specified through the
     * intent, {@link ContactPicker#intent}
     * @throws ContactQueryException if querying the Contact Details Fails
     */
    public String getContactPhoneNumber() throws ContactQueryException {
        Cursor cursor = null;
        String phoneNumber = null;
        try {
            cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{intent.getData().getLastPathSegment()}, null);

            if (cursor.moveToFirst())
                phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            throw new ContactQueryException(e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return phoneNumber;
    }

    public class ContactQueryException extends Exception {
        private static final long serialVersionUID = 1L;

        public ContactQueryException(String message) {
            super(message);
        }
    }

}