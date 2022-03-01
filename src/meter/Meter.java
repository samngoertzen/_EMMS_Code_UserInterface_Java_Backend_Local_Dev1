package meter;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

import database.dbConnection;
import wireless.Client;

/**
 * @author ZacheryHolsinger
 */
public class Meter 
{
	// Number of times a command should be sent to receive a response before giving up.
	private static final int SEND_ATTEMPTS = 3;

	private static final String CURRENT_CENTURY = "20"; // i.e. 20 is for years 2000 to 2099.

	// Stores all meter object data relevant to live meters.
	HashMap<InfoGET, String> data =  new HashMap<InfoGET, String>();



	/**
	 * Make a new instance of a Meter
	 * @param ip Ip Address of the Meter
	 * @throws Exception Will throw if Meter is not found @ IP
	 */
	public Meter(String ip) throws Exception 
	{
		// First thing we do is check and see if the meter is on the network
		// Is the meter connected?
		if( !isConnected( ip ) )
		{
			Exception e = new Exception("Meter not connected.");
			throw e;
		}

		// Next, set default data in the data map. This is primarily
		// to initialize the map, not to set valid values.
		defaultData();

		data.put( InfoGET.IP_address, ip );
	}




	/**
	 * Writes default values to the data map. Primarily for initialization,
	 * but also convenient for debugging.
	 * @author Bennett Andrews
	 */
	private void defaultData()
	{
		for( InfoGET datum : InfoGET.values() )
		{
			data.put( datum, "9");
		}
	}

	/**
	 * Getter for meter information.
	 * @author Bennett Andrews
	 * @return Returns the specified InfoGET field of this meter.
	 */
	public String getDatum( InfoGET field )
	{
		return data.get( field );
	}

	/**
	 * Setter for meter information.
	 * @author Bennett Andrews
	 */
	public void setDatum( InfoGET field, String value )
	{
		data.put( field, value );
	}

	/**
	 * Macro for getting Meter_id because it is used frequently.
	 * @author Bennett Andrews
	 * @return This Meter_id
	 */
	public String id()
	{
		return getDatum( InfoGET.Meter_id );
	}

	/**
	 * Macro for getting IP_address because it is used frequently.
	 * @author Bennett Andrews
	 * @return
	 */
	public String ip()
	{
		return getDatum( InfoGET.IP_address );
	}





	/**
	 * Runs the main functions of the meter. 
	 * <p>1) Updates meter object data by fetching information from live meters.
	 * <p>2) Pushes updates to the database.
	 * <p>3) Fetches commands from the action table and pushes them to the live meters.
	 * @author Bennett Andrews
	 * @return true/false - run completed successfully/unsuccessfully
	 */
	public boolean run()
	{
		System.out.println("\n\nRunning meter " + id() );

		try 
		{
			update();
			pushAllInDB();
			pushCommands();
			return true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("! Run failed.");
			return false;
		}
	}





	/**
	 * Fetches meter commands from the Action table in the database
	 * and pushes them to live meters.
	 * @author Bennett Andrews
	 */
	private void pushCommands()
	{
		System.out.println("\n\nPushing commands.\n\n");

		Client client = new Client();
		

		// Fetch all the commands for a specific meter
		String [][] command_list = dbConnection.getCommandsForMeter( id() );


		for ( String[] commandset : command_list ) 
		{
			// commandset = [action_index, meter_id, command]
			String action_index = commandset[0];
			String command = commandset[2];

			String response = "";

			dbConnection.logSendAttempt( action_index );

			try 
			{
				for( int i = 0; i < SEND_ATTEMPTS; i++ )
				{
					System.out.println("Sending command " + command + " to meterid " + id() );
					response = client.communicate( ip() , command );

					if( response != "" ) break;	// Stop resending commands if we get a response
				}
				
				parseResponse( response );

				dbConnection.logSuccess(action_index);
				
			} 
			catch( Exception e ) 
			{
				System.out.println("command send error");
			}	

			// Wait 300 ms then move to the next command.
			try 
			{
				Thread.sleep( 300 );
			} 
			catch( InterruptedException e ) 
			{
				e.printStackTrace();
			}
		}

		client.close();
	}





