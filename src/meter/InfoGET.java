/**
 * 
 */
package meter;

/**
 * @author ZacheryHolsinger
 * Enum used to lock in the set information you can get from the meter.
 * 
 * Command board commands are Read;~ while Wifi Commands are Set;~
 */
public enum InfoGET {
	ALARM, CB_VERSION, DEBUG, EMERGENCYBUTTON, ENERGYALLOCATION,  
	ENERGY_USED, IP, LIGHTS, MAC, PASSWORD, POWERDATA, POWERFAIL, 
	RELAY, RESET_TIME, SSID, TIME, WIFIBOARDVERSION, LOCATION, INSTALLYEAR, ID, ONLINE
}