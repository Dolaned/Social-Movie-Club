package au.com.dylanaird.android.s3249319assignment2.model.objects;

import java.util.UUID;

/**
 * Created by Dylan on 8/09/2015.
 */
public class ContactObject {
    private String contactDisplayName;
    private String contactPhoneNumber;
    private String contactEmailAddress;
    private UUID contactPartyId;
    private UUID contactId;

    public ContactObject(String dName, String n, UUID pId, UUID cId) {
        this.contactDisplayName = dName;
        this.contactPhoneNumber = n;
        this.contactPartyId = pId;
        this.contactId = cId;
    }

    public ContactObject(String dName, String n, UUID cId) {
        this.contactDisplayName = dName;
        this.contactPhoneNumber = n;
        this.contactId = cId;
    }

    public ContactObject(String dName, String n, String email, UUID pId, UUID cId) {
        this.contactDisplayName = dName;
        this.contactPhoneNumber = n;
        this.contactEmailAddress = email;
        if (email == null) {
            this.contactEmailAddress = "EmailPlaceholder";
        } else {
            this.contactEmailAddress = email;
        }
        this.contactPartyId = pId;
        this.contactId = cId;
    }

    public String getContactDisplayName() {
        return contactDisplayName;
    }

    public void setContactDisplayName(String contactDisplayName) {
        this.contactDisplayName = contactDisplayName;
    }

    public String getContactEmailAddress() {
        return contactEmailAddress;
    }

    public void setContactEmailAddress(String contactEmailAddress) {
        this.contactEmailAddress = contactEmailAddress;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
    }

    public UUID getContactPartyId() {
        return this.contactPartyId;
    }

    public void setContactPartyId(UUID u) {
        this.contactPartyId = u;
    }

    public UUID getContactId() {
        return this.contactId;
    }
}