	/**
	 * Update all values stored in the data map to the database.
	 * @author Bennett Andrews
	 * @param field
	 * @param value
	 * @apiNote *!IMOPORTANT!* - This function solely relies on the fact 
	 * that InfoSET and InfoGET are in the same order! Otherwise, Java can't convert
	 * between enums.
	 */
	public void pushAllInDB()
	{
		for( InfoGET field : InfoGET.values() )
		{
			String value = getDatum( field );
			
			if( (value != "") && (value != null) )
			{
				System.out.println("DB - Setting " + field + " to " + value );
				dbConnection.setTo( value, field, id() );
			}	
		}

		updateTimestampInDB();
	}




	
	/**
	 * Updates the meter object with live meter information.
	 * @param data
	 * @param value
	 * @return
	 */
	public void update() throws IOException
	{
		System.out.println("Updating Meter_id: " + id() );

		// is the meter online?
		if( !isConnected() )
		{
			throw new IOException("Meter is not connected.");
		}

		// is the meter registered in the database?
		boolean meterInSystem = dbConnection.isMeterInDB( id() );
		System.out.println("Is meter in system? >" + meterInSystem);

		if(!meterInSystem)
		{
			addMeterInDB();
		}

		updateDatum( InfoGET.Meter_password );
		updateDatum( InfoGET.Meter_time );
		updateDatum( InfoGET.Energy_allocation );
		updateDatum( InfoGET.Energy_allocation_reset_time );
		updateDatum( InfoGET.Energy_used );
		updateDatum( InfoGET.Power_failure_last );
		updateDatum( InfoGET.Alarm_audible ); 				// Updates ALL Alarm values
		updateDatum( InfoGET.Emergency_button_enabled );
		updateDatum( InfoGET.Relay );
		updateDatum( InfoGET.Firmware_version_WiFi_board );
		updateDatum( InfoGET.Firmware_version_command_board );
		updateDatum( InfoGET.Energy_used_lifetime );		// Updates all Stats values

		// TODO: Problems!

		// Energy_remaining
		// Energy_time_remaining
		// Power_failure_last
		// ???? Power_restore
		// Debug
		// Lights
		// Wifi board firmware version
		// Online last
		// Meter id

		setDatum( InfoGET.Online, "1" );
	}

	/**
	 * Tests to see if configured Database is active
	 * 
	 * Already Done in dbConnection Code, this is an extension
	 */
	public boolean testDBConnection()
	{
		boolean isConnected = dbConnection.testConnection();
		return isConnected;
	}

	/**
	 * Adds meter into DB with relevant information!
	 * @author Bennett Andrews
	 */
	private boolean addMeterInDB()
	{
		dbConnection.insertMeter( id() );
		return false; 
	}

	/**
	 * Deletes this meter from the database.
	 * @author Bennett Andrews
	 */
	public void removeMeterInDB()
	{
		dbConnection.deleteMeter( id() );
	}

	/**
	 * Tells the database that the meter was accessed and sets Last_update
	 * to the current time.
	 * @author Bennett Andrews
	 */
	public void updateTimestampInDB()
	{
		dbConnection.meterTimestamp( id() );
	}

	/**
	 * Flags a meter as disconnected in the database.
	 * @author Bennett Andrews
	 */
	public void setOfflineInDB()
	{
		dbConnection.setTo( "0", InfoGET.Online, id() );
	}

	/**
	 * Polymorphed version of isConnected. Defaults to use "this" object ipv4.
	 * @author Bennett Andrews
	 * @param ipv4 - IPV4 address of the meter to be pinged
	 * @return true/false - is_connected/is_not_connected
	 */
	private boolean isConnected()
	{
		return isConnected( getDatum( InfoGET.IP_address ) );
	}

