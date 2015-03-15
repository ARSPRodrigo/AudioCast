/**Group Members**
 * E/11/054
 * E/11/067
 */

package audiocast.ui;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;
import audiocast.audio.Play;
import audiocast.audio.Record;
import audiocast.udp.Client;
import audiocast.udp.Server;
import co324.audiocast.R;

/** 
 * @author (C) ziyan maraikar
 */
public class AudiocastActivity extends Activity {
	final static int SAMPLE_HZ = 22050, BACKLOG = 8;
	
	protected PowerManager.WakeLock mWakeLock;
	
	Record rec; 
	Play play;
	Server server;
	Client client;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audiocast);
		
		//to keep the screen on
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();
		
		WifiManager wifi = (WifiManager)getSystemService( Context.WIFI_SERVICE );
		if(wifi != null){
		    WifiManager.MulticastLock lock = 
		    		wifi.createMulticastLock("Audiocast");
		    lock.setReferenceCounted(true);
		    lock.acquire();
		} else {
			Log.e("Audiocast", "Unable to acquire multicast lock");
			finish();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		BlockingQueue<byte[]> recbuf = new ArrayBlockingQueue<byte[]>(BACKLOG);
		BlockingQueue<byte[]> playbuf = new ArrayBlockingQueue<byte[]>(BACKLOG);
		
		rec = new Record(SAMPLE_HZ, recbuf);
		play = new Play(SAMPLE_HZ, playbuf);
		server = new Server(recbuf);
		client = new Client(playbuf);
		
		findViewById(R.id.Record).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					//recording and broadcasting
					rec.pause(!((ToggleButton)v).isChecked());
					if(((ToggleButton)v).isChecked()) Server.broadcast = true;
					else Server.broadcast = false;
					
					//receiving and playing
					play.pause(((ToggleButton)v).isChecked());
					if(!((ToggleButton)v).isChecked()) Client.receive=true;
					else Client.receive = false;
			}
		});
		/*
		findViewById(R.id.Play).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					play.pause(!((ToggleButton)v).isChecked());
					if(((ToggleButton)v).isChecked()) Client.receive=true;
					else Client.receive = false;
			}
		});	
		*/
		
		Log.i("Audiocast", "Starting all threads");
		rec.start();
		play.start();
		server.start();
		client.start();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		Log.i("Audiocast", "Stopping all threads");
		rec.interrupt();
		play.interrupt();
		server.interrupt();
		client.interrupt();
	}
	
	 @Override
	    public void onDestroy() {
	        this.mWakeLock.release();
	        super.onDestroy();
	    }
}


