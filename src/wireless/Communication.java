package wireless;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Zachery Holsinger
 *
 */
public class Communication {
	public static URL url = ClassLoader.getSystemResource("meterData.csv");
	
	/**
	 * Sandbox for Communication Class
	 * MAIN NOT USED IN REV, ONLY TO ASSIST IN DEVELOPMENT
	 * @param args
	 */
	public static void main(String[] args) {
				Client client = new Client();
		//		String response;
		//		String ip = (String) meters[0]; // THIS WILL ALWAYS PING THE FIRST LOWEST METER, SO USE FOR WHEN ONLY 1 IS PLUGGED in
		//		fetchNewMeterConfig(ip);
		//		updateCSVInfoNetwork(ip);
//		startUp();
											String ip = "192.168.1.2";
				String response = client.Communicate(ip, 80, "!Set;Lights;On*");
				System.out.println(response);
//				 response = client.Communicate(ip, 80, "!Read;PwrData*");
//				System.out.println(response);
//				response = client.Communicate(ip, 80, "!MOD;CONFIG*");
//				System.out.println(response);
				 response = client.Communicate(ip, 80, "!Set;Lights;Off*");
				System.out.println(response);
//				fetchNewPowerConfig("192.168.1.108");

				
	}

	/**
	 * Yee ole' 'Main' from the java GUI days. 
	 * Grabs meter data for connected meters.
	 * @return
	 * @throws URISyntaxException
	 * @author ZacheryHolsinger
	 * @deprecated Java Gui Days
	 */
	public static String[] startUp() throws URISyntaxException {
		Object[] metersobj = meterScan.getMeters();
		String[] meters;
		meters = new String[metersobj.length];
		for (int i = 0; i < meters.length; i++) {
			meters[i] = (String) metersobj[i];
			String ip = (String) meters[i]; 
			fetchNewMeterConfig(ip);
			updateCSVInfoNetwork(ip);
		}
		refreshActive((String[]) meters);
		return meters;
	}

