package wireless;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Scans IP's AND Confirms it with EMMS Standard to see which meters are ours
 * @author Zachery Holsinger
 *
 */
public class meterScan {

	private Socket clientSocket;
	private static Object[] meterArrayNew;
	private PrintWriter out;
	private BufferedReader in;
	private ArrayList<String> scannedIps= new ArrayList<String>();
	private static ArrayList<String> confMeters = new ArrayList<String>();
	
	public static void main(String[] args) throws UnknownHostException {
		Object[] meters;
		meters = getMeters();
		System.out.println(meters.toString());
	}
	
	public static Object[] getMeters(){
		String ip;
		//			ip = InetAddress.getLocalHost().getHostAddress(); Only works for windows
					ip = SystemIp.getSystemIP();
				ip = ip.replace(".", "%");
				String[] ips = ip.split("%");
				System.out.println("My Current IP is: " + ips[0] + "." + ips[1] + "." + ips[2] + "." + ips[3]);
				String ipSubnet = ips[0] + "." + ips[1] + "." + ips[2] + ".";
				meterScan getIps = new meterScan(ipSubnet);
				
					return meterArrayNew;
	}
	
	public meterScan(String inputSubNet) {
		confMeters.clear();
		for(int i = 0; i <= 20; i++) { //ADJUST THIS ZACH 0 TO 255
			boolean isMeter = isIp(inputSubNet + i);
		}
		
//		System.out.println(scannedIps.toString());
		
		for( int i = 0; i < scannedIps.size(); i++) {
			boolean isMeter = isMeter(scannedIps.get(i));
		}
		
		System.out.println("Confirmed (" + confMeters.size() + ") Messiah Meters are: " + confMeters.toString());
		meterArrayNew = confMeters.toArray();
	}
	
	public boolean isIp(String subMask) {
//		System.out.println(subMask);
		clientSocket = new Socket();
		try {
			System.out.println( subMask + "Try");
			clientSocket.connect(new InetSocketAddress(subMask, 80), 200);
			clientSocket.setSoTimeout(1000);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//			System.out.println(subMask);
		} catch (IOException e) {
//			System.out.println( "Nothing here");
//			System.out.println(e);
			return false;
		}
		
		String line = null;
		try {
			while ((line = in.readLine()) != null ) {
//				System.out.println(subMask);
				scannedIps.add(subMask);
				in.close();
				out.close();
				clientSocket.close();
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			System.out.println(subMask);
			scannedIps.add(subMask);
			try {
				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Broke trying to close");
			}
			return true;
		}
		
//		System.out.println("We shouldnt be here");
		return false;
	}
	
	public boolean isMeter(String ip) {
//		System.out.println(subMask);
		clientSocket = new Socket();
		try {
			clientSocket.connect(new InetSocketAddress(ip, 80), 200);
			clientSocket.setSoTimeout(1000);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
//			System.out.println( "Nothing here");
			return false;
		}
		
		String line = null;
		try {
			while ((line = in.readLine()) != null ) {
				if (line.contains("MESSIAH COLLEGE COLLABORATORY")) {
//					System.out.println("Got: " + ip);
					confMeters.add(ip);
					try {
						in.close();
						out.close();
						clientSocket.close();
						} catch (IOException e) {
						System.out.println("Broke trying to close");
					}
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				in.close();
				out.close();
				clientSocket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("Broke trying to close");
			}
			return false;
		}
		
		
		try {
			in.close();
			out.close();
			clientSocket.close();
		} catch (IOException e) {
			System.out.println("Broke trying to close");
		}

		return false;
	}
}
