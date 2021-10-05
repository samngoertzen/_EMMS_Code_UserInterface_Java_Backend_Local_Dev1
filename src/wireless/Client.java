package wireless;
/**
 *  A Java program for a Client-based connections 
 *  More individual, lowest-level
 */
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.io.*; 

public class Client
{ 
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private String timeOut = null;
	
	/**
	 * @apiNote Sends / Receives Command, Then Closes TCP Socket to Wifi Board
	 * @param ip
	 * @param port Should be 80
	 * @param command 
	 * @returns Value of Command from Wifi Board
	 * @author ZacheryHolsinger
	 */
	public String Communicate(String ip, int port, String command) {
		clientSocket = new Socket();
		//// BEGIN OPEN CONNECTION ////
		try {
			clientSocket.connect(new InetSocketAddress(ip, port), 4000);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			return "NoDev";
		}
		//// END OPEN CONNECTION ////
		
		//// BEGIN SEND COMMAND ////
		String response = "NODev";
		try {
			response = sendMessage(command);
		} catch (IOException e) {
			return timeOut ;
		}
		//// END SEND COMMAND ////
				
		//// BEGIN CLOSE CONNECTION ////
		try {
			stopConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//// END CLOSE CONNECTION ////
		
		return response;
	} 

	/**
	 * Sends message and gets response back from Wifi Board
	 * @param msg
	 * @return Response from Wifi Board
	 * @throws IOException
	 * @author ZacheryHolsinger
	 * @apiNote Private Function, Assumes Connection already open
	 */
	private String sendMessage(String msg) throws IOException {
		out.println(msg);
		String line = "";
		while ((line = in.readLine()) != null) {
			if (line.contains("\n")) {
				break;
			}
			System.out.println("Got: " + line);
			break;
		}
		return line;
	}

	/**
	 * Closes Open TCP Connection. In Function to make more readable
	 * @author ZacheryHolsinger
	 * @throws IOException
	 */
	private void stopConnection() throws IOException {
		in.close();
		out.close();
		clientSocket.close();
	} 
	
	/**
	 * Used to 'Ping' Wifi Board
	 * @apiNote Wrapper for connSucc why did I make it so hard to understand good lord
	 * @author ZacheryHolsinger
	 * @deprecated No Longer Used
	 * @return
	 */
	private String attemptConnect() {
		String line = "";
		try {
			line = connSucc();
		} catch (IOException e) {
			return timeOut;
		}
		return line;
	}
	
	/**
	 * Same as attemptConnect with with print statements (Hence the Succ) to succ you in
	 * @deprecated Confirmed working 10/5/2021
	 * @author ZacheryHolsinger
	 * @return
	 * @throws IOException
	 */
	private String connSucc() throws IOException {
		String line = "";
		while ((line = in.readLine()) != null) {
			System.out.println("Got: " + line);
			break;
		}
		return line;
	}

} 