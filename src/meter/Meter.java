/**
 * 
 */
package meter;

import java.util.Arrays;
import java.util.HashMap;

import database.dbConnection;
import wireless.Client;

/**
 * @author ZacheryHolsinger
 *
 */
public class Meter {

	private static final char CHANGE_INDICATOR = '~'; // character appended to beginning of data when
													  // the data is updated from the meters but has
													  // not been pushed to the database yet.

	Boolean SetupComplete = false; // flips to true once initialized and the startup information is gathered.
	
	// Information from the meter /// BEGIN ///
	String Online = "";
	String IP = "";
	String Meter_id = "";
	String LOCATION = "";
	String WIFIBOARDVER = "";
	String INSTALLYEAR = "";
	String CLIENT = "";
	String ID = "";
	String SSID = ""; //TODO
	String DEBUG = "";
	String LIGHTS = "";
	String RESET = "";
	String TIME = "";
	String ENERGYALLOCATION = "";
	String ALARM = "";
	String PASSWORD = "";
	String EMERGENCYBUTTON = "";
	String RESET_TIME = "";
	String RELAY = "";
	String ENERGY_USED = "";
	String CB_VERSION = "";
	String POWERFAIL = "";
	String POWERDATA = "";
	
	// Information from the meter /// END ///

	/**
	 * @param args
	 * Main function used for testing in the class itself
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub 
		try {
			// Create a new meter
			Meter Test = new Meter("192.168.1.5");

			// test updateMeter function
			Test.updateMeter();
			Client client = new Client();
			String action_index;
			String meter_id;
			String command;

			while(true) {

				String [][] command_list = dbConnection.getCommandsForMeter( Test.Meter_id );
				System.out.println( Arrays.deepToString(command_list) );

			
				for (String[] commandset : command_list) {
					try {
						// commandset = [action_index, meter_id, command]
						action_index = commandset[0];
						meter_id = commandset[1];
						command = commandset[2];
						dbConnection.logSendAttempt(action_index);
						client.Communicate(Test.IP, 80, command);
						dbConnection.logSuccess(action_index);
						
					} catch (Exception e) {
						System.out.println("command send error");
					}	
				}

				Thread.sleep(300);
			}

//			Test.removeThisMeter();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	// LIVE DEMOS ARE DIFFICULT
	
	/**
	 * Make a new instance of a Meter
	 * @param ip Ip Address of the Meter
	 * @throws Exception Will throw if Meter is not found @ IP
	 */
	public Meter(String ip) throws Exception {
		// First thing we do is check and see if the meter is on the network
		IP = ip;
		getWifiInfo();
	}
	
/////////// Begin Code that Pushes information Java-->DB  ////////////
	/**
	 * Tests to see if configured Database is active
	 * 
	 * Already Done in dbConnection Code, this is an extension
	 */
	public boolean testDBConnection() {
		boolean isConnected = dbConnection.testConnection();	
		return isConnected;
	}
	
