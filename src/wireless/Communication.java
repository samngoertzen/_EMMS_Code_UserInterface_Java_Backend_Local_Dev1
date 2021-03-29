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
//	public static URL url = getClass().getResource("meterData.csv");
//	getClass().getResource("/MyResource").toExternalForm()
	public static void main(String[] args) {
		//		Object meters[] = meterScan.getMeters();
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


	public static String[][] ReturnFileValues() {
		File info = new File("meterData.csv");
//		File info = new File(CurrentPath() + "data.sav");
//		File info = null;
//		try {
//			info = new File(url.toURI());
//		} catch (URISyntaxException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		try {
//			info.createNewFile();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			//			System.out.print("file operation error");
//		}

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
				//                System.out.println(Arrays.asList(values));
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
				//                System.out.println("Line " + lineNo + " Column " + columnNo + ": " + value);
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

		//        System.out.println(rowNum + " " + columnNum);
		String[][] meterInfo = new String[rowNum][columnNum];
		lineNo = 1;
		for(List<String> line: lines) {
			int columnNo = 1;
			for (String value: line) {
				// CREATE Nice ARRAY BOI
				//                System.out.println("Line " + lineNo + " Column " + columnNo + ": " + value);

				meterInfo[lineNo - 1][columnNo - 1] = value;
				columnNo++;
			}
			lineNo++;
		}

		//        System.out.println(Arrays.deepToString(meterInfo));
		return meterInfo;
	}

	private static String[] fetchNewNetworkInfo(String ip) {
		String[] meterInfo = new String[2];
		Client client = new Client();
		String response = client.Communicate(ip, 80, "!MOD;NETWORK*");
//		System.out.println(response);
		String[] blocks = response.split(",");
		blocks[1] = blocks[1].replaceAll("[^\\d.]", "");
		blocks[2] = blocks[2].replaceAll("OK", "");		
		meterInfo[0] = blocks[1];
		meterInfo[1] = blocks[2];
//		System.out.println("Ip: " + meterInfo[0] + ", MAC: " + meterInfo[1]);

		return meterInfo;
	}

	private static String[] fetchNewMeterConfig(String ip) {
		String[] meterInfo = new String[7];
		Client client = new Client();
		String response = client.Communicate(ip, 80, "!MOD;CONFIG*");
//		System.out.println(response);
		String[] blocks = response.split(";");
//				System.out.println(Arrays.toString(blocks));
		meterInfo[0] = blocks[1];
		meterInfo[1] = blocks[2];
		meterInfo[2] = blocks[3];
		meterInfo[3] = blocks[4];
		meterInfo[4] = blocks[5];
		meterInfo[5] = blocks[6];
//		System.out.println("Date: " + meterInfo[0] + ", Version No: " + meterInfo[1] + ", Name: " + meterInfo[2] + ", Number: " + meterInfo[3] + ", Location: " + meterInfo[4]);

		return meterInfo;
	}
	
	private static String[] fetchNewPowerConfig(String ip) {
		
		for(int i = 0; i < 5; i ++) {
			Client client = new Client();
			String response = client.Communicate(ip, 80, "!Read;PwrData*");
		if( response != null) {
		response = response.replaceAll("!", "");
//		response = response.replaceAll("*", "");
		String[] powerInfo = response.split(";");
		System.out.println("Energy Allocation: " + powerInfo[2] + " Energy Used: " + powerInfo[3] + " Power (Watts): " + powerInfo[4]);
		String[] powerStats = new String[3];
		powerStats[0] = powerInfo[2];
		powerStats[1] = powerInfo[3];
		powerStats[2] = powerInfo[4];
		return powerStats;
		} else {
			
			System.out.println("Got nothin that time");
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

	public static void updateCSVInfoNetwork(String ip) throws URISyntaxException {

		String[][] CSVold = ReturnFileValues();
		String[][] CSVnew;
		String[] netWorkStuff = fetchNewNetworkInfo(ip);
		String[] meterConfig = fetchNewMeterConfig(ip);
		String[] powerConfig = fetchNewPowerConfig(ip);
		String debugState = meterConfig[5];
		String MacAddr = netWorkStuff[1];
		//		System.out.println(MacAddr);
		boolean exists = false;

		for (int i = 0; i < CSVold.length; i++ ) {
			String MACinCSV = CSVold[i][11];
			//			System.out.print(MACinCSV); // This is MAC Addr List
			if (MACinCSV.contentEquals(MacAddr)) {
				exists = true;
				String old_ip = CSVold[i][3];
				String current_ip = netWorkStuff[0];
				//				System.out.println(old_ip + " " + current_ip);
				if (!old_ip.equals(current_ip)) {
					System.out.println("Replacing Ip for " + CSVold[i][4]);
					CSVold[i][3] = current_ip;
				}
				CSVold[i][13] = debugState;
//				CSVold[i][7] =  Integer.toString(100 - (Integer.parseInt(powerConfig[0]) - Integer.parseInt(powerConfig[1])) / 100);
				CSVold[i][7] = powerConfig[1];
				CSVold[i][8] = powerConfig[2];
//				CSVold[i][9] = // JOE PUT EQUATION HERE
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
//			CSVnew[CSVold.length][9] = // JOE PUT EQUATION HERE
			CSVnew[CSVold.length][10] = powerConfig[0];
			CSVnew[CSVold.length][11] = netWorkStuff[1];
			CSVnew[CSVold.length][12] = "TRUE";
			CSVnew[CSVold.length][13] = meterConfig[6];


			//			System.out.println(Arrays.deepToString(CSVnew));
		} else {

			CSVnew = new String[CSVold.length][CSVold[0].length];
			for(int i = 0; i < CSVold.length; i++ ) {
				for(int j = 0; j < CSVold[0].length; j++) {
					CSVnew[i][j] = CSVold[i][j]; 
				}
			}
//			CSVnew[CSVold.length][0] = "EMMS Collaboratory Team";
//			CSVnew[CSVold.length][1] = meterConfig[0];
//			CSVnew[CSVold.length][2] = meterConfig[1];
//			CSVnew[CSVold.length][3] = netWorkStuff[0];
//			CSVnew[CSVold.length][4] = meterConfig[2];
//			CSVnew[CSVold.length][5] = meterConfig[3];
//			CSVnew[CSVold.length][6] = meterConfig[4];
//			CSVnew[CSVold.length][7] =  Integer.toString((Integer.parseInt(powerConfig[0]) - Integer.parseInt(powerConfig[1])) / 100);
//			CSVnew[CSVold.length][8] = powerConfig[2];
////			CSVnew[CSVold.length][9] = // JOE PUT EQUATION HERE
//			CSVnew[CSVold.length][10] = powerConfig[0];
//			CSVnew[CSVold.length][11] = netWorkStuff[1];
//			CSVnew[CSVold.length][12] = "TRUE";
//			CSVnew[CSVold.length][13] = meterConfig[6];
		}

		File file_old = new File("meterData.csv");
//		File file_old = new File(url.toURI());

		file_old.delete();
		File file = new File("meterData.csv");
//		File file = new File(url.toURI());

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
				//				System.out.println("Done Adding..");
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 

	}

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
//		File file_old = new File(url.toURI());

		file_old.delete();
		File file = new File("meterData.csv");
//		File file = new File(url.toURI());

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
				//				System.out.println("Done Adding..");
				fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		//		System.out.println(Arrays.deepToString(CSVold));
	}
	
// Unused, loops through and pulses all connected. Unused since Spring 2019
	public static void showOff(String[][] ips, int trials) {
		Client client = new Client();
		for( int r = 0; r < trials * 2; r ++) {
			for(int i = 0; i < ips.length; i++ ) {
				String active = ips[i][12];
				if(active.contains("TRUE")) {
					String ip = ips[i][3];
//					System.out.println(ip);
					client.Communicate(ip, 80, "!MOD;PULSE*");
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					}
				}
			}
		}

	}
}