	/**
	 * Tests whether a meter is online at the specified IPV4 address.
	 * @author Bennett Andrews
	 * @param ipv4 - IPV4 address of the meter to be pinged
	 * @return true/false - is_connected/is_not_connected
	 */
	public static boolean isConnected( String ipv4 )
	{
		boolean is_meter = false;

		Client client = new Client();
		String response = "";

		// Try to confirm that it is a meter for SEND_ATTEMPTS times
		for( int i = 0; i < SEND_ATTEMPTS; i++ )
		{
			response = ""; // reset response
			response = client.communicate( ipv4, "!Read;CBver$905*"); // TODO get real ping command
			System.out.println( "Response > " + response );

			if( response.equals("!Set;CBver;20190929$1300*") ) // TODO get better meter verification condition.
			{
				is_meter = true;
			}
			// else, it is not a meter and the default false carries through
		}

		client.close();
		return is_meter;
	}





	/**
	 * Builds a meter command from the given arguments.
	 * @author Bennett Andrews
	 * @apiNote Vararg function
	 * @param params Parameters to be converted to a command. Must be listed in order of the final command.
	 * @return Completed command
	 */
	private static String commandBuilder(String... params)
	{
		String command = "";

		command += Checksum.START_DELIMETER;

		for( String param : params )
		{
			command += param + ";";
		}

		// replace last ';' with '*' - i.e. !Set;EnAl;3000; to !Set;EnAl;3000*
		command = command.substring(0, command.length() - 1);
		command += Checksum.STOP_DELIMETER;

		return Checksum.convert(command);
	}

	/**
	 * Parses responses received from the live meters. If any action needs to be
	 * taken after interpreting the response, the action is taken. 
	 * <p>e.g. update
	 * meter object data
	 * @author Bennett Andrews
	 * @param response The string to be parsed
	 */
	private void parseResponse( String response )
	{
		if( !Checksum.isVerified( response ) )
		{
			System.out.println("Ignoring invalid response.");
			return;
		}

		System.out.println("Response >> " + response );

		// Removes start delimeter and everything after (including) the checksum delimeter.
		response = response.substring(1, response.indexOf( Checksum.CHECKSUM_DELIMETER , 0) );
		
		String[] params = response.split( Checksum.ARG_DELIMETER );

		if( params[0].equals( "Set" ) )
		{
			parseSetResponse(params);
		}
		else if( params[0].equals( "Conf" ) )
		{
			// TODO send final confirm.
			System.out.println("Confirmation received.");
		}
		else
		{
			System.out.println("Parser unable to find command type.");
		}
	}

	/**
	 * Helper function to parseResponse(). Parses a set of parameters
	 * determined to be a Set command. Assumes that the command is verified and
	 * already broken into valid parameters.
	 * @author Bennett Andrews
	 * @param params String array of parameters generated by parseResponse()
	 * @see Meter.parseResponse()
	 */
	public void parseSetResponse( String[] params )
	{
		System.out.println("Parsing params: " + Arrays.toString( params ) );

		try
		{
			switch( params[1] )
			{
				case "CBver":
					setDatum( InfoGET.Firmware_version_command_board, params[2] );
					break;

				case "Pass":
					setDatum( InfoGET.Meter_password, params[2] );
					break;

				case "Time":
					setDatum( InfoGET.Meter_time, 
							  String.format("%s %s", convertDDMMYYtoYYYYMMDD(params[2]), params[3] ) );
					break;

				case "EnAl":
					setDatum( InfoGET.Energy_allocation, params[2] );
					break;

				case "RstTim":
					setDatum( InfoGET.Energy_allocation_reset_time, 
							// Note that this throws SQL errors for datetime format if there are 
							// !=2 digits in the hour or minute parameters.
					          String.format("1111-11-11 %s:%s:00", params[2], params[3]) );
					break;

				case "PwrFail":
					// TODO - Not working
					break;

				case "Alarm":
					setDatum( InfoGET.Alarm_audible, (params[2].equals("On")) ? "1" : "0" );
					setDatum( InfoGET.Alarm_one_enabled, (params[3].equals("On")) ? "1" : "0" );
					setDatum( InfoGET.Alarm_one_thresh, params[4] );
					setDatum( InfoGET.Alarm_two_enabled, (params[5].equals("On")) ? "1" : "0" );
					setDatum( InfoGET.Alarm_two_thresh, params[6] );
					break;

				case "Emer":
					setDatum( InfoGET.Emergency_button_enabled, (params[2].equals("On")) ? "1" : "0" );
					setDatum( InfoGET.Emergency_button_allocation, params[3] );
					break;

				case "Lights": // NOT VALID
					setDatum( InfoGET.Lights_enabled, (params[2].equals("On")) ? "1" : "0" );
					break;

				case "Relay":
					setDatum( InfoGET.Relay, params[2].equals("On") ? "1" : (params[2].equals("Off") ? "0" : "2") );
					// i know, nested ternary operators are stupid.
					break;

				case "PwrData":
					setDatum( InfoGET.Emergency_buffer, params[2] );
					setDatum( InfoGET.Energy_used, params[3] );
					setDatum( InfoGET.Energy_load, params[4] );
					break;

				case "Stat":
					setDatum( InfoGET.Energy_used_lifetime, params[2] );
					setDatum( InfoGET.Energy_used_previous_day, params[3] );
					break;

				default:
					break;
			}
		}
		catch( NullPointerException e )
		{
			System.out.println("Invalid Set command.");
		}

		return;
	}


