package wireless;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 * Class handles all TCP connection items
 */

/**
 * @author Zachery Holsinger
 *
 */
public class tcpCommunication {
	// Settings for CTIE
	private String IPaddress;
	private String MAC = "";
	private int PorttoUse;

	// Settings for TCP communication
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private boolean ifConected = true;
	private int timeoutAttmps = 2;
	private int timeouts = 0;

	/**
	 * Class for all things TCP!
	 * 
	 * @param IP
	 * @param Port
	 */
	public tcpCommunication(String IP, int Port) {
		IPaddress = IP;
		PorttoUse = Port;
	}
	
	public tcpCommunication(String IP, int Port, String MAC) {
		IPaddress = IP;
		PorttoUse = Port;
		this.MAC = MAC;
	}
	
	

	public void startConnection() throws UnknownHostException, IOException {
		ExecutorService service = Executors.newSingleThreadExecutor();
//		System.out.println("Trying New Connection...");
		InetAddress inet = InetAddress.getByName(IPaddress);
//		System.out.println(IPaddress);
		if (inet.isReachable(3000)) {
			try {
//				System.out.println("Trying!");
				clientSocket = new Socket(IPaddress, PorttoUse);
				clientSocket.setSoTimeout(1500);
				out = new PrintWriter(clientSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				this.ifConected = true;
//				System.out.println("Sucess...");
			} catch (java.net.ConnectException e) {
				// Device will go here if on network but not hooked up to ctie
				Date d = new Date();
//				System.out.println("[" + d.toString() + "] " + this.IPaddress + " Connect Exception");
				e.printStackTrace();
				this.stopConnection();
				this.stopConnection();
				this.stopConnection();
				this.stopConnection();
				this.stopConnection();
				this.ifConected = false;
				
			}
		} else {
//			System.out.println(this.IPaddress + " is not reachable...");
			this.stopConnection();
			this.stopConnection();
			this.stopConnection();
			this.stopConnection();
			this.stopConnection();
			this.stopConnection();
			this.ifConected = false;
		}
	}

	public String sendMessage(String msg) throws IOException {
//		if (this.isConnected()) {
		timeouts++;
//		System.out.println(timeouts);
		if (timeouts >= timeoutAttmps) {
			this.ifConected = false;
//			System.out.println("Too many tries!");
			return "null";
		}
		try {
			this.startConnection();
			out.println(msg + "\r");
			String resp = in.readLine();
			this.stopConnection();
			this.ifConected = true;
			timeouts = 0;
			return resp;
		} catch (java.net.SocketTimeoutException e) {
			// this is for if device drops out first
			this.ifConected = false;
			while (!this.isConnected()) {
//				System.out.println("SocketTimeout");
				this.stopConnection();
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
//				System.out.println("Returning");
				break;
			}
			return sendMessage(msg);
		} catch (java.net.SocketException e) {
//			System.out.println("SocketException");
			// this is if for some reason the computer drops out first
			this.stopConnection();
			this.startConnection();
			this.stopConnection();
			return sendMessage(msg);
		} catch (java.io.IOException e) {
//				System.out.println("IO Fail...Stopping and Retrying");
//				System.out.println("Returning");
			try {
//				System.out.println("Back on the Network, waiting For Boot");
				TimeUnit.MILLISECONDS.sleep(30);
//				System.out.println("Re-establishing connection");
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return sendMessage(msg);
		}
//		} else {
		// this means they are sending info when not supposed too
//			throw new Exceptions.tcpConnectionNotOpened();
//		}
	}

	public void stopConnection() throws IOException {
		try {
				// Code isn't hanging here
				in.close();
				out.close();
				clientSocket.close();
		} catch (java.lang.NullPointerException e) {
//			System.out.println("BIG UH OH");

		}
	}

	public boolean isConnected() {
		return this.ifConected;
	}

}
