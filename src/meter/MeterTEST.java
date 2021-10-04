/**
 * 
 */
package meter;

import wireless.Client;

/**
 * @author ZacheryHolsinger
 *
 */
public class MeterTEST {
	Boolean SetupComplete = false; // flips to true once initialized and the startup information is gathered.
	
	// Dummy variables /// BEGIN ///
	String IP = "XXX.XXX.X.xxx";
	String MAC = "12:34:56:78";
	String SSID = "fbi sURVeLIIeNCE vAN tREe";
	String DEBUG = "On";
	String LIGHTS = "On";
	String RESET = "No";
	String TIME = "February 30";
	String ENERGYALLOCATION = "40kWh";
	String ALARM = "Brrring";
	String PASSWORD = "Coconuts";
	String EMERGENCYBUTTON = "No";
	String RESET_TIME = "12/43/2021";
	String RELAY = "On";
	String ENERGY_USED = "304";
	String CB_VERSION = "1.3";
	String POWERFAIL = "4/5/60";
	String POWERDATA = "50";
	// Dummy variables /// END /// 

	/**
	 * @param args
	 * Main function used for testing in the class itself
	 * [3/5/21] - made for future goal of multi-threading
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Meter Test = new Meter("192.168.1.2");

	}
	
	public MeterTEST(String ip) throws Exception {
		// First thing we do is check and see if the meter is on the network
		IP = ip;
		Client client = new Client();
		// go and get network settings from the meter to see if it is actually alive
		String networkInformationRAW = client.Communicate(ip, 80, "!MOD,NETWORK*");
		// since we might get "noDev" as a response, and our expected output is a large string we can set this minimum cap
		if (networkInformationRAW.length() < 15) {
			throw new Exception("Meter Not Found");
		}
	}

}