	/**
	 * Converts the DD-MM-YY format from the received meter time to the 
	 * YYYY-MM-DD format required by the datetime MySQL entry. Returns 
	 * null if the format is incorrect.
	 * @author Bennett Andrews
	 * @param mmddyy
	 * @return
	 */
	public static String convertDDMMYYtoYYYYMMDD( String mmddyy )
	{
		if( (mmddyy == "") || (mmddyy == null) )
		{
			System.out.println("Time-conversion: Time is null");
			return "";
		}

		String[] pieces = mmddyy.split("-");

		if( pieces.length != 3 )
		{
			System.out.println("Time-conversion: Incorrect number of parameters.");
			return "";
		}

		return String.format( (CURRENT_CENTURY + "%s-%s-%s"), pieces[2], pieces[1], pieces[0] );
	}



	/**
	 * Helper function to update a specific field of the meter 
	 * object with live meter data.
	 * @author Bennett Andrews
	 * @param field - InfoGET field to be updated
	 */
	private void updateDatum( InfoGET field )
	{
		String datum = "";

		switch( field )
		{
			case Meter_id:
				datum = "";
				break;

			case Meter_password:
				datum = "Pass";
				break;
			
			case Meter_time:
				datum = "Time";
				break;
			
			case Energy_allocation:
				datum = "EnAl";
				break;
			
			case Energy_allocation_reset_time:
				datum = "RstTim";
				break;
			
			case Energy_used:
				datum = "PwrData";
				break;

			case Energy_load:
				datum = "PwrData";
				break;
			
			case Power_failure_last:
				datum = "PwrFail";
				break;
			
			case Alarm_audible:
				datum = "Alarm";
				break;
			
			case Alarm_one_enabled:
				datum = "Alarm";
				break;

			case Alarm_one_thresh:
				datum = "Alarm";
				break;

			case Alarm_two_enabled:
				datum = "Alarm";
				break;

			case Alarm_two_thresh:
				datum = "Alarm";
				break;

			case Emergency_buffer:
				datum = "Emer";
				break;

			case Emergency_button_enabled:
				datum = "Emer";
				break;

			case Emergency_button_allocation:
				datum = "Emer";
				break;

			case Relay:
				datum = "Relay";
				break;

			case Firmware_version_command_board:
				datum = "CBver";
				break;

			case Energy_used_previous_day:
				datum = "Stat";
				break;

			case Energy_used_lifetime:
				datum = "Stat";
				break;

			default:
				System.out.println("InfoGET value not available for update.");
				break;
		}

		if( datum != "" )
		{
			String command = commandBuilder( "Read", datum );

			Client client = new Client();
			String response = "";

			for( int i = 0; i < SEND_ATTEMPTS; i++ )
			{
				System.out.println("\nSending to id: " + id() + " >> " + command );

				response = client.communicate( ip(), command );
				
				if( response != "" )
				{
					break;
				}
			}
			
			System.out.println("Received: " + response );
			parseResponse( response );
		}
	}
}
