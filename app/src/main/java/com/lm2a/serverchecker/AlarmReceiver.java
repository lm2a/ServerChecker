package com.lm2a.serverchecker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class AlarmReceiver extends BroadcastReceiver {

    Context mCtx;

	@Override
	public void onReceive(Context arg0, Intent arg1) {
        mCtx = arg0;
		// For our recurring task, we'll just display a message
		Toast.makeText(arg0, "I'm running", Toast.LENGTH_SHORT).show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                    //boolean isAlive = InetAddress.getByName("www.lm2a.com").isReachable(60000);
                    boolean isAlive = isReachable("www.lm2a.de", 80, 60000);
                    if(isAlive){
                        Log.i("TAG", "lm2a Alive");
                    }else{
                        Log.i("TAG", "lm2a Dead");
                        //sendErrorReport("lm2a Dead");
                        showNotification("lm2a Dead");
                        //createNotification();
                    }

            }
        });
        thread.start();


	}

    private static boolean isReachable(String addr, int openPort, int timeOutMillis) {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            }
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public void sendErrorReport(final String report) {

        Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "ServerChecker Report");
        intent.putExtra(Intent.EXTRA_TEXT, report);
        intent.setData(Uri.parse("mailto:lamenza@gmail.com")); // or just "mailto:" for blank
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
        mCtx.startActivity(intent);



//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    GMailSender sender = new GMailSender("aadroidreports@gmail.com", "AAIr3l4nd");
//                    sender.sendMail("AA Android App Error Report",
//                            report,
//                            "aadroidreports@gmail.com",
//                            "aadroidreports@gmail.com");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        t.start();


    }

    public void createNotification() {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(mCtx, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mCtx, (int) System.currentTimeMillis(), intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(mCtx)
                .setContentTitle("New mail from " + "test@gmail.com")
                .setContentText("Subject").setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent).build();;
        NotificationManager notificationManager = (NotificationManager) mCtx.getSystemService(Service.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);

    }


    public void showNotification(String report){

        // define sound URI, the sound to be played when there's a notification
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // intent triggered, you can add other intent for other actions
        Intent intent = new Intent(mCtx, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mCtx, 0, intent, 0);

        // this is it, we'll build the notification!
        // in the addAction method, if you don't want any icon, just set the first param to 0
        Notification mNotification = new Notification.Builder(mCtx)

                .setContentTitle("Server Checker Report")
                .setContentText(report)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setSound(soundUri)

                .addAction(R.mipmap.ic_launcher, "View", pIntent)
                .addAction(0, "Remind", pIntent)

                .build();

        NotificationManager notificationManager = (NotificationManager) mCtx.getSystemService(Service.NOTIFICATION_SERVICE);

        // If you want to hide the notification after it was selected, do the code below
        // myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, mNotification);
    }

    public void cancelNotification(int notificationId){

        if (Context.NOTIFICATION_SERVICE!=null) {
            String ns = Context.NOTIFICATION_SERVICE;
            NotificationManager nMgr = (NotificationManager) mCtx.getApplicationContext().getSystemService(ns);
            nMgr.cancel(notificationId);
        }
    }
}


