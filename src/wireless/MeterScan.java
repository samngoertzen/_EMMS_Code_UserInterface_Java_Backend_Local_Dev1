package wireless;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.stream.Stream;

import meter.Meter;

/**
 * Scans IP's AND Confirms it with EMMS Standard to see which meters are ours
 * @author Zachery Holsinger
 *
 */
public class MeterScan {

	private static ArrayList<Meter> confirmed_meters = new ArrayList<Meter>();

	// IPV4 scan ranges.
	// TODO: Getters and setters
	private static int net1_start = 192;
	private static int net1_end   = 192;
	private static int net2_start = 168;
	private static int net2_end   = 168;
	private static int net3_start = 1;
	private static int net3_end   = 1;
	private static int net4_start = 1;
	private static int net4_end   = 15;
	
	
	public static void main(String[] args) 
	{	
		MeterScan meterScan = new MeterScan();
		meterScan.getMeters( false );
	}

	/**
	 * function from https://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
	 * Grabs IP from network.
	 * @param preferIpv4
	 * @param preferIPv6
	 * @return
	 * @throws SocketException
	 */
	public static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException {
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while (en.hasMoreElements()) {
			NetworkInterface i = (NetworkInterface) en.nextElement();
			for (Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements();) {
				InetAddress addr = (InetAddress) en2.nextElement();
				if (!addr.isLoopbackAddress()) {
					if (addr instanceof Inet4Address) {
						if (preferIPv6) {
							continue;
						}
						return addr;
					}
					if (addr instanceof Inet6Address) {
						if (preferIpv4) {
							continue;
						}
						return addr;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Gets an array of all confirmed meters on the network.
	 * The network is defined by the IPV4 address ranges specified in the object
	 * variables net1_start, net1_end, ... etc.
	 * @author Bennett Andrews and ZacharyHolsinger
	 * @param useMyIp - Use the host range specified by local variables, or use the host of this device.
	 * @return Meter[] - Array of confirmed meters connected to the network
	 */
	public Meter[] getMeters( boolean useMyIp )
	{
		String ip = "";
		Meter[] meters;

		if( useMyIp )
		{
			try 
			{
				// returns with a slash in front fo the ip. i.e. /153.168.1.1
				// substring to get rid of the slash.
				ip = getFirstNonLoopbackAddress(true, false).toString().substring(1);
			} 
			catch (SocketException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

			String[] ip_str = ip.split("\\."); // double backslash for regex
			int[] ip_int = Stream.of( ip_str ).mapToInt( Integer::parseInt ).toArray();
			System.out.println("My Current IP is: " + ip_int[0] + "." + ip_int[1] + "." + ip_int[2] + "." + ip_int[3]);

			// use this host ipv4 instead of globally defined.
			meters = scan_ip4_ranges(ip_int[0], ip_int[0], 
									 ip_int[1], ip_int[1], 
									 net3_start, net3_end, 
									 net4_start, net4_end);
		}
		else // do not useMyIp
		{
			//  use class-set variables for ipv4 host 
			meters = scan_ip4_ranges(net1_start, net1_end, 
									 net2_start, net2_end, 
									 net3_start, net3_end, 
									 net4_start, net4_end);
		}

		return meters;
	}

	/**
	 * Scan a specified range of IPV4 addresses to find potential meters.
	 * Meters are stored in the confirmed_meters ArrayList for later extraction.
	 * @author Bennett Andrews
	 * @param net1_start
	 * @param net1_end
	 * @param net2_start
	 * @param net2_end
	 * @param net3_start
	 * @param net3_end
	 * @param net4_start
	 * @param net4_end
	 * @return Meter[] array of all confirmed meters in the desired range.
	 */
	public Meter[] scan_ip4_ranges( int net1_start, int net1_end,
										  int net2_start, int net2_end,
										  int net3_start, int net3_end,
										  int net4_start, int net4_end )
	{
		// ============================================================================
		// START Guard clause against addresses out of range.
		// IPV4 address parts go from 1 to 255.
		if( net1_start < 1 ||
			net1_end < 1   ||
		    net2_start < 1 ||
			net2_end < 1   ||
			net3_start < 1 ||
			net3_end < 1   ||
			net4_start < 1 ||
			net4_end < 1     )
		{
			System.out.println("Network address out of range.");
			return null;
		}
		else 
		if(  net1_start > 255 ||
			 net1_end > 255   ||
			 net2_start > 255 ||
			 net2_end > 255   ||
			 net3_start > 255 ||
			 net3_end > 255   ||
			 net4_start > 255 ||
			 net4_end > 255     )
		{
			System.out.println("Network address out of range.");
			return null;
		}
		// END Guard clause against addresses out of range.

		// START Guard clause against end addresses in front of start addressses.
		if( net1_end < net1_start ||
		    net2_end < net2_start ||
			net3_end < net3_start ||
			net4_end < net4_start   )
		{
			System.out.println("Network end addresses in front of start addresses.");
			return null;
		}
		// END Guard clause against end addresses in front of start addressses.
		// ============================================================================



		// Create client.
		Client client = new Client();
		String response = "";

		// Reset confirmed meters.
		

		// I know that this looks stupid, but it loops every address in each range.
		for( int a = net1_start; a <= net1_end; a++ )
		{
			for( int b = net2_start; b <= net2_end; b++ )
			{
				for( int c = net3_start; c <= net3_end; c++ )
				{
					for( int d = net4_start; d <= net4_end; d++ )
					{
						String ip = String.format( "%s.%s.%s.%s", a, b, c, d ); // format ip from ints
						System.out.println( "Scanning ip " + ip );

						response = ""; // reset response
						response = client.communicate( ip, 80, "!Read;CBver$905*"); // TODO get real ping command
						System.out.println( response );

						if( response == "METER" ) // TODO get better meter verification condition.
						{
							try 
							{
								System.out.println("IP " + ip + " confirmed as meter.");
								confirmed_meters.add( new Meter( ip ) );
							} 
							catch (Exception e) 
							{
								e.printStackTrace();
								System.out.println("Ip " + ip + " IO error.");
							}
						}
						else if( response == "NoDev" )
						{
							System.out.println( "Not a meter (NoDev)" );
						}
						else
						{
							System.out.println( "Not a meter." );
						}
					}
				}
			}
		}

		client.close();

		Meter[] meter_array = new Meter[ confirmed_meters.size() ];
		meter_array = confirmed_meters.toArray( meter_array );

		return meter_array;
	}

	/**
	 * Gets the meter array from the last scan.
	 * @author Bennett Andrews
	 * @return
	 */
	public Meter[] lastScan()
	{
		Meter[] meter_array = new Meter[ confirmed_meters.size() ];
		meter_array = confirmed_meters.toArray( meter_array );
		return meter_array;
	}
}
