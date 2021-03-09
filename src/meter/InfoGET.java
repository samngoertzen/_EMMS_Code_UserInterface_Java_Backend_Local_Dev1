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
	IP, MAC, SSID, DEBUG, LIGHTS, RESET, TIME, ENERGYALLOCATION,
	ALARM, PASSWORD, EMERGENCYBUTTON, RESET_TIME, RELAY, ENERGY_USED,
	CB_VERSION, POWERFAIL, POWERDATA
}