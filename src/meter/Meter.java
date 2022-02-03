package meter;

import java.io.*;
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
		update();
		pushAllInDB();
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
			data.put( datum, "0");
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
		catch (IOException e) 
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
		// TODO test stub.

		// Client client = new Client();
		// String action_index;
		// String command;

		// // Fetch all the commands for a specific meter
		// String [][] command_list = dbConnection.getCommandsForMeter( Meter_id );

		// // For each command,
		// for (String[] commandset : command_list) {

		// 	try {
		// 		// commandset = [action_index, meter_id, command]
		// 		action_index = commandset[0];
		// 		command = commandset[2];

		// 		// Send the command to the Wi-Fi board and log results
		// 		// in the database.
		// 		dbConnection.logSendAttempt(action_index);
		// 		client.communicate(IP, 8001, command);
		// 		dbConnection.logSuccess(action_index);
				
		// 	} catch (Exception e) {
		// 		System.out.println("command send error");
		// 	}	

		// 	// Wait 300 ms then move to the next command.
		// 	try {
		// 		Thread.sleep(300);
		// 	} catch (InterruptedException e) {
		// 		// TODO Auto-generated catch block
		// 		e.printStackTrace();
		// 	}
		// }
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
		for( InfoSET field : InfoSET.values() )
		{
			InfoGET converted_field = InfoGET.values()[ field.ordinal() ];
			System.out.println("InfoGET: " + converted_field.toString() );

			dbConnection.setTo( data.get( converted_field ), field, id() );
		}

		updateTimestampInDB();
	}




	
	/**
	 * Updates a meter for the database
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

		// TODO

		// updateALARM();
		// updateCBV();
		// updateDEBUG();
		// updateEB();
		// updateEA();
		// updateEU();
		// updateIP();
		// updateWFBV();
		// updateDBG();
		// updateLIGHTS();
		// updatePSWD();
		// updatePF();
		// updateRELAY();
		// updateRSTT();
		// updateTIME();

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
		dbConnection.setTo( "0", InfoSET.Online, id() );
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
			else
			{
				System.out.println( "No online meter." );
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
	 * Command Board Version
	 * <p>Updates meter object with live meter datum.
	 * @author Bennett Andrews
	 * @return
	 */
	public boolean updateCBV()
	{
		boolean updated = false;
		String command = commandBuilder( "Read", "CBver" );
		String response = "";
		try
		{
			// TODO response = sendCommand();
			// TODO parseResponse( response );
			updated = true;
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			updated = false;
		}

		return updated;
	}


	// TODO: Update meter datum


	// /**
	//  * Updates Debug mode state to the database
	//  * @Zachery_Holsinger
	//  * @return true/false if completed Successfully
	//  */
	// private boolean updateDBG() {
	// 	if (isDatumUpdated(DEBUG)) { //@Bennett idk why but I had to take out the "!"
	// 		try {
	// 			dbConnection.setTo(DEBUG, InfoSET.Debug_enabled, Meter_id);
	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("DEBUG - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }



	// /**
	//  * Updates meter wifi board version to to the database
	//  * @Zachery_Holsinger
	//  * @return true/false if completed Successfully
	//  */
	// private boolean updateWFBV() {
	// 	if (isDatumUpdated(WIFIBOARDVER)) { //@Bennett idk why but I had to take out the "!"
	// 		try {
	// 			dbConnection.setTo(WIFIBOARDVER, InfoSET.Firmware_version_WiFi_board, Meter_id);
	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("WIFIBOARDVER - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the ALARM value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateALARM() {

	// 	if (!isDatumUpdated(ALARM)) {
	// 		try {
	// 			String strippedData = ALARM.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Alarm_enabled, Meter_id);
	// 			ALARM = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("ALARM - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }



	// /**
	//  * Updates the DEBUG value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateDEBUG() {

	// 	if (!isDatumUpdated(DEBUG)) {
	// 		try {
	// 			String strippedData = DEBUG.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Debug_enabled, Meter_id);
	// 			DEBUG = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("DEBUG - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the EMERGENCYBUTTON value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	//  public boolean updateEB() {

	// 	if (!isDatumUpdated(EMERGENCYBUTTON)) {
	// 		try {
	// 			String strippedData = EMERGENCYBUTTON.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Emergency_button_enabled, Meter_id);
	// 			EMERGENCYBUTTON = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("EMERGENCYBUTTON - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the ENERGYALLOCATION value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateEA() {

	// 	if (!isDatumUpdated(ENERGYALLOCATION)) {
	// 		try {
	// 			String strippedData = ENERGYALLOCATION.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Energy_allocation, Meter_id);
	// 			ENERGYALLOCATION = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("ENERGYALLOCATION - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the ENERGY_USED value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateEU() {

	// 	if (!isDatumUpdated(ENERGY_USED)) {
	// 		try {
	// 			String strippedData = ENERGY_USED.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Energy_used, Meter_id);
	// 			ENERGY_USED = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("ENERGY_USED - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the IP value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateIP() {

	// 	//TODO this statement only works if evaluates to true, not sure why it is different than the rest
	// 	if (!isDatumUpdated(IP)) {
	// 		try {
	// 			String strippedData = IP.substring(1); //@Bennett not sure why you added this substring
	// 			dbConnection.setTo(IP, InfoSET.IP_address, Meter_id);
	// 			IP = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("IP - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the LIGHTS value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateLIGHTS() {

	// 	if (!isDatumUpdated(LIGHTS)) {
	// 		try {
	// 			String strippedData = LIGHTS.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Lights_enabled, Meter_id);
	// 			LIGHTS = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("LIGHTS - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the PASSWORD value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updatePSWD() {

	// 	if (!isDatumUpdated(PASSWORD)) {
	// 		try {
	// 			String strippedData = PASSWORD.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Meter_password, Meter_id);
	// 			PASSWORD = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("PASSWORD - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the POWERFAIL value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updatePF() {

	// 	if (!isDatumUpdated(POWERFAIL)) {
	// 		try {
	// 			String strippedData = POWERFAIL.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Power_failure_last, Meter_id);
	// 			POWERFAIL = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("POWERFAIL - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the RELAY value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateRELAY() {

	// 	if (!isDatumUpdated(RELAY)) {
	// 		try {
	// 			String strippedData = RELAY.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Relay_enabled, Meter_id);
	// 			RELAY = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("RELAY - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	// /**
	//  * Updates the RESET_TIME value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateRSTT() {

	// 	if (!isDatumUpdated(RESET_TIME)) {
	// 		try {
	// 			String strippedData = RESET_TIME.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Energy_allocation_reset_time, Meter_id);
	// 			RESET_TIME = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("RESET_TIME - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }
	//
	// /**
	//  * Updates the TIME value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateTIME() {

	// 	if (!isDatumUpdated(SSID)) {
	// 		try {
	// 			String strippedData = TIME.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Meter_time, Meter_id);
	// 			TIME = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("TIME - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }
}
