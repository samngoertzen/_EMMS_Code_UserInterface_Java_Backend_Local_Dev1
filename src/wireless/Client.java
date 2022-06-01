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

	private static final int VERBOSITY = 2; // Global variable for how much output we want. 0 = none, 1 = errors only, 2 = all output.


	private final int METER_TCP_PORT = 8001;
	private static final int TIMEOUT = 500;
	
	/**
	 * Polymorphed communicate function to use the default meter port.
	 * @author Bennett Andrews
	 * @param ip
	 * @param command
	 * @return Value of Command from WiFi board
	 */
	public String communicate( String ip, String command )
	{
		return communicate( ip, METER_TCP_PORT, command );
	}

	/**
	 * @apiNote Sends / Receives Command, Then Closes TCP Socket to Wifi Board
	 * @param ip
	 * @param port Should be 8001
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
			clientSocket.connect(new InetSocketAddress(ip, port), TIMEOUT);
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch ( SocketTimeoutException e0 )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "SocketTimeout" );
			}
			return "";
		}
		catch (IOException e1) 
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "IO Exception" );
			}
			return "";
		}
		//// END OPEN CONNECTION ////
		
		//// BEGIN SEND COMMAND ////
		String response = "No response";
		try 
		{
			if( VERBOSITY >= 2 )
			{
				System.out.println("Attempting to send command > " + command );	
			}
			response = sendMessage( command );
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
	private String sendMessage( String msg ) throws IOException 
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

		while ( in.ready() ) 
		{
			response += (char) in.read();
		}
		return response;
	}

	/**
	 * Closes Open TCP Connection. In Function to make more readable
	 * @author ZacheryHolsinger
	 * @throws IOException
	 */
	private void stopConnection() throws IOException 
	{
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
} 