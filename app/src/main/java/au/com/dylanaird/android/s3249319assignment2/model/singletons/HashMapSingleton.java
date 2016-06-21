package au.com.dylanaird.android.s3249319assignment2.model.singletons;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import au.com.dylanaird.android.s3249319assignment2.model.objects.ContactObject;
import au.com.dylanaird.android.s3249319assignment2.model.objects.MovieObject;
import au.com.dylanaird.android.s3249319assignment2.model.objects.PartyObject;

/**
 * Created by Dylan on 22/08/2015.
 * <p/>
 * <p/>
 * This file is my singleton / single insatance of hashmaps, rather than creating new objects or
 * hashmaps this class is accessed and is only created once, later in the second assignment this
 * class with work hand in hand with the database singleton.
 */
public class HashMapSingleton {


    //hashmap to contain movieobject data structure
    public static final HashMap<String, MovieObject> MOVIE_MAP = new HashMap<String, MovieObject>();
    public static final HashMap<UUID, PartyObject> PARTY_MAP = new HashMap<UUID, PartyObject>();
    public static final HashMap<UUID, ContactObject> CONTACT_MAP = new HashMap<UUID, ContactObject>();


    //set the hashmap singleton to null before it is instantiated.
    private static volatile HashMapSingleton INSTANCE = null;

    // empty constructor to prevent instantiation
    private HashMapSingleton() {}

    public static HashMapSingleton getINSTANCE() {
        if (INSTANCE == null) {
            synchronized (HashMapSingleton.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HashMapSingleton();
                }
            }
        }
        return INSTANCE;
    }

    public HashMap<String, MovieObject> getMovieMap() {
        return MOVIE_MAP;
    }

    public synchronized void addMovie(String key, MovieObject m) {
        MOVIE_MAP.put(key, m);
    }

    public HashMap<UUID, PartyObject> getPartyMap() {
        return PARTY_MAP;
    }

    public HashMap<UUID, ContactObject> getContactMap() {
        return CONTACT_MAP;
    }

    public synchronized ContactObject getContact(UUID key){
        ContactObject c = CONTACT_MAP.get(key);
        return c;
    }

    public synchronized void addContactList(ArrayList<ContactObject>list){
        for(ContactObject c : list){
            CONTACT_MAP.put(c.getContactId(), c);
        }
    }
    public synchronized void removeContactList(UUID partyId){
        for(ContactObject c : CONTACT_MAP.values()){
            if(c.getContactPartyId().equals(partyId)){
                CONTACT_MAP.remove(c);
            }
        }
    }
    public synchronized void addContact(ContactObject c){
        CONTACT_MAP.put(c.getContactId(), c);
    }
    public synchronized void removeContact(ContactObject c){
        CONTACT_MAP.remove(c.getContactId());
    }

    public synchronized void addParty(UUID key, PartyObject p) {
        PARTY_MAP.put(key, p);
    }

    public synchronized void editParty(UUID key, PartyObject p ){
        if(PARTY_MAP.get(key).getPartyId() == p.getPartyId()){
            PARTY_MAP.remove(key);
            PARTY_MAP.put(p.getPartyId(), p);
        }
    }

    public synchronized MovieObject getMovie(String key) {
        return MOVIE_MAP.get(key);
    }

    public synchronized PartyObject getParty(UUID key) {
        return PARTY_MAP.get(key);
    }

    public synchronized boolean deleteParty(UUID key) {
        PARTY_MAP.remove(key);
        boolean deleted;
        if (PARTY_MAP.get(key) == null) {
            deleted = true;
        } else {
            deleted = false;
        }
        return deleted;
    }

    public synchronized HashMap<UUID, PartyObject> getPartyMapFromMovieId(String key) {
        HashMap<UUID, PartyObject> temp = new HashMap<UUID, PartyObject>();
        for (PartyObject p : PARTY_MAP.values()) {
            if (p.getPartyMovieId().equals(key)) {
                temp.put(PARTY_MAP.get(p.getPartyId()).getPartyId(), getParty(p.getPartyId()));
            }
        }
        return temp;
    }

    public synchronized void printMovieHashMap(){
        for(MovieObject m : MOVIE_MAP.values()){
            Log.d("Movies", m.getMovieId());
        }
    }
    public synchronized void printPartyHashMap(){
        for(PartyObject p : PARTY_MAP.values()){
            Log.d("Movies", p.getPartyId().toString());
        }
    }

    public void deleteMovie(String id) {
        MOVIE_MAP.remove(id);
    }
}
