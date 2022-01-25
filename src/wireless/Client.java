package wireless;
/**
 *  A Java program for a Client-based connections 
 *  More individual, lowest-level
 */
import java.net.*;
import java.io.*; 

public class Client
{ 
	private Socket clientSocket;
	private PrintWriter out;
	private BufferedReader in;
	private String timeOut = null;

	private static final int TIMEOUT = 4000;
	
	/**
	 * @apiNote Sends / Receives Command, Then Closes TCP Socket to Wifi Board
	 * @param ip
	 * @param port Should be 80
	 * @param command 
	 * @returns Value of Command from Wifi Board
	 * @author ZacheryHolsinger
	 */
	public String communicate(String ip, int port, String command) 
	{
		clientSocket = new Socket();

		//// BEGIN OPEN CONNECTION ////
		try 
		{
			System.out.println("Opening socket.");
			clientSocket.connect(new InetSocketAddress(ip, port), TIMEOUT);
			System.out.println("Socket open.");
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			System.out.println("Buffered reader and writer created.");
		}
		catch ( SocketTimeoutException e0 )
		{
			System.out.println( "SocketTimeout" );
			return "";
		}
		catch (IOException e1) 
		{
			System.out.println( "IO Exception" );
			return "";
		}
		//// END OPEN CONNECTION ////
		
		//// BEGIN SEND COMMAND ////
		String response = "No response";
		try 
		{
			response = sendMessage(command);
		} 
		catch (IOException e) 
		{
			return timeOut;
		}
		//// END SEND COMMAND ////
				
		//// BEGIN CLOSE CONNECTION ////
		try 
		{
			stopConnection();
		} 
		catch (IOException e) 
		{
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
	private String sendMessage(String msg) throws IOException 
	{
		out.println(msg);

		// Wait a while to ensure buffer is fully built before trying to read it.
		try
		{
			Thread.sleep( TIMEOUT );
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		String response = "";
		System.out.println("in ready? " + in.ready() );

		while ( in.ready() ) 
		{
			response += (char) in.read();

			if (response.contains("*"))
			{
				break;
			}
		}

		System.out.println("Got: " + response);
		return response;
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
	 * Public method for closing connections. Handles exceptions.
	 * Probably a duplicate of Zach's stopConnection() method,
	 * but his private method throws exceptions instead of handling them.
	 * @author Bennett Andrews
	 * @return
	 */
	public void close()
	{
		try
		{
			if( in != null )
			{
				in.close();
			}

			if( out != null )
			{
				out.close();
			}

			if( clientSocket != null )
			{
				clientSocket.close();
			}
		}
		catch( IOException e )
		{
			e.printStackTrace();
			System.exit( -1 );
		}
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