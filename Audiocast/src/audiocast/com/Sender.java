package audiocast.com;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import android.util.Log;

public class Sender extends Thread{
	private static MulticastSocket ms = null;
	private static InetAddress groupAddr;
	final static int MAXLEN = 1400;
	public static boolean broadcasting = false;
	final String host = "228.5.5.5";
	final int port = 4567;
	
	final BlockingQueue<byte[]> queue;
	
	public Sender(BlockingQueue<byte[]> queue){
		this.queue = queue;
		
		try {
			groupAddr = InetAddress.getByName(host);
			ms = new MulticastSocket(port);
			ms.joinGroup(groupAddr);
		} catch (UnknownHostException e){
			Log.e("Audiocast", "UnknownHostException in Sender construction");
		} catch (IOException e){
			Log.e("Audiocast", "IOException in Sender construction");
		}
		Log.d("Audiocast", "Sender construction success");
	}
	
	@Override
	public void run(){
		try{
			byte [] packet = new byte[MAXLEN];
			DatagramPacket dPacket;
			
			while(!Thread.interrupted()){
				packet = queue.take();
				dPacket = new DatagramPacket(packet, MAXLEN, groupAddr, port);
				try{
					if(broadcasting) ms.send(dPacket);
				} catch(IOException e){
					Log.e("Audiocast", "IOException in packet sending");
				}
			}
		} catch(InterruptedException e){
			Log.e("Audiocast", "InterruptedException in packet sending");
		} finally{
			try{
				ms.leaveGroup(groupAddr);
			} catch(IOException e){
				Log.e("Audiocast", "IOException in group leaving");
			}
		}
	}
}
