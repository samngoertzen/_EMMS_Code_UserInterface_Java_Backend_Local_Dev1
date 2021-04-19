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
	String IP = "";
	String MAC = "";
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
	 * [3/5/21] - made for future goal of multi-threading
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			// Create a new meter
			Meter Test = new Meter("192.168.1.2");

			// set some default values for testing
			Test.TIME = "~00:00";
			Test.MAC = "n:cheese";

			// test updateMeter function
			Test.updateMeter();

			// // Test setting a value to the database
			// boolean isSet = dbConnection.setTo("0", InfoSET.EMERGENCYBUTTON, "n:cheese");
			// System.out.println("Set test worked? >" + isSet);

			// // Test getting a value from the database
			// String[] get = dbConnection.getFrom(InfoGET.EMERGENCYBUTTON, "n:cheese");
			// System.out.println("Get Test: " + Arrays.toString(get));

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
		//getWifiInfo();
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
	 * Updates a meter for the database
	 * @param data
	 * @param value
	 * @return
	 */
	public void updateMeter() {
		// the first thing to is to see if the meter exists!
		boolean meterInSystem = dbConnection.isMeterInDB(this.MAC);

		System.out.println("Is meter in system? >" + meterInSystem);

		if(!meterInSystem) {
			addMeterInDB();
		}

		updateALARM();
		updateCBV();
		updateDEBUG();
		updateEB();
		updateEA();
		updateEU();
		updateIP();
		updateLIGHTS();
		updatePSWD();
		updatePF();
		updateRELAY();
		updateRSTT();
		updateSSID();
		updateTIME();
		
	}






	/**
	 * Updates the ALARM value to the database
	 * @return true/false - Update successful/update unsuccessful
	 */
	public boolean updateALARM() {

		if (!isDatumUpdated(ALARM)) {
			try {
				String strippedData = ALARM.substring(1);
				dbConnection.setTo(strippedData, InfoSET.ALARM, MAC);
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
	 */
	public boolean updateCBV() {

		if (!isDatumUpdated(CB_VERSION)) {
			try {
				String strippedData = CB_VERSION.substring(1);
				dbConnection.setTo(strippedData, InfoSET.CB_VERSION, MAC);
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
	 */
	public boolean updateDEBUG() {

		if (!isDatumUpdated(DEBUG)) {
			try {
				String strippedData = DEBUG.substring(1);
				dbConnection.setTo(strippedData, InfoSET.DEBUG, MAC);
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
	 */
	 public boolean updateEB() {

		if (!isDatumUpdated(EMERGENCYBUTTON)) {
			try {
				String strippedData = EMERGENCYBUTTON.substring(1);
				dbConnection.setTo(strippedData, InfoSET.EMERGENCYBUTTON, MAC);
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
	 */
	public boolean updateEA() {

		if (!isDatumUpdated(ENERGY_USED)) {
			try {
				String strippedData = ENERGYALLOCATION.substring(1);
				dbConnection.setTo(strippedData, InfoSET.ENERGYALLOCATION, MAC);
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
	 */
	public boolean updateEU() {

		if (!isDatumUpdated(ENERGY_USED)) {
			try {
				String strippedData = ENERGY_USED.substring(1);
				dbConnection.setTo(strippedData, InfoSET.ENERGY_USED, MAC);
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
	 */
	public boolean updateIP() {

		if (!isDatumUpdated(IP)) {
			try {
				String strippedData = IP.substring(1);
				dbConnection.setTo(strippedData, InfoSET.IP, MAC);
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
	 */
	public boolean updateLIGHTS() {

		if (!isDatumUpdated(LIGHTS)) {
			try {
				String strippedData = LIGHTS.substring(1);
				dbConnection.setTo(strippedData, InfoSET.LIGHTS, MAC);
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
	 */
	public boolean updatePSWD() {

		if (!isDatumUpdated(PASSWORD)) {
			try {
				String strippedData = PASSWORD.substring(1);
				dbConnection.setTo(strippedData, InfoSET.PASSWORD, MAC);
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
	 */
	public boolean updatePF() {

		if (!isDatumUpdated(POWERFAIL)) {
			try {
				String strippedData = POWERFAIL.substring(1);
				dbConnection.setTo(strippedData, InfoSET.POWERFAIL, MAC);
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
	 */
	public boolean updateRELAY() {

		if (!isDatumUpdated(RELAY)) {
			try {
				String strippedData = RELAY.substring(1);
				dbConnection.setTo(strippedData, InfoSET.RELAY, MAC);
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
	 */
	public boolean updateRSTT() {

		if (!isDatumUpdated(RESET_TIME)) {
			try {
				String strippedData = RESET_TIME.substring(1);
				dbConnection.setTo(strippedData, InfoSET.RESET_TIME, MAC);
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

	/**
	 * Updates the SSID value to the database
	 * @return true/false - Update successful/update unsuccessful
	 */
	public boolean updateSSID() {

		if (!isDatumUpdated(SSID)) {
			try {
				String strippedData = SSID.substring(1);
				dbConnection.setTo(strippedData, InfoSET.SSID, MAC);
				SSID = strippedData;

				return true;

			} catch (Exception e) {
				// What if data update fails?
				System.out.println("SSID - false");
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates the TIME value to the database
	 * @return true/false - Update successful/update unsuccessful
	 */
	public boolean updateTIME() {

		if (!isDatumUpdated(SSID)) {
			try {
				String strippedData = TIME.substring(1);
				dbConnection.setTo(strippedData, InfoSET.TIME, MAC);
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
	 */
	private boolean addMeterInDB() {
		dbConnection.insertMeter(MAC);
		return false; //TODO finish this stub
	}
	
	
/////////// End Code that Pushes information Java-->DB  ////////////
// 						Hug Tom
/////////// Begin Code that Pulls information Meter-->Java////////////
	/**
	 * TODO
	 * Updates the Meter instance with new Wifi Data
	 * @return
	 * @throws Exception if it cannot find a meter
	 */
	public HashMap<String,String> getWifiInfo() throws Exception{
		HashMap<String, String> wifiData = new HashMap<String,String>();
		Client client = new Client();
		// go and get network settings from the meter to see if it is actually alive
		String networkInformationRAW = client.Communicate(this.IP, 80, "!MOD;NETWORK*");
		// since we might get "noDev" as a response, and our expected output is a large string we can set this minimum cap
		if (networkInformationRAW.length() < 15) {
			System.out.println(networkInformationRAW);
			throw new Exception("Meter Not Found");
		} else {
			String netRAW = networkInformationRAW;
//			System.out.println("Origg: " + netRAW);
			netRAW = netRAW.replaceAll("OK", ""); // This removes the ending OK 
			String[] RAWArray = netRAW.split(",");
//			System.out.println(Arrays.deepToString(RAWArray));
			this.IP = RAWArray[1].replaceAll("CIFSR:STAMAC", "");
			this.MAC = RAWArray[2].toUpperCase();
			System.out.println(this.IP);
			System.out.println(this.MAC);
		}
		
		String configInfo = client.Communicate(this.IP, 80, "!MOD;CONFIG*");
		System.out.println(configInfo);
		
		configInfo = client.Communicate(this.IP, 80, "!Read;Alarm*");
		System.out.println(configInfo);
		return wifiData;
	}
	
	/**
	 * TODO
	 * Updates the Meter instance with new data from the CB
	 * @return
	 */
	public HashMap<String,String> getPowerInfo(){
		HashMap<String, String> wifiData = new HashMap<String,String>();
		
		
		
		return wifiData;
	}
/////////// End Code that Pulls information Meter-->Java////////////
}