	/**
	 * Get 2D Array of meter information from CSV file
	 * @deprecated Java Gui Days
	 * @return
	 */
	public static String[][] ReturnFileValues() {
		File info = new File("meterData.csv");
		File file= info;

		// this gives you a 2-dimensional array of strings
		List<List<String>> lines = new ArrayList<>();
		Scanner inputStream;

		try{
			inputStream = new Scanner(file);

			while(inputStream.hasNextLine()){
				String line= inputStream.nextLine();
				String[] values = line.split(",");
				// this adds the currently parsed line to the 2-dimensional string array
				lines.add(Arrays.asList(values));
			}

			inputStream.close();
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// the following code lets you iterate through the 2-dimensional array
		int lineNo = 1;
		int rowNum = 0;
		int columnNum = 0;

		// Just count for initial array size from double arrayList
		for(List<String> line: lines) {
			int columnNo = 1;
			for (String value: line) {
				// CREATE Nice ARRAY BOI
				columnNo++;
				if (columnNo - 1 > columnNum) {
					columnNum = columnNo;
				}
				if( lineNo > rowNum) {
					rowNum = lineNo;
				}
			}
			lineNo++;
		}
		// Actually put values in for array 
		String[][] meterInfo = new String[rowNum][columnNum];
		lineNo = 1;
		for(List<String> line: lines) {
			int columnNo = 1;
			for (String value: line) {
				meterInfo[lineNo - 1][columnNo - 1] = value;
				columnNo++;
			}
			lineNo++;
		}
		return meterInfo;
	}

	/**
	 * Implicit, Gets network info from Wifi Board
	 * @author ZacheryHolsinger
	 * @deprecated Java Gui Days
	 * @param ip Meter IP
	 * @return
	 */
	private static String[] fetchNewNetworkInfo(String ip) {
		String[] meterInfo = new String[2];
		Client client = new Client();
		String response = client.Communicate(ip, 80, "!MOD;NETWORK*");
		String[] blocks = response.split(",");
		blocks[1] = blocks[1].replaceAll("[^\\d.]", "");
		blocks[2] = blocks[2].replaceAll("OK", "");		
		meterInfo[0] = blocks[1];
		meterInfo[1] = blocks[2];
		return meterInfo;
	}

	/**
	 * Implicit, Gets Wifi Board Configuration from Wifi Board
	 * @author ZacheryHolsinger
	 * @deprecated Java Gui Days
	 * @param ip Meter IP
	 * @return Array of unparsed response from meter
	 * @return
	 */
	private static String[] fetchNewMeterConfig(String ip) {
		String[] meterInfo = new String[7];
		Client client = new Client();
		String response = client.Communicate(ip, 80, "!MOD;CONFIG*");
		String[] blocks = response.split(";");
		meterInfo[0] = blocks[1];
		meterInfo[1] = blocks[2];
		meterInfo[2] = blocks[3];
		meterInfo[3] = blocks[4];
		meterInfo[4] = blocks[5];
		meterInfo[5] = blocks[6];
		return meterInfo;
	}
	
	/**
	 * Implicit, Gets Power Configuration from Wifi Board
	 * @author ZacheryHolsinger
	 * @deprecated Java Gui Days
	 * @param ip Meter IP
	 * @return Array of unparsed response from meter
	 * @return
	 */
	private static String[] fetchNewPowerConfig(String ip) {
		
		for(int i = 0; i < 5; i ++) {
			Client client = new Client();
			String response = client.Communicate(ip, 80, "!Read;PwrData*");
		if( response != null) {
		response = response.replaceAll("!", "");
		response = response.replaceAll("*", "");
		String[] powerInfo = response.split(";");
		System.out.println("Energy Allocation: " + powerInfo[2] + " Energy Used: " + powerInfo[3] + " Power (Watts): " + powerInfo[4]);
		String[] powerStats = new String[3];
		powerStats[0] = powerInfo[2];
		powerStats[1] = powerInfo[3];
		powerStats[2] = powerInfo[4];
		return powerStats;
		} else {
			String[] powerStats = new String[3];
			powerStats[0] =  null;
			powerStats[1] = null;
			powerStats[2] = null;
			return powerStats;
		}
		}
		System.out.println("Something is wrong with SPI...");
		return null;
	}

	/**
	 * Implicitive, Updates CSV with new information
	 * @author ZacheryHolsinger
	 * @deprecated Java Gui Days
	 * @param ip Meter IP
	 * @return
	 */
	public static void updateCSVInfoNetwork(String ip) throws URISyntaxException {

		String[][] CSVold = ReturnFileValues();
		String[][] CSVnew;
		String[] netWorkStuff = fetchNewNetworkInfo(ip);
		String[] meterConfig = fetchNewMeterConfig(ip);
		String[] powerConfig = fetchNewPowerConfig(ip);
		String debugState = meterConfig[5];
		String MacAddr = netWorkStuff[1];
		boolean exists = false;

		for (int i = 0; i < CSVold.length; i++ ) {
			String MACinCSV = CSVold[i][11]; // Mac ADDR List
			if (MACinCSV.contentEquals(MacAddr)) {
				exists = true;
				String old_ip = CSVold[i][3];
				String current_ip = netWorkStuff[0];
				if (!old_ip.equals(current_ip)) {
					System.out.println("Replacing Ip for " + CSVold[i][4]);
					CSVold[i][3] = current_ip;
				}
				CSVold[i][13] = debugState;
				CSVold[i][7] = powerConfig[1];
				CSVold[i][8] = powerConfig[2];
//				CSVold[i][9] = // JOE PUT POWER EQUATION HERE
				CSVold[i][10] = powerConfig[0];
						
			}
		}

		if (!exists) {
			System.out.println("Meter isn't found in System, adding...");
			CSVnew = new String[CSVold.length + 1][CSVold[0].length];
			try {
			for(int i = 0; i < CSVold.length; i++ ) {
				for(int j = 0; j < CSVold[0].length; j++) {
					CSVnew[i][j] = CSVold[i][j]; 
				}
			}
			} catch (java.lang.ArrayIndexOutOfBoundsException e) {
				
			}

			CSVnew[CSVold.length][0] = "EMMS Collaboratory Team";
			CSVnew[CSVold.length][1] = meterConfig[0];
			CSVnew[CSVold.length][2] = meterConfig[1];
			CSVnew[CSVold.length][3] = netWorkStuff[0];
			CSVnew[CSVold.length][4] = meterConfig[2];
			CSVnew[CSVold.length][5] = meterConfig[3];
			CSVnew[CSVold.length][6] = meterConfig[4];
//			CSVnew[CSVold.length][7] =  Integer.toString((Integer.parseInt(powerConfig[0]) - Integer.parseInt(powerConfig[1])) / 100);
			CSVnew[CSVold.length][8] = powerConfig[2];
//			CSVnew[CSVold.length][9] = // JOE PUT POWER EQUATION HERE
			CSVnew[CSVold.length][10] = powerConfig[0];
			CSVnew[CSVold.length][11] = netWorkStuff[1];
			CSVnew[CSVold.length][12] = "TRUE";
			CSVnew[CSVold.length][13] = meterConfig[6];

		} else {

			CSVnew = new String[CSVold.length][CSVold[0].length];
			for(int i = 0; i < CSVold.length; i++ ) {
				for(int j = 0; j < CSVold[0].length; j++) {
					CSVnew[i][j] = CSVold[i][j]; 
				}
			}

		}

		File file_old = new File("meterData.csv");
		file_old.delete();
		File file = new File("meterData.csv");
		FileWriter fr;
		for (int i = 0; i < CSVnew.length; i++) {
			try {
				fr = new FileWriter(file, true);
				// Converting and sending to CSV
				String line = Arrays.toString(CSVnew[i]);
				line = line.replace("[", "");
				line = line.replace("]", "");
				line = line.replace(", ", ",");
				fr.write(line);
				fr.write("\n");
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}

	/**
	 * Implicitive, Refreshes information from Meter into CSV
	 * @author ZacheryHolsinger
	 * @deprecated Java Gui Days
	 * @param activeIps Meter IPs as array
	 */
	public static void refreshActive(String[] activeIps) throws URISyntaxException {
		String[][] CSVold = ReturnFileValues(); 

		for (int i = 0; i < CSVold.length; i++ ) {
			CSVold[i][12] = "FALSE";
			for (int j = 0; j < activeIps.length; j++) {
				String IPfile = CSVold[i][3];
				if(IPfile.contains(activeIps[j])) {
					CSVold[i][12] = "TRUE";
				}
			}
		}
		String[][] CSVnew = new String[CSVold.length][CSVold[0].length];
		try {
		for(int i = 0; i < CSVold.length; i++ ) {
			for(int j = 0; j < CSVold[0].length; j++) {
				CSVnew[i][j] = CSVold[i][j]; 
			}
		}
		} catch( java.lang.ArrayIndexOutOfBoundsException e) {
			
		}

		File file_old = new File("meterData.csv");
		file_old.delete();
		File file = new File("meterData.csv");
		FileWriter fr;
		for (int i = 0; i < CSVnew.length; i++) {
			try {
				fr = new FileWriter(file, true);
				// Converting and sending to CSV
				String line = Arrays.toString(CSVnew[i]);
				line = line.replace("[", "");
				line = line.replace("]", "");
				line = line.replace(", ", ",");
				fr.write(line);
				fr.write("\n");
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}
	
	/**
	 *  Unused, loops through and pulses all connected. Unused since Spring 2019
	 * @param ips
	 * @param trials
	 * @deprecated
	 * @author ZacheryHolsinger
	 * @category Useless
	 * @apiNote Specific code that got me a 97% on my Programming II Final
	 */
	public static void showOff(String[][] ips, int trials) {
		Client client = new Client();
		for( int r = 0; r < trials * 2; r ++) {
			for(int i = 0; i < ips.length; i++ ) {
				String active = ips[i][12];
				if(active.contains("TRUE")) {
					String ip = ips[i][3];
					client.Communicate(ip, 80, "!MOD;PULSE*");
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						// FOO
					}
				}
			}
		}

	}
}
