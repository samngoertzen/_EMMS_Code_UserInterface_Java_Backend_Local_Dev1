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
			Meter Test = new Meter("192.168.1.2");
			Test.upDateMeter(InfoSET.RELAY, "OFf");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
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
	 * Updates a value for the database
	 * @param data
	 * @param value
	 * @return
	 */
	public boolean upDateMeter(InfoSET data, String value) {
		// the first thing to is to see if the meter exists!
		boolean meterInSystem = checkIfNewMeter(this.MAC);
		if(!meterInSystem) {
			addMeterInDB();
		}
		
		
		return false; // TODO make it return only true when sucessfull update!
	}
	
	/**
	 * Adds meter into DB with relevant information!
	 */
	private boolean addMeterInDB() {
		
		
		return false; //TODO finish this stub
	}

	public boolean checkIfNewMeter(String MAC) {
		dbConnection.sendMySQL("SELECT Meters FROM EMMS");
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