	/**
	 * Tests to see if a meter variable has been updated in the database or not.
	 * @author Bennett Andrews
	 * @param datum - String literal of the testing variable.
	 * @return true/false  - The variable is updated/The variable has not been updated.
	 */
	public static boolean isDatumUpdated(String datum) {

		char firstChar;
		try {
			firstChar = datum.charAt(0);
		} catch (StringIndexOutOfBoundsException e) {
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
	public void updateTimestamp() {
		dbConnection.meterTimestamp(this.Meter_id);
	}

	/**
	 * Updates a meter for the database
	 * @param data
	 * @param value
	 * @return
	 */
	public void updateMeter() {
		// the first thing to is to see if the meter exists!
		System.out.println("This Meter_id: " + this.Meter_id);
		boolean meterInSystem = dbConnection.isMeterInDB(this.Meter_id);

		System.out.println("Is meter in system? >" + meterInSystem);

		if(!meterInSystem) {
			addMeterInDB();
		}

		//If we get to here then the meter is online
		isOnline();
		/////////////////
		
		updateALARM();
		updateCBV();
		updateDEBUG();
		updateEB();
		updateEA();
		updateEU();
		updateIP();
		updateWFBV();
		updateDBG();
		updateLIGHTS();
		updatePSWD();
		updatePF();
		updateRELAY();
		updateRSTT();
		updateTIME();
		
	}


	
	/**
	 * Updates if meter is online
	 * @Zachery_Holsinger
	 * @return true/false if completed Successfully
	 */
	private boolean isOnline() {
		if (isDatumUpdated(Online)) { //@Bennett idk why but I had to take out the "!"
			try {
				dbConnection.setTo(Online, InfoSET.Online, Meter_id);
				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("OFFLINE - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates Debug mode state to the database
	 * @Zachery_Holsinger
	 * @return true/false if completed Successfully
	 */
	private boolean updateDBG() {
		if (isDatumUpdated(DEBUG)) { //@Bennett idk why but I had to take out the "!"
			try {
				dbConnection.setTo(DEBUG, InfoSET.Debug_enabled, Meter_id);
				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("DEBUG - false");
				return false;
			}
		}
		return true;
	}



	/**
	 * Updates meter wifi board version to to the database
	 * @Zachery_Holsinger
	 * @return true/false if completed Successfully
	 */
	private boolean updateWFBV() {
		if (isDatumUpdated(WIFIBOARDVER)) { //@Bennett idk why but I had to take out the "!"
			try {
				dbConnection.setTo(WIFIBOARDVER, InfoSET.Firmware_version_WiFi_board, Meter_id);
				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("WIFIBOARDVER - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the ALARM value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateALARM() {

		if (!isDatumUpdated(ALARM)) {
			try {
				String strippedData = ALARM.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Alarm_enabled, Meter_id);
				ALARM = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("ALARM - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the CB_VERSION value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateCBV() {

		if (!isDatumUpdated(CB_VERSION)) {
			try {
				String strippedData = CB_VERSION.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Firmware_version_command_board, Meter_id);
				CB_VERSION = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("CB_VERSION - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the DEBUG value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateDEBUG() {

		if (!isDatumUpdated(DEBUG)) {
			try {
				String strippedData = DEBUG.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Debug_enabled, Meter_id);
				DEBUG = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("DEBUG - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the EMERGENCYBUTTON value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	 public boolean updateEB() {

		if (!isDatumUpdated(EMERGENCYBUTTON)) {
			try {
				String strippedData = EMERGENCYBUTTON.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Emergency_button_enabled, Meter_id);
				EMERGENCYBUTTON = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("EMERGENCYBUTTON - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the ENERGYALLOCATION value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateEA() {

		if (!isDatumUpdated(ENERGYALLOCATION)) {
			try {
				String strippedData = ENERGYALLOCATION.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Energy_allocation, Meter_id);
				ENERGYALLOCATION = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("ENERGYALLOCATION - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the ENERGY_USED value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateEU() {

		if (!isDatumUpdated(ENERGY_USED)) {
			try {
				String strippedData = ENERGY_USED.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Energy_used, Meter_id);
				ENERGY_USED = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("ENERGY_USED - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the IP value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateIP() {

		//TODO this statement only works if evaluates to true, not sure why it is different than the rest
		if (!isDatumUpdated(IP)) {
			try {
				String strippedData = IP.substring(1); //@Bennett not sure why you added this substring
				dbConnection.setTo(IP, InfoSET.IP_address, Meter_id);
				IP = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("IP - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the LIGHTS value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateLIGHTS() {

		if (!isDatumUpdated(LIGHTS)) {
			try {
				String strippedData = LIGHTS.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Lights_enabled, Meter_id);
				LIGHTS = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("LIGHTS - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the PASSWORD value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updatePSWD() {

		if (!isDatumUpdated(PASSWORD)) {
			try {
				String strippedData = PASSWORD.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Meter_password, Meter_id);
				PASSWORD = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("PASSWORD - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the POWERFAIL value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updatePF() {

		if (!isDatumUpdated(POWERFAIL)) {
			try {
				String strippedData = POWERFAIL.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Power_failure_last, Meter_id);
				POWERFAIL = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("POWERFAIL - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the RELAY value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateRELAY() {

		if (!isDatumUpdated(RELAY)) {
			try {
				String strippedData = RELAY.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Relay_enabled, Meter_id);
				RELAY = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("RELAY - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the RESET_TIME value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateRSTT() {

		if (!isDatumUpdated(RESET_TIME)) {
			try {
				String strippedData = RESET_TIME.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Energy_allocation_reset_time, Meter_id);
				RESET_TIME = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("RESET_TIME - false");
				return false;
			}
		}
		return true;
	}

	// /**
	//  * Updates the SSID value to the database
	//  * @return true/false - Update successful/update unsuccessful
	//  * @author Bennett Andrews
	//  */
	// public boolean updateSSID() {

	// 	if (!isDatumUpdated(SSID)) {
	// 		try {
	// 			String strippedData = SSID.substring(1);
	// 			dbConnection.setTo(strippedData, InfoSET.SSID, Meter_id);
	// 			SSID = strippedData;

	// 			return true;

	// 		} catch (Exception e) {
	// 			// What if data update fails?
	// 			System.out.println("SSID - false");
	// 			return false;
	// 		}
	// 	}
	// 	return true;
	// }

	public void removeThisMeter() {
		dbConnection.deleteMeter(this.Meter_id);
	}

	/**
	 * Updates the TIME value to the database
	 * @return true/false - Update successful/update unsuccessful
	 * @author Bennett Andrews
	 */
	public boolean updateTIME() {

		if (!isDatumUpdated(SSID)) {
			try {
				String strippedData = TIME.substring(1);
				dbConnection.setTo(strippedData, InfoSET.Meter_time, Meter_id);
				TIME = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("TIME - false");
				return false;
			}
		}
		return true;
	}





	
	
	/**
	 * Adds meter into DB with relevant information!
	 * @author Bennett Andrews
	 */
	private boolean addMeterInDB() {
		dbConnection.insertMeter(Meter_id);
		return false;
	}
	
	
/////////// End Code that Pushes information Java-->DB  ////////////
// 						Hug Tom
/////////// Begin Code that Pulls information Meter-->Java////////////
	/**
	 * TODO
	 * Updates the Meter instance with Wifi Data
	 * @apiNote Runs on Meter Class Initiation
	 * @throws Exception if it cannot find a meter
	 * @author ZacheryHolsinger
	 */
	public void getWifiInfo() throws Exception{
		HashMap<String, String> wifiData = new HashMap<String,String>();
		Client client = new Client();
		//// BEGIN GET INFORMATION ////
		// go and get network settings from the meter to see if it is actually alive
		String networkInformationRAW = client.Communicate(this.IP, 80, "!MOD;NETWORK*");
		//since we might get "noDev" as a response, and our expected output is a large string we can set this minimum cap
		if (networkInformationRAW.length() < 15) {
			System.out.println(networkInformationRAW);
			throw new Exception("Meter Not Found");
		} else {
			String netRAW = networkInformationRAW;
			netRAW = netRAW.replaceAll("OK", ""); // This removes the ending OK which is by default sent back
			String[] RAWArray = netRAW.split(",");
			this.IP = RAWArray[1].replaceAll("CIFSR:STAMAC", "");
			this.Meter_id = RAWArray[2].toUpperCase();
		}
		
		/// END GET INFORMATION ////
		
		/// BEGIN PARSE INFORMATION ////
		String configInfo = client.Communicate(this.IP, 80, "!MOD;CONFIG*");
		String configParse[] = configInfo.split(";");
		LOCATION = configParse[3];
		WIFIBOARDVER = configParse[2];
		INSTALLYEAR = configParse[1];
		CLIENT = configParse[5];
		ID = configParse[4];
		DEBUG = configParse[6];
		/// END PARSE INFORMATION
		
		return;
	}
	
	/**
	 * TODO
	 * Updates the Meter instance with new data from the CB
	 * @apiNote Unimplemented
	 * @return
	 */
	public HashMap<String,String> getPowerInfo(){
		HashMap<String, String> wifiData = new HashMap<String,String>();
		// TODO Populate power information from board. Not working as of 10/5/2021
		
		
		return wifiData;
	}
/////////// End Code that Pulls information Meter-->Java////////////
}
