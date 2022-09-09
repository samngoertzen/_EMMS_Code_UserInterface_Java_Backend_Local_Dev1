package meter;

import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
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
	private static final int SEND_ATTEMPTS = 2;

	private static final String CURRENT_CENTURY = "20"; // i.e. 20 is for years 2000 to 2099.

	public static final String DEFAULT_DATUM = "NODATA"; // data placeholder, overriden by actual meter data

	// Stores all meter object data relevant to live meters.
	HashMap<InfoGET, String> data =  new HashMap<InfoGET, String>();

	private static final int VERBOSITY = 0; // Global variable for how much output we want. 0 = none, 1 = errors only, 2 = all output.



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
		initializeDefaultData();

		data.put( InfoGET.IP_address, ip );
	}




	/**
	 * Writes default values to the data map. Primarily for initialization,
	 * but also convenient for debugging.
	 * @author Bennett Andrews
	 */
	private void initializeDefaultData()
	{
		for( InfoGET datum : InfoGET.values() )
		{
			data.put( datum, DEFAULT_DATUM );
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
		if( VERBOSITY >= 2 )
		{
			System.out.println("setDatum: Setting " + field + " to " + value );
		}
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
		if( VERBOSITY >= 2 )
		{
			System.out.println("\n\nRunning meter " + id() );
		}

		try 
		{
			pushCommands();
			update();
			pushAllInDB();
			return true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();

			if( VERBOSITY >= 1 )
			{
				System.out.println("! Run failed.");
			}

			setDatum( InfoGET.Online, "0" );
			return false;
		}
	}





	/**
	 * Fetches meter commands from the Action table in the database
	 * and pushes them to live meters.
	 * @author Bennett Andrews
	 * @throws IOException if the meter is missing under the isConnected() method.
	 */
	private void pushCommands() throws IOException
	{
		// is the meter online?
		if( !isConnected() )
		{
			throw new IOException("Meter is not connected.");
		}


		Client client = new Client();

		// Sends meter time command as a test and to keep meter clocks correct.
		if( VERBOSITY >= 2 )
		{
			System.out.println("\n\nUpdating time.\n\n");
		}

		parseResponse( client.communicate( ip(), createMeterDateTimeCommand() + createMeterDateTimeCommand() ) );

		if( VERBOSITY >= 2 )
		{
			System.out.println("\n\nPushing commands.\n\n");
		}

		// Fetch all the commands for a specific meter
		String [][] command_list = dbConnection.getCommandsForMeter( id() );


		for ( String[] commandset : command_list ) 
		{
			// commandset = [action_index, meter_id, command]
			String action_index = commandset[0];
			String command = commandset[2];
			String doubled_command = command + command;

			String response = "";

			dbConnection.logSendAttempt( action_index );

			try 
			{
				for( int i = 0; i < SEND_ATTEMPTS; i++ )
				{
					if( VERBOSITY >= 2 )
					{
						System.out.println("Sending command " + doubled_command + " to meterid " + id() );
					}
					response = client.communicate( ip() , doubled_command );

					if( response != "" ) break;	// Stop resending commands if we get a response
				}
				
				parseResponse( response );

				dbConnection.logSuccess(action_index);
				
			} 
			catch( Exception e ) 
			{
				if( VERBOSITY >= 1 )
				{
					System.out.println("command send error");				
				}
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
	 * between enums.
	 */
	public void pushAllInDB()
	{
		// is the meter registered in the database?
		boolean meterInDB = dbConnection.isMeterInDB( id() );

		if( VERBOSITY >= 2 )
		{
			System.out.println("Is meter in the database already? > " + meterInDB);
		}

		if( !meterInDB )
		{
			addMeterInDB();
		}

		for( InfoGET field : InfoGET.values() )
		{
			String value = getDatum( field );
			
			if( (value != "") &&
			    (value != null) && 
				(value != DEFAULT_DATUM ) )
			{
				if( VERBOSITY >= 2 )
				{
					System.out.println("pushDB -> Setting " + field + " to " + value );
				}
				dbConnection.setTo( value, field, id() );
			}	
		}

		updateTimestampInDB();
	}




	
	/**
	 * Updates the meter object with live meter information.
	 * @author Bennett Andrews
	 * @throws IOException if the meter is missing under the isConnected() method.
	 */
	public void update() throws IOException
	{
		if( VERBOSITY >= 2 )
		{
			System.out.println("UPDATING datum on Meter_id: " + id() );
		}

		// is the meter online?
		if( !isConnected() )
		{
			throw new IOException("Meter is not connected.");
		}

		updateDatum( InfoGET.Meter_id ); // important, do first.

		if( id().equals( DEFAULT_DATUM ) )
		{
			throw new IOException("Could not fetch Meter ID!");
		}

		updateDatum( InfoGET.Meter_name );
		updateDatum( InfoGET.Meter_password );
		updateDatum( InfoGET.Meter_time );
		updateDatum( InfoGET.Energy_allocation );
		updateDatum( InfoGET.Energy_allocation_reset_time );
		updateDatum( InfoGET.Energy_used_since_reset );		// Updates ALL Energy Data values
		updateDatum( InfoGET.Power_failure_last );			// Updates ALL Power failure values
		updateDatum( InfoGET.Alarm_audible ); 				// Updates ALL Alarm values
		updateDatum( InfoGET.Emergency_button_enabled );	// Updates ALL Emergency Button values
		updateDatum( InfoGET.Relay );
		updateDatum( InfoGET.Energy_used_lifetime );		// Updates all Stats values

		updateDatum( InfoGET.ModInfo00 );
		updateDatum( InfoGET.ModInfo01 );
		updateDatum( InfoGET.ModInfo02 );
		updateDatum( InfoGET.ModInfo03 );
		updateDatum( InfoGET.ModInfo04 );

		updateDatum( InfoGET.ModInfo10 );
		updateDatum( InfoGET.ModInfo11 );
		updateDatum( InfoGET.ModInfo12 );
		updateDatum( InfoGET.ModInfo13 );
		updateDatum( InfoGET.ModInfo14 );

		updateDatum( InfoGET.ModInfo20 );
		updateDatum( InfoGET.ModInfo21 );
		updateDatum( InfoGET.ModInfo22 );
		updateDatum( InfoGET.ModInfo23 );
		updateDatum( InfoGET.ModInfo24 );

		updateDatum( InfoGET.ModInfo30 );
		updateDatum( InfoGET.ModInfo31 );
		updateDatum( InfoGET.ModInfo32 );
		updateDatum( InfoGET.ModInfo33 );
		updateDatum( InfoGET.ModInfo34 );

		updateDatum( InfoGET.ModInfo40 );
		updateDatum( InfoGET.ModInfo41 );
		updateDatum( InfoGET.ModInfo42 );
		updateDatum( InfoGET.ModInfo43 );
		updateDatum( InfoGET.ModInfo44 );

		updateDatum( InfoGET.ModInfo50 );
		updateDatum( InfoGET.ModInfo51 );
		updateDatum( InfoGET.ModInfo52 );
		updateDatum( InfoGET.ModInfo53 );
		updateDatum( InfoGET.ModInfo54 );

		setDatum( InfoGET.Online, "1" );
	}

	/**
	 * Creates a fully constructed command for setting the date and time
	 * of an energy meter. Uses the current date and time set on the raspberry
	 * pi.
	 * 
	 * @author Bennett Andrews
	 * @return Fully constructed command for setting the date/time of this meter
	 */
	private static String createMeterDateTimeCommand()
	{
		String command = "!Set;Time;";

		Date date = new Date( System.currentTimeMillis() );
		SimpleDateFormat formatter = new SimpleDateFormat( "dd-MM-yy';'HH:mm:ss" );

		command += formatter.format( date ) + '*';
		command = Checksum.convert( command );

		return command;
	}

	public static void main( String[] args )
	{
		createMeterDateTimeCommand();
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
			// If we find a confirmed meter, we don't need to keep sending confirmation attempts.
			if( is_meter == true )
			{
				break; 
			}

			response = ""; // reset response every loop

			String command = commandBuilder( "Mod;Read;WiFiMAC" );
			String doubled_command = command + command;

			response = client.communicate( ipv4, doubled_command );

			if( VERBOSITY >= 2 )
			{
				System.out.println( "Online Check: > " + response );
			}

			String[] response_list = Checksum.separateMultipleCommands( response );

			for( String response_item : response_list )
			{
				if( VERBOSITY >= 2 )
				{
					System.out.println("Online Check: Single Command > " + response_item );
				}

				if( Checksum.isVerified( response_item ) ) // If we get a verified command, this is a meter!
				{
					is_meter = true;
					break;				// After we find  a good response, we don't need to check the other commands.
				}
				else
				{
					continue;			// it is not a meter and the default false carries through
				}
			}
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
	private void parseResponse( String responseString )
	{
		boolean successful_parse = false; // TODO implement this (return whether a command was successful or not)

		String[] responses = Checksum.separateMultipleCommands( responseString );

		if( VERBOSITY >= 2 )
		{
			System.out.println( Arrays.deepToString( responses ) );
		}

		for( String response : responses )
		{
			if( VERBOSITY >= 2 )
			{
				System.out.println( "Parser: Command found >> " + response );
			}

			if( !Checksum.isVerified( response ) )
			{
				if( VERBOSITY >= 1 )
				{
					System.out.println("Parser: Ignoring invalid response.");
				}
				continue;
			}

			if( response.equals("!Conf;ModInfo$1133*") )
			{
				if( VERBOSITY >= 2 )
				{
					System.out.println("Parser: Ignoring default Conf message.");
				}
				continue;
			}

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
				if( VERBOSITY >= 2 )
				{
					System.out.println("Parser: Confirmation received.");
				}
			}
			else
			{
				if( VERBOSITY >= 2 )
				{
					System.out.println("Parser: Unable to find command type.");
				}
			}
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
		if( VERBOSITY >= 2 )
		{
			System.out.println("Parsing params: " + Arrays.toString( params ) );
		}

		try
		{
			switch( params[1] )
			{
				case "MName":
					setDatum( InfoGET.Meter_name, params[2] );
					break;

				case "WiFiMAC":
					setDatum( InfoGET.Meter_id, params[2] );
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
					setDatum( InfoGET.Power_failure_last, convertPowerFailureTime( params[2] ) );
					setDatum( InfoGET.Power_restore_last, convertPowerFailureTime( params[3] ) );
					break;

				case "Alarm":
					setDatum( InfoGET.Alarm_audible, (params[2].equals("On")) ? "1" : "0" );
					setDatum( InfoGET.Alarm_one_thresh, params[3] );
					setDatum( InfoGET.Alarm_two_thresh, params[4] );
					break;

				case "Emer":
					setDatum( InfoGET.Emergency_button_enabled, (params[2].equals("On")) ? "1" : "0" );
					setDatum( InfoGET.Emergency_button_allocation, params[3] );
					break;

				case "Relay":
					setDatum( InfoGET.Relay, params[2].equals("On") ? "1" : (params[2].equals("Off") ? "0" : "2") );
					// i know, nested ternary operators are stupid.
					break;

				case "PwrData":
					setDatum( InfoGET.Energy_used_since_reset, params[3] );
					setDatum( InfoGET.Current_power_used, params[4] );
					break;

				case "Stat":
					setDatum( InfoGET.Energy_used_lifetime, params[2] );
					setDatum( InfoGET.Energy_used_previous_day, params[3] );
					break;

				case "ModInfo":
					try
					{
						if( params.length == 5 )
						{
							modInfoParse( Integer.parseInt(params[2]), Integer.parseInt(params[3]), params[4] );
						}
						else
						{
							if( VERBOSITY >= 2 )
							{
								System.out.println("Parser: ModInfo" + params[2] + params[3] + " illegal number of arguments.");
							}
						}
						
					}
					catch( NumberFormatException e )
					{
						if( VERBOSITY >= 1 )
						{
							System.out.println("Parser: ModInfo index non integer!");
						}
					}
					
					break;

				default:
					break;
			}
		}
		catch( NullPointerException e )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println("Invalid Set command.");
			}
		}

		return;
	}

	/**
	 * Given the pre-broken parameters from a ModInfo response, set the correct information in the meter object.
	 * 
	 * @author Bennett Andrews
	 * @param moduleIndex First module info index to determine which module
	 * @param infoIndex Second module info index to determine which bit of info within a module
	 * @param value String value of the module info.
	 */
	private void modInfoParse( int moduleIndex, int infoIndex, String value )
	{
		int indexPair = 10 * moduleIndex + infoIndex; // This puts it into the format ModInfo23 -> (int) 23

		switch( indexPair )
		{
			case 00:
				setDatum( InfoGET.ModInfo00, value );
				break;

			case 01:
				setDatum( InfoGET.ModInfo01, value );
				break;

			case 02:
				setDatum( InfoGET.ModInfo02, value );
				break;

			case 03:
				setDatum( InfoGET.ModInfo03, value );
				break;

			case 04:
				setDatum( InfoGET.ModInfo04, value );
				break;



			case 10:
				setDatum( InfoGET.ModInfo10, value );
				break;

			case 11:
				setDatum( InfoGET.ModInfo11, value );
				break;

			case 12:
				setDatum( InfoGET.ModInfo12, value );
				break;

			case 13:
				setDatum( InfoGET.ModInfo13, value );
				break;

			case 14:
				setDatum( InfoGET.ModInfo14, value );
				break;



			case 20:
				setDatum( InfoGET.ModInfo20, value );
				break;

			case 21:
				setDatum( InfoGET.ModInfo21, value );
				break;

			case 22:
				setDatum( InfoGET.ModInfo22, value );
				break;

			case 23:
				setDatum( InfoGET.ModInfo23, value );
				break;

			case 24:
				setDatum( InfoGET.ModInfo24, value );
				break;


			
			case 30:
				setDatum( InfoGET.ModInfo30, value );
				break;

			case 31:
				setDatum( InfoGET.ModInfo31, value );
				break;

			case 32:
				setDatum( InfoGET.ModInfo32, value );
				break;

			case 33:
				setDatum( InfoGET.ModInfo33, value );
				break;

			case 34:
				setDatum( InfoGET.ModInfo34, value );
				break;



			case 40:
				setDatum( InfoGET.ModInfo40, value );
				break;

			case 41:
				setDatum( InfoGET.ModInfo41, value );
				break;

			case 42:
				setDatum( InfoGET.ModInfo42, value );
				break;

			case 43:
				setDatum( InfoGET.ModInfo43, value );
				break;

			case 44:
				setDatum( InfoGET.ModInfo44, value );
				break;



			case 50:
				setDatum( InfoGET.ModInfo50, value );
				break;

			case 51:
				setDatum( InfoGET.ModInfo51, value );
				break;

			case 52:
				setDatum( InfoGET.ModInfo52, value );
				break;

			case 53:
				setDatum( InfoGET.ModInfo53, value );
				break;

			case 54:
				setDatum( InfoGET.ModInfo54, value );
				break;
		}
	}


	/**
	 * Converts the DD-MM-YY format from the received meter time to the 
	 * YYYY-MM-DD format required by the datetime MySQL entry. Returns 
	 * empty string if the format is incorrect.
	 * @author Bennett Andrews
	 * @param mmddyy
	 * @return
	 */
	public static String convertDDMMYYtoYYYYMMDD( String mmddyy )
	{
		if( (mmddyy == "") || (mmddyy == null) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println("Time-conversion: Time is null");
			}
			return "";
		}

		String[] pieces = mmddyy.split("-");

		if( pieces.length != 3 )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println("Time-conversion: Incorrect number of parameters.");
			}
			return "";
		}

		return String.format( (CURRENT_CENTURY + "%s-%s-%s"), pieces[2], pieces[1], pieces[0] );
	}

	/**
	 * Converts the EMMS meter Power Failure time format (MM-DD HH:MM::SS) to the
	 * database format required by the datetime MySQL entry. Returns empty string if
	 * the format is incorrect.
	 * @author Bennett Andrews
	 * @param pftime
	 * @return
	 */
	public static String convertPowerFailureTime( String pftime )
	{
		// Guard clause against null input
		if( (pftime == "") || (pftime == null) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println("Time-conversion: Time is null");
			}
			return "";
		}

		
		String[] pieces = pftime.split("-|\\s|:");	// split at '-' or ' ' or ':'

		// Guard clause against incorrect formatting/number of parameters.
		if( pieces.length != 4 )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println("Time-conversion: Incorrect number of power fail delimeters.");
			}
			return "";
		}

		
		for( String piece : pieces )
		{
			// Guard clause against non-integer parameters.
			try
			{
				Integer.parseInt( piece );
			}
			catch( Exception e )
			{
				if( VERBOSITY >= 1 )
				{
					System.out.println("Time-conversion: Power failure delimeters non integers.");
				}
				return "";
			}

			if( piece.length() != 2 )
			{
				if( VERBOSITY >= 1 )
				{
					System.out.println("Time-conversion: Power failure delimeters not spaced correctly.");
				}
				return "";
			}
		}

		String output = String.format("1111-%s-%s %s:%s:00", pieces[0], pieces[1], pieces[2], pieces[3]);
		return output;
	}



	/**
	 * Helper function to update a specific field of the meter 
	 * object with live meter data.
	 * @author Bennett Andrews
	 * @param field - InfoGET field to be updated
	 */
	public void updateDatum( InfoGET field )
	{
		String readCommand = "";

		switch( field )
		{
			case Meter_name:
				readCommand = "Read;MName";
				break;

			case Meter_id:
				readCommand = "Mod;Read;WiFiMAC";
				break;

			case Meter_password:
				readCommand = "Read;Pass";
				break;
			
			case Meter_time:
				readCommand = "Read;Time";
				break;
			
			case Energy_allocation:
				readCommand = "Read;EnAl";
				break;
			
			case Energy_allocation_reset_time:
				readCommand = "Read;RstTim";
				break;
			
			case Energy_used_since_reset:
				readCommand = "Read;PwrData";
				break;

			case Current_power_used:
				readCommand = "Read;PwrData";
				break;
			
			case Power_failure_last:
				readCommand = "Read;PwrFail";
				break;

			case Power_restore_last:
				readCommand = "Read;PwrFail";
				break;
			
			case Alarm_audible:
				readCommand = "Read;Alarm";
				break;

			case Alarm_one_thresh:
				readCommand = "Read;Alarm";
				break;

			case Alarm_two_thresh:
				readCommand = "Read;Alarm";
				break;

			case Emergency_button_enabled:
				readCommand = "Read;Emer";
				break;

			case Emergency_button_allocation:
				readCommand = "Read;Emer";
				break;

			case Relay:
				readCommand = "Read;Relay";
				break;

			case Energy_used_previous_day:
				readCommand = "Read;Stat";
				break;

			case Energy_used_lifetime:
				readCommand = "Read;Stat";
				break;

			// case Lights_enabled:
			// 	datum = "Lights";
			// 	break;



			case ModInfo00:
				readCommand = "Read;ModInfo;0;0";
				break;

			case ModInfo01:
				readCommand = "Read;ModInfo;0;1";
				break;

			case ModInfo02:
				readCommand = "Read;ModInfo;0;2";
				break;

			case ModInfo03:
				readCommand = "Read;ModInfo;0;3";
				break;

			case ModInfo04:
				readCommand = "Read;ModInfo;0;4";
				break;



			case ModInfo10:
				readCommand = "Read;ModInfo;1;0";
				break;

			case ModInfo11:
				readCommand = "Read;ModInfo;1;1";
				break;

			case ModInfo12:
				readCommand = "Read;ModInfo;1;2";
				break;

			case ModInfo13:
				readCommand = "Read;ModInfo;1;3";
				break;

			case ModInfo14:
				readCommand = "Read;ModInfo;1;4";
				break;



			case ModInfo20:
				readCommand = "Read;ModInfo;2;0";
				break;

			case ModInfo21:
				readCommand = "Read;ModInfo;2;1";
				break;

			case ModInfo22:
				readCommand = "Read;ModInfo;2;2";
				break;

			case ModInfo23:
				readCommand = "Read;ModInfo;2;3";
				break;

			case ModInfo24:
				readCommand = "Read;ModInfo;2;4";
				break;

			

			case ModInfo30:
				readCommand = "Read;ModInfo;3;0";
				break;

			case ModInfo31:
				readCommand = "Read;ModInfo;3;1";
				break;

			case ModInfo32:
				readCommand = "Read;ModInfo;3;2";
				break;

			case ModInfo33:
				readCommand = "Read;ModInfo;3;3";
				break;

			case ModInfo34:
				readCommand = "Read;ModInfo;3;4";
				break;



			case ModInfo40:
				readCommand = "Read;ModInfo;4;0";
				break;

			case ModInfo41:
				readCommand = "Read;ModInfo;4;1";
				break;

			case ModInfo42:
				readCommand = "Read;ModInfo;4;2";
				break;

			case ModInfo43:
				readCommand = "Read;ModInfo;4;3";
				break;

			case ModInfo44:
				readCommand = "Read;ModInfo;4;4";
				break;

			

			case ModInfo50:
				readCommand = "Read;ModInfo;5;0";
				break;

			case ModInfo51:
				readCommand = "Read;ModInfo;5;1";
				break;

			case ModInfo52:
				readCommand = "Read;ModInfo;5;2";
				break;

			case ModInfo53:
				readCommand = "Read;ModInfo;5;3";
				break;

			case ModInfo54:
				readCommand = "Read;ModInfo;5;4";
				break;



			default:
				if( VERBOSITY >= 1 )
				{
					System.out.println("InfoGET value not available for update.");
				}
				break;
		}

		if( readCommand != "" )
		{
			String command = commandBuilder( readCommand );
			String doubled_command = command + command;

			Client client = new Client();
			String response = "";

			for( int i = 0; i < SEND_ATTEMPTS; i++ )
			{
				if( VERBOSITY >= 2 )
				{
					System.out.println("\nSending to id: " + id() + " >> " + doubled_command );
				}

				response = client.communicate( ip(), doubled_command );
				
				if( response != "" )
				{
					break;
				}
			}
			
			if( VERBOSITY >= 2 )
			{
				System.out.println("Received: " + response );
			}
			parseResponse( response );
		}
	}
}
