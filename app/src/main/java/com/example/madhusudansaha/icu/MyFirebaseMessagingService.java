package com.example.madhusudansaha.icu;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessageService";
    Bitmap bitmap;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        //
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        //The message which i send will have keys named [message, image, AnotherActivity] and corresponding values.
        //You can change as per the requirement.

        //message will contain the Push Message
        String message = remoteMessage.getData().get("message");
        //imageUri will contain URL of the image to be displayed with Notification
        String imageUri = remoteMessage.getData().get("image");
        String buzz = remoteMessage.getData().get("buzz");
        String category = remoteMessage.getData().get("category");
        String severity = remoteMessage.getData().get("severity");
        String location = remoteMessage.getData().get("location");
        String remarks = remoteMessage.getData().get("remarks");
        String time = remoteMessage.getData().get("timestamp");
        Long t = Long.parseLong(time);
        time = (new SimpleDateFormat("hh:mm:ss")).format(new Date(t)) + ":";
        //If the key AnotherActivity has  value as True then when the user taps on notification, in the app AnotherActivity will be opened.
        //If the key AnotherActivity has  value as False then when the user taps on notification, in the app MainActivity will be opened.
        //String TrueOrFlase = remoteMessage.getData().get("AnotherActivity");

        //To get a Bitmap image from the URL received
        bitmap = getBitmapfromUrl(imageUri);
        Long timestamp = System.currentTimeMillis()/1000;
        String filename = "image" + timestamp.toString() + ".jpg";

        String path = saveToInternalStorage(bitmap, filename);

        sendNotification(message, path, buzz, category, severity, location, remarks, imageUri, time);
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     */

    private void sendNotification(String messageBody, String path, String buzz, String category, String severity, String location, String remarks, String uri, String time) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.putExtra("message", messageBody);
        intent.putExtra("image", path);
        intent.putExtra("category", category);
        intent.putExtra("severity", severity);
        intent.putExtra("location", location);
        intent.putExtra("remarks", remarks);
        intent.putExtra("uri", uri);
        intent.putExtra("time", time);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bitmap image = ImageRetrieve.loadImageFromStorage(path);

        Uri sound;
        boolean buzzTime = false;

        try{
            ContextWrapper cw = new ContextWrapper(getApplicationContext());

            File directory = cw.getDir("dir", Context.MODE_PRIVATE);

            File file=new File(directory, "buzz_time");

            Date lastModDate = new Date(file.lastModified());
            Log.d("Last modified : ", lastModDate.toString());

            Date currentDate = new Date();

            long diff = currentDate.getTime() - lastModDate.getTime();
            long seconds = diff / 1000;

            if(seconds >= 30) {
                buzzTime = true;
            }

        } catch (Exception e){
            e.printStackTrace();
        }



        if(buzz.equals("True") && buzzTime) {
            sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.buzzer);
        }
        else {
            sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(image)/*Notification icon image*/
                .setSmallIcon(R.drawable.icon_report_problem)
                .setContentTitle(messageBody)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(image))/*Notification with Image*/
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    /*
    *To get a Bitmap image from the URL received
    * */
    public Bitmap getBitmapfromUrl(String imageUrl) {
        final String user;
        final String password;

        try {
            if(imageUrl.contains("@")) {
                int userStartIndex = imageUrl.indexOf("//") + 2;
                int userEndIndex = imageUrl.indexOf(":", userStartIndex);
                int passwordStartIndex = userEndIndex + 1;
                int passwordEndIndex = imageUrl.indexOf("@");

                user = imageUrl.substring(userStartIndex, userEndIndex);
                password = imageUrl.substring(passwordStartIndex, passwordEndIndex);

                imageUrl.replace(user, "");
                imageUrl.replace(password, "");
                imageUrl.replace(":", "");
                imageUrl.replace("@", "");

                Authenticator.setDefault(new Authenticator(){
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password.toCharArray());
                    }});
            }

            URL url = new URL(imageUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String filename){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/images
        File directory = cw.getDir("images", Context.MODE_PRIVATE);
        // Create imageDir
        File path=new File(directory, filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.d("PATH", "Image path: " + path.getAbsolutePath());

        return path.getAbsolutePath();
    }

}
