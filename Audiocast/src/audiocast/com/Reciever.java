/* 
 * E/11/343 Rodrigo ARSP
 * E/11/237 Lankathilaka YMPN
 */

package audiocast.com;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;

import android.util.Log;

public class Reciever extends Thread{
	private static MulticastSocket ms = null;
	private static InetAddress groupAddr;
	final static int MAXLEN = 1400;
	public static boolean receiving = false;
	final String host = "228.5.5.5";
	final int port = 6800;
	final BlockingQueue<byte[]> queue;
	
	public Reciever(BlockingQueue<byte[]> queue) {
		this.queue = queue;
		
		try {
			groupAddr = InetAddress.getByName(host);
			ms = new MulticastSocket(port);
			ms.joinGroup(groupAddr);
		} catch (UnknownHostException e){
			Log.e("Audiocast", "UnknownHostException in Receiver construction");
		} catch (IOException e){
			Log.e("Audiocast", "IOException in Receiver construction");
		}
		Log.d("Audiocast", "Sender Receiver success");
	}
	
	@Override
	public void run(){
		try{
			byte [] packet = new byte[MAXLEN];
			DatagramPacket dPacket;
			
			while(!Thread.interrupted()){
				dPacket = new DatagramPacket(packet, MAXLEN);
				try{
					if(receiving) ms.receive(dPacket);
					packet = dPacket.getData();
					queue.put(packet);
				} catch(IOException e){
					Log.e("Audiocast", "IOException in packet receiving");
				}
			}
		} catch(InterruptedException e){
			Log.e("Audiocast", "InterruptedException in packet receiving");
		} finally{
			try{
				ms.leaveGroup(groupAddr);
			} catch(IOException e){
				Log.e("Audiocast", "IOException in group receiving");
			}
		}
	}
}
