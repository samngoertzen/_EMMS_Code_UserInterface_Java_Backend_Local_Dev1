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
	Meter_name,
	Meter_password, 
	Meter_id, 
	Meter_time, 
	IP_address, 
	Online, 
	Energy_allocation, 
	Energy_allocation_reset_time, 
	Energy_used_since_reset,
	Energy_used_previous_day,
	Energy_used_lifetime,
	Energy_remaining, 
	Energy_time_remaining, 
	Power_failure_last, 
	Power_restore_last,
	Current_power_used,
	Alarm_audible,
	Alarm_one_thresh,
	Alarm_two_thresh, 
	Emergency_button_enabled, 
	Emergency_button_allocation, 
	Lights_enabled,
	Relay, 
	ModInfo00,
	ModInfo01,
	ModInfo02,
	ModInfo03,
	ModInfo04,
	ModInfo10,
	ModInfo11,
	ModInfo12,
	ModInfo13,
	ModInfo14,
	ModInfo20,
	ModInfo21,
	ModInfo22,
	ModInfo23,
	ModInfo24,
	ModInfo30,
	ModInfo31,
	ModInfo32,
	ModInfo33,
	ModInfo34,
	ModInfo40,
	ModInfo41,
	ModInfo42,
	ModInfo43,
	ModInfo44,
	ModInfo50,
	ModInfo51,
	ModInfo52,
	ModInfo53,
	ModInfo54
}