package wireless;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.stream.Stream;

import meter.InfoGET;
import meter.Meter;

/**
 * Scans IP's AND Confirms it with EMMS Standard to see which meters are ours
 * @author Bennett Andrews and Zachery Holsinger
 *
 */
public class MeterScan 
{
	private ArrayList<Meter> confirmed_meters = new ArrayList<Meter>();

	// Default IPV4 scan ranges.
	// Can be overriden by setters.
	private int default_net1_start = 192;
	private int net1_end   = 192;
	private int net2_start = 168;
	private int net2_end   = 168;
	private int net3_start = 1;
	private int net3_end   = 1;
	private int net4_start = 10;
	private int net4_end   = 15;

	private static final int VERBOSITY = 2; // Global variable for how much output we want. 0 = none, 1 = errors only, 2 = all output.



	/**
	 * function from https://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
	 * Grabs self-IP from network.
	 * @param preferIpv4
	 * @param preferIPv6
	 * @return
	 * @throws SocketException
	 */
	public static InetAddress getFirstNonLoopbackAddress(boolean preferIpv4, boolean preferIPv6) throws SocketException 
	{
		Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
		while( en.hasMoreElements() ) 
		{
			NetworkInterface i = (NetworkInterface) en.nextElement();
			for( Enumeration en2 = i.getInetAddresses(); en2.hasMoreElements(); ) 
			{
				InetAddress addr = (InetAddress) en2.nextElement();
				if( !addr.isLoopbackAddress() ) 
				{
					if( addr instanceof Inet4Address ) 
					{
						if( preferIPv6 ) 
						{
							continue;
						}
						return addr;
					}
					if( addr instanceof Inet6Address ) 
					{
						if( preferIpv4 ) 
						{
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
	 * Returns the string IPV4 address for this device.
	 * @author Bennett Andrews
	 * @param useMyIp
	 * @return
	 */
	public static String getMyIp()
	{
		// String ip = "";

		// try 
		// {
		// 	// returns with a slash in front fo the ip. i.e. /153.168.1.1
		// 	// substring to get rid of the slash.
		// 	ip = getFirstNonLoopbackAddress(true, false).toString().substring(1);
		// } 
		// catch (SocketException e) 
		// {
		// 	e.printStackTrace();
		// 	ip = null;
		// }

		// return "192.168.1.3";
		return "127.0.0.1";
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
				if( VERBOSITY >= 2 )
						{
							System.out.println( "-\nScanning ip (NonLoopBack)" + ip );
						}
			} 
			catch (SocketException e) 
			{
				e.printStackTrace();
				return null;
			}

			String[] ip_str = ip.split("\\."); // double backslash for regex
			int[] ip_int = Stream.of( ip_str ).mapToInt( Integer::parseInt ).toArray();

			// use this host ipv4 instead of globally defined.
			meters = scan_ip4_ranges(ip_int[0], ip_int[0], 
									 ip_int[1], ip_int[1], 
									 net3_start, net3_end, 
									 net4_start, net4_end);
		}
		else // do not useMyIp
		{
			//  use class-set variables for ipv4 host 
			meters = scan_ip4_ranges(default_net1_start, net1_end, 
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
		if( net1_start < 0 ||
			net1_end < 0   ||
		    net2_start < 0 ||
			net2_end < 0   ||
			net3_start < 0 ||
			net3_end < 0   ||
			net4_start < 0 ||
			net4_end < 0     )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println("Network address out of range. (less than 0)");
			}
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
			if( VERBOSITY >= 1 )
			{
				System.out.println("Network address out of range. (greater than 255)");
			}
			return null;
		}
		// END Guard clause against addresses out of range.

		// START Guard clause against end addresses in front of start addressses.
		if( net1_end < net1_start ||
		    net2_end < net2_start ||
			net3_end < net3_start ||
			net4_end < net4_start   )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println("Network end addresses in front of start addresses.");
			}
			return null;
		}
		// END Guard clause against end addresses in front of start addressses.
		// ============================================================================


		// Reset confirmed_meters arrayList
		confirmed_meters.clear();
		

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

						if( VERBOSITY >= 2 )
						{
							System.out.println( "-\nScanning ip " + ip );
						}

						scan_ip4( ip );
					}
				}
			}
		}

		return lastScan();
	}

	/**
	 * Scans a specific IPV4 address to see if it is an EMMS meter.
	 * If it is a meter, the meter is added to the confirmed_meters arraylist.
	 * @apiNote DOES NOT: reset confirmed_meters
	 * @author Bennett Andrews
	 * @param ipv4 IPV4 address to be tested
	 * @return true/false - ipv4 is a meter/isnot a meter
	 */
	private boolean scan_ip4( String ipv4 )
	{
		try
		{
			Meter newMeter = new Meter( ipv4 );
			newMeter.updateDatum( InfoGET.Meter_id );
			confirmed_meters.add( newMeter );
			
			if ( VERBOSITY >= 2 )
			{
				System.out.println("Added meter " + ipv4 );
			}
			return true;
		}
		catch( Exception e )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println("Did not add " + ipv4 + " as meter.");
			}
			return false;
		}
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

	/* =============================================================================== */
	/* START IPV4 NETWORK ADDRESS RANGE SETTERS                                        */
	/* =============================================================================== */

	/**
	 * Set the lower address bound for the first term of the network scan.
	 * @author Bennett Andrews
	 * @param net1_start
	 * @return 0 on success, -1 on failure.
	 */
	public int setN1S( int net1_start )
	{
		if( (net1_start < 0) || (net1_start > 255) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "Invalid net1_start value." );
			}
			return -1;
		}
		else
		{
			this.default_net1_start = net1_start;
			return 0;
		}
	}

	/**
	 * Set the upper address bound for the first term of the network scan.
	 * @author Bennett Andrews
	 * @param net1_end
	 * @return 0 on success, -1 on failure.
	 */
	public int setN1E( int net1_end )
	{
		if( (net1_end < 0) || (net1_end > 255) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "Invalid net1_end value." );
			}
			return -1;
		}
		else
		{
			this.net1_end = net1_end;
			return 0;
		}
	}



	/**
	 * Set the lower address bound for the second term of the network scan.
	 * @author Bennett Andrews
	 * @param net2_start
	 * @return 0 on success, -1 on failure.
	 */
	public int setN2S( int net2_start )
	{
		if( (net2_start < 0) || (net2_start > 255) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "Invalid net2_start value." );	
			}
			return -1;
		}
		else
		{
			this.net2_start = net2_start;
			return 0;
		}
	}

	/**
	 * Set the upper address bound for the second term of the network scan.
	 * @author Bennett Andrews
	 * @param net2_end
	 * @return 0 on success, -1 on failure.
	 */
	public int setN2E( int net2_end )
	{
		if( (net2_end < 0) || (net2_end > 255) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "Invalid net2_end value." );	
			}
			return -1;
		}
		else
		{
			this.net2_end = net2_end;
			return 0;
		}
	}


	
	/**
	 * Set the lower address bound for the third term of the network scan.
	 * @author Bennett Andrews
	 * @param net3_start
	 * @return 0 on success, -1 on failure.
	 */
	public int setN3S( int net3_start )
	{
		if( (net3_start < 0) || (net3_start > 255) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "Invalid net3_start value." );	
			}
			return -1;
		}
		else
		{
			this.net3_start = net3_start;
			return 0;
		}
	}

	/**
	 * Set the upper address bound for the third term of the network scan.
	 * @author Bennett Andrews
	 * @param net3_end
	 * @return 0 on success, -1 on failure.
	 */
	public int setN3E( int net3_end )
	{
		if( (net3_end < 0) || (net3_end > 255) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "Invalid net3_end value." );	
			}
			return -1;
		}
		else
		{
			this.net3_end = net3_end;
			return 0;
		}
	}

	/**
	 * Set the lower address bound for the fourth term of the network scan.
	 * @author Bennett Andrews
	 * @param net4_start
	 * @return 0 on success, -1 on failure.
	 */
	public int setN4S( int net4_start )
	{
		if( (net4_start < 0) || (net4_start > 255) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "Invalid net4_start value." );
			}
			return -1;
		}
		else
		{
			this.net4_start = net4_start;
			return 0;
		}
	}

	/**
	 * Set the upper address bound for the fourth term of the network scan.
	 * @author Bennett Andrews
	 * @param net4_end
	 * @return 0 on success, -1 on failure.
	 */
	public int setN4E( int net4_end )
	{
		if( (net4_end < 0) || (net4_end > 255) )
		{
			if( VERBOSITY >= 1 )
			{
				System.out.println( "Invalid net4_end value." );	
			}
			return -1;
		}
		else
		{
			this.net4_end = net4_end;
			return 0;
		}
	}

	/* =============================================================================== */
	/* END IPV4 NETWORK ADDRESS RANGE SETTERS                                          */
	/* =============================================================================== */
}
