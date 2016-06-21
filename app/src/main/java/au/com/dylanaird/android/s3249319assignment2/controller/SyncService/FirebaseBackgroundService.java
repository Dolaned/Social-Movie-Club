package au.com.dylanaird.android.s3249319assignment2.controller.SyncService;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import au.com.dylanaird.android.s3249319assignment2.R;
import au.com.dylanaird.android.s3249319assignment2.controller.activities.Splash;

/**
 *
 * This class is not part of the assingment i was just playing around with firebase functions
 * Created by Dylan on 12/10/2015.
 */
public class FirebaseBackgroundService extends Service {
    private ValueEventListener handler;
    private Runnable mTask = new Runnable() {
        public void run() {
            Firebase dataRef = new Firebase("https://social-movie-club.firebaseio.com/parties");
            handler = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    postNotif();
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            };
            dataRef.addValueEventListener(handler);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Thread notifyingThread = new Thread(null, mTask, "NotifyingService");
        notifyingThread.start();
    }

    private void postNotif() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = R.drawable.playstore_icon;
        Notification notification = new Notification(icon, "Firebase", System.currentTimeMillis());
        Context context = getApplicationContext();
        CharSequence contentTitle = "Social Movie Club";
        String notificationString = "Parties Have Been updated \n Click to View";
        Intent notificationIntent = new Intent(context, Splash.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, contentTitle, notificationString, contentIntent);
        mNotificationManager.notify(1, notification);
    }
}
