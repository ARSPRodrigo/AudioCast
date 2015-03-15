/**Group Members**
 * E/11/054
 * E/11/067
 */

package audiocast.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

import android.util.Log;

public class Client extends Thread{
	private static MulticastSocket s = null;
	private static InetAddress group;
    final static int MAXLEN = 1024;
    public static boolean receive = false;
    
    InetAddress aHost;
    
    final BlockingQueue<byte[]> queue;
    
    public Client(BlockingQueue<byte[]> queue){
    	this.queue = queue;
    	
    	try {
    		 group = InetAddress.getByName("228.5.6.7");
    		 s = new MulticastSocket(6789);
    		 s.joinGroup(group);
		} catch (UnknownHostException e) {
			Log.e("Audiocast", "UnknownHostException at srver construction");
		} catch (IOException e) {
			Log.e("Audiocast", "IOException at srver construction");
		}
    	Log.d("Audiocast", "server costruction success.");
    }
    
    @Override
	public void run() {
		try {
			byte[] pkt = new byte[MAXLEN];
			DatagramPacket DGpkt;
			
			while (!Thread.interrupted()) {	
				DGpkt = new DatagramPacket(pkt, MAXLEN);
				try {
					if(receive) s.receive(DGpkt);
					pkt = DGpkt.getData();
					queue.put(pkt);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
		} finally {
			try {
				s.leaveGroup(group);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

    }
}
