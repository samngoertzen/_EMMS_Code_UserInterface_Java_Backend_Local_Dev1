package meter;


/**
 * @author ZacheryHolsinger
 * Enum used to lock in the set information you can get from the meter.
 * 
 * Command board commands are Read;~ while Wifi Commands are Set;~
 * 
 * @api *!*IMPORTANT*!* - Order of enum values must be the same between InfoGET and InfoSET!
 */
public enum InfoGET 
{
	Meter_password, 
	Meter_id, 
	Meter_time, 
	IP_address, 
	Online,
	Online_last, 
	Energy_allocation, 
	Energy_allocation_reset_time, 
	Energy_used, 
	Energy_remaining, 
	Energy_load, 
	Energy_time_remaining, 
	Power_failure_last, 
	Alarm_audible,
	Alarm_one_enabled,
	Alarm_one_thresh,
	Alarm_two_enabled,
	Alarm_two_thresh, 
	Emergency_button_enabled, 
	Emergency_button_allocation, 
	Debug_enabled, 
	Lights_enabled, 
	Relay_enabled, 
	Firmware_version_command_board, 
	Firmware_version_WiFi_board, 
}