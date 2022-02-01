package meter;

import java.util.HashMap;

import database.dbConnection;
import wireless.Client;

/**
 * @author ZacheryHolsinger
 *
 */
public class Meter 
{
	private static final char CHANGE_INDICATOR = '~'; // character appended to beginning of data when
													  // the data is updated from the meters but has
													  // not been pushed to the database yet.
	private static final int SEND_ATTEMPTS = 3;

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
		if( !is_connected( ip ) )
		{
			Exception e = new Exception("Meter not connected.");
			throw e;
		}

		// Next, set default data in the data map. This is primarily
		// to initialize the map, not to set valid values.
		defaultData();

		data.put( InfoGET.IP_address, ip );
		//data.put(InfoGET.Meter_id, "TEST");
		//updateMeter();
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






	public boolean run()
	{
		// Update the meter information
		updateMeter();

		Client client = new Client();
		String action_index;
		String command;

		// TODO test stub

		System.out.println("\n\nRunning meter " + "\n\n" + data.get( InfoGET.Meter_id ) );
		dbPushAll();

		return true;

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

	

	
/////////// Begin Code that Pushes information Java-->DB  ////////////
	
	/**
	 * Updates a meter for the database
	 * @param data
	 * @param value
	 * @return
	 */
	public void updateMeter() 
	{
		// is the meter registered in the database?
		System.out.println("Updating Meter_id: " + data.get( InfoGET.Meter_id ) );
		boolean meterInSystem = dbConnection.isMeterInDB( data.get( InfoGET.Meter_id ) );
		System.out.println("Is meter in system? >" + meterInSystem);

		if(!meterInSystem) 
		{
			addMeterInDB();
		}

		dbConnection.setTo( "noodle", InfoSET.Meter_password, data.get( InfoGET.Meter_id ) );

		//If we get to here then the meter is online
		//isOnline();
		/////////////////
		
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
		dbConnection.insertMeter( data.get( InfoGET.Meter_id ) );
		return false; 
	}

	/**
	 * Deletes this meter from the database.
	 * @author Bennett Andrews
	 */
	public void removeMeterInDB()
	{
		dbConnection.deleteMeter( data.get( InfoGET.Meter_id ) );
	}

	/**
	 * Tests to see if a meter variable has been updated in the database or not.
	 * @author Bennett Andrews
	 * @param datum - String literal of the testing variable.
	 * @return true/false  - The variable is updated/The variable has not been updated.
	 */
	public static boolean isDatumUpdated(String datum) 
	{
		char firstChar;
		try 
		{
			firstChar = datum.charAt(0);
		} 
		catch (StringIndexOutOfBoundsException e) 
		{
			System.out.println("Null datum");
			return true;
		}

		return CHANGE_INDICATOR != firstChar;
	}

	/**
	 * Tells the database that the meter was accessed and sets Last_update
	 * to the current time.
	 * @author Bennett Andrews
	 */
	public void updateTimestamp()
	{
		dbConnection.meterTimestamp( data.get( InfoGET.Meter_id ) );
	}

	/**
	 * Update all values stored in the data map to the database.
	 * TODO: testing
	 * 
	 * @author Bennett Andrews
	 * @param field
	 * @param value
	 */
	public void dbPushAll()
	{
		for( InfoSET field : InfoSET.values() )
		{
			InfoGET converted_field = InfoGET.values()[ field.ordinal() ]; // TODO: Test this
			System.out.println("InfoGET: " + converted_field.toString() );

			dbConnection.setTo( data.get( converted_field ), field, data.get( InfoGET.Meter_id ) );
		}
		updateTimestamp();
	}

	/**
	 * Tests whether this meter is online.
	 * @author Bennett Andrews
	 * @param ipv4 - IPV4 address of the meter to be pinged
	 * @return true/false - is_connected/is_not_connected
	 */
	public static boolean is_connected( String ipv4 )
	{
		boolean is_meter = false;

		// Create client.
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
				System.out.println( "Not a meter." );
			}
		}

		client.close();
		return is_meter;
	}


	  // TODO: Update meter datum


	// /**
	//  * Updates if meter is online
	//  * @Zachery_Holsinger
	//  * @return true/false if completed Successfully
	//  */
	// private boolean isOnline() {
	// 	if (isDatumUpdated(Online)) { //@Bennett idk why but I had to take out the "!"
	// 		try {
	// 			dbConnection.setTo(Online, InfoSET.Online, Meter_id);
	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("OFFLINE - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

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
	//  * Updates the CB_VERSION value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateCBV() {

	// 	if (!isDatumUpdated(CB_VERSION)) {
	// 		try {
	// 			String strippedData = CB_VERSION.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.Firmware_version_command_board, Meter_id);
	// 			CB_VERSION = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("CB_VERSION - false");
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


	

	
	
/////////// End Code that Pushes information Java-->DB  ////////////
// 						Hug Tom
/////////// Begin Code that Pulls information Meter-->Java////////////
	// /**
	//  * Updates the Meter instance with Wifi Data
	//  * @apiNote Runs on Meter Class Initiation
	//  * @throws Exception if it cannot find a meter
	//  * @author ZacheryHolsinger
	//  * @depreciated - OLD WIFI BOARD
	//  */
	// public void getWifiInfo() throws Exception{
	// 	HashMap<String, String> wifiData = new HashMap<String,String>();
	// 	Client client = new Client();
	// 	//// BEGIN GET INFORMATION ////
	// 	// go and get network settings from the meter to see if it is actually alive
	// 	String networkInformationRAW = client.communicate(this.IP, 80, "!MOD;NETWORK*");
	// 	//since we might get "noDev" as a response, and our expected output is a large string we can set this minimum cap
	// 	if (networkInformationRAW.length() < 15) {
	// 		System.out.println(networkInformationRAW);
	// 		throw new Exception("Meter Not Found");
	// 	} else {
	// 		String netRAW = networkInformationRAW;
	// 		netRAW = netRAW.replaceAll("OK", ""); // This removes the ending OK which is by default sent back
	// 		String[] RAWArray = netRAW.split(",");
	// 		this.IP = RAWArray[1].replaceAll("CIFSR:STAMAC", "");
	// 		this.Meter_id = RAWArray[2].toUpperCase();
	// 	}
		
	// 	/// END GET INFORMATION ////
		
	// 	/// BEGIN PARSE INFORMATION ////
	// 	String configInfo = client.communicate(this.IP, 80, "!MOD;CONFIG*");
	// 	String configParse[] = configInfo.split(";");
	// 	LOCATION = configParse[3];
	// 	WIFIBOARDVER = configParse[2];
	// 	INSTALLYEAR = configParse[1];
	// 	CLIENT = configParse[5];
	// 	ID = configParse[4];
	// 	DEBUG = configParse[6];
	// 	/// END PARSE INFORMATION
		
	// 	return;
	// }
	
	// /**
	//  * TODO
	//  * Updates the Meter instance with new data from the CB
	//  * @apiNote Unimplemented
	//  * @return
	//  */
	// public HashMap<String,String> getPowerInfo(){
	// 	HashMap<String, String> wifiData = new HashMap<String,String>();
	// 	// TODO Populate power information from board. Not working as of 10/5/2021
		
		
	// 	return wifiData;
	// }
/////////// End Code that Pulls information Meter-->Java////////////
}
