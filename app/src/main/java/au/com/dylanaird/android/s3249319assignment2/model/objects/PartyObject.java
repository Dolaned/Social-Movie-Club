package au.com.dylanaird.android.s3249319assignment2.model.objects;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by Dylan on 30/07/2015.
 * <p/>
 * <p/>
 * This class contains all the necessary information for the party object including the list of attendees and the attributing
 * movie it is attached to.
 */
public class PartyObject {
    //declaring private varibles.

    private String partyMovieId;
    private String partyVenue;
    private String partyLocation;
    private String partyTime;
    private String partyDate;
    private UUID partyId;
    private ArrayList<ContactObject> attendees;

    //default constructor for this object
    public PartyObject(String mId, String venue, String location, String date, String time) {
        this.partyMovieId = mId;
        this.partyVenue = venue;
        this.partyLocation = location;
        this.partyDate = date;
        this.partyTime = time;
        if (this.attendees == null) {
            this.attendees = new ArrayList<>();
        }

        this.partyId = UUID.randomUUID();
    }

    public ArrayList<ContactObject> getAttendees() {
        return attendees;
    }

    public void setAttendees(ArrayList<ContactObject> attendees) {
        this.attendees = attendees;
    }

    //needed getters and settings for this object.
    public String getPartyMovieId() {
        return this.partyMovieId;
    }

    public void setPartyMovieId(String movieId) {
        this.partyMovieId = movieId;
    }

    public String getPartyVenue() {
        return partyVenue;
    }

    public void setPartyVenue(String partyVenue) {
        this.partyVenue = partyVenue;
    }

    public String getPartyLocation() {
        return this.partyLocation;
    }

    public void setPartyLocation(String partyLocation) {
        this.partyLocation = partyLocation;
    }

    public int getAttendeesCount() {
        return attendees.size();
    }

    public void removeAttendeeFromList(int pos) {
        this.attendees.remove(pos);
    }

    public String getPartyDate() {
        return this.partyDate;
    }

    public void setPartyDate(String partyDate) {
        this.partyDate = partyDate;
    }

    public String getPartyTime() {
        return this.partyTime;
    }

    public void setPartyTime(String partyTime) {
        this.partyTime = partyTime;
    }

    public UUID getPartyId() {
        return this.partyId;
    }

    public void setPartyId(UUID s) {
        this.partyId = s;
    }

    public void addAttendeeToList(ContactObject c) {
        this.attendees.add(c);
    }
}
