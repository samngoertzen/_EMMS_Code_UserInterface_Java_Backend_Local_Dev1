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

	Meter_name, Meter_password, Meter_id, Meter_time, IP_address, Online, 
	Online_last, Last_database_update, Energy_allcation, Energy_allocation_reset_time, 
	Energy_used, Energy_remaining, Energy_time_remaining, Power_failure_last, 
	Alarm_enabled, Emergency_button_enabled, Emergency_button_allocation, 
	Debug_enabled, Lights_enabled, Relay_enabled, Firmware_version_command_board, 
	Firmware_version_WiFi_board, Location, Install_year
	
}