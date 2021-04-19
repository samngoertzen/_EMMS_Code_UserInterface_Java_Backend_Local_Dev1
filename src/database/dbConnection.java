/**
 *  test update
 */
package database;

import java.sql.*;
import java.util.Arrays;

import meter.InfoGET;
import meter.InfoSET;

/**
 * @author ZacheryHolsinger
 *
 */
public class dbConnection {
	
	// Driver settings
	static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    static final String DB_IP = "153.42.35.209";
    static final String DB_PORT = "3306";
    
    // Profile settings from /var/www/html/index.php
    static final String USER = "emmsdev";
    static final String PASS = "pumpkin";
    static final String DATABASE = "EMMS";
    // add table name
    
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//sendMySQL("SELECT * FROM Meters");
        //getFrom(InfoGET.EMERGENCYBUTTON, "n:cheese");
        //setTo("0", InfoSET.EMERGENCYBUTTON, "n:cheese");
        //insertMeter("testMAC");
        //deleteMeter("testMAC");

	}

    /**
     * Converts the InfoGET enum to string column name in the database.
     * @author Bennett Andrews
     * @param field - InfoGET value to be converted.
     * @return Database column of type "String"
     */
    public static String columnFromInfoGET(InfoGET field) {

        switch (field) {

            case ALARM:
                return "Alarm";

            case CB_VERSION:
                return "CB_version";
            
            case DEBUG:
                return "Debug";

            case EMERGENCYBUTTON:
                return "Emergency_button_state";

            case ENERGYALLOCATION:
                return "Total_allotment";

            case ENERGY_USED:
                return "Used_energy";

            case IP:
                return "IP_addr";
            
            case LIGHTS:
                return "Lights";
            
            case MAC:
                return "MAC";

            case PASSWORD:
                return "Password";
            
            case POWERDATA:
                return "";

            case POWERFAIL:
                return "Last_power_failure";

            case RELAY:
                return "Relay";

            case RESET_TIME:
                return "Reset_time";
            
            case SSID:
                return "Meter_id";

            case TIME:
                return "Time";
            
            default:
                return "INFOGET ENUM ERROR";
        }
    }

    /**
     * Converts the InfoSET enum to string column name in the database.
     * @author Bennett Andrews
     * @param field - InfoSET value to be converted.
     * @return Database column of type "String"
     */
    public static String columnFromInfoSET(InfoSET field) {

        switch (field) {

            case ALARM:
                return "Alarm";

            case CB_VERSION:
                return "CB_version";
            
            case DEBUG:
                return "Debug";

            case EMERGENCYBUTTON:
                return "Emergency_button_state";

            case ENERGYALLOCATION:
                return "Total_allotment";

            case ENERGY_USED:
                return "Used_energy";

            case IP:
                return "IP_addr";
            
            case LIGHTS:
                return "Lights";
            
            case MAC:
                return "MAC";

            case PASSWORD:
                return "Password";
            
            case POWERDATA:
                return "";

            case POWERFAIL:
                return "Last_power_failure";

            case RELAY:
                return "Relay";

            case RESET_TIME:
                return "Reset_time";
            
            case SSID:
                return "Meter_id";

            case TIME:
                return "Time";
            
            default:
                return "INFOSET ENUM ERROR";
        }
    }

    /**
     * An abstraction for setting information to the database.
     * @author Bennett Andrews
     * @param value - The desired string literal value. 
     * @param field - InfoSET value to be modified.
     * @param mac - The MAC address of the desired meter in String format.
     * @return The return is a boolean true if the set worked, false if an error occured.
     */
    public static boolean setTo(String value, InfoSET field, String mac) {
        String sfield = columnFromInfoSET(field);
        String statement = "UPDATE Meters SET " + sfield + " = '" + value + "' WHERE (MAC='" + mac + "')";
        try {
            sendMySQL(statement);
            return true;
        } catch (Exception e) {
            // SQL failure
            return false;
        }
    }

    /**
     * An abstraction for getting information from the database.
     * @author Bennett Andrews
     * @param field - InfoGET value to be fetched
     * @param mac - The MAC address of the desired meter in String format.
     * @return The return is a ResultSet.
     */
    public static String[] getFrom(InfoGET field, String mac) {
        String sfield = columnFromInfoGET(field);
        String statement = "SELECT " + sfield + " FROM Meters" + " WHERE (MAC ='" + mac + "')";
        return sendMySQL(statement);
    }
	
    /** 
     * An abstraction for verifying that a meter exists in the database. 
     * @author Bennett Andrews
     * @param mac - Desired meter MAC as a string.
     * @return true/false - Meter is in the database already/Meter is not in the database.
     */

     public static boolean isMeterInDB(String mac) {
        String statement = "SELECT EXISTS (SELECT * FROM Meters WHERE(MAC='" + mac + "'))";
        String[] resultSet = sendMySQL(statement); //Note: this returns exactly one cell with 0 or 1.

        return (resultSet[0].equals("1")); 

     }

    /**
     * Inserts a meter into the database in a new row for a given MAC address.
     * @author Bennett Andrews
     * @param mac - Desired meter MAC as a string.
     * @return true/false - Insert was successful/unsuccessful.
     */
     public static boolean insertMeter(String mac) {

        String statement = "INSERT INTO Meters(MAC) VALUES ('" + mac + "');";

        try {
            sendMySQL(statement);
            return true;
        } catch (Exception e) {
            // SQL failure
            return false;
        }
    }

    /**
     * Deletes a meter and its row in the database for a given MAC address.
     * @author Bennett Andrews
     * @param mac - Desired meter MAC as a string.
     * @return true/false - Delete was successful/unsuccessful.
     */
    public static boolean deleteMeter(String mac) {

        String statement = "DELETE FROM Meters WHERE(MAC='" + mac + "');";

        try {
            sendMySQL(statement);
            return true;
        } catch (Exception e) {
            // SQL failure
            return false;
        }
    }
     
	/**
	 * Talks to Meters table in the database and gets all the meter ips!
	 */
	public static void updateIPs() {
		
	}
	
	/**
	 * Executes mysql query on database and returns response
	 * @param statement
	 * @return
	 */
	public static String[] sendMySQL(String statement) {
		Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ResultSet returnrs = null;

        String[] resultString;

        try {
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
//            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(
                    "jdbc:mariadb://"+ DB_IP + ":" + DB_PORT + "/" + DATABASE, USER, PASS);
//            System.out.println("Connected database successfully...");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(statement);
            returnrs = rs;
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            resultString = new String[columnsNumber];
            while (rs.next()) {
                for (int i = 1; i <= columnsNumber; i++) {
                    if (i > 1) System.out.print(",  ");
                    String columnValue = rs.getString(i);
                    resultString[i-1] = columnValue;
//                    System.out.print(columnValue + " " + rsmd.getColumnName(i));
                }
//                System.out.println("");
            }
            
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return null;
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return null;
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            	return null;
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
            
            //end finally try
        }//end try
//        System.out.println("Goodbye!"); 
		
		return resultString;
	}
	
	
	/**
	 * Tests to see if connection can be established to configured database
	 * @return true if connection can be established
	 */
	public static boolean testConnection() {
		Connection conn = null;
        Statement stmt = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(
                    "jdbc:mariadb://"+ DB_IP + ":" + DB_PORT + "/" + DATABASE, USER, PASS);
            System.out.println("Connected database successfully...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return false;
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return false;
        } finally {
            //finally block used to close resources
        	//BENNETT YOU ARE AWESOME ALSO THIS COMMENT IS FOR GITHUB TESTING DELETE MEEEE
            try {
                if (stmt != null) {
                    conn.close();
                }
            } catch (SQLException se) {
            	return false;
            }// do nothing
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
        System.out.println("Goodbye!"); 
		
		return true;
	}

}
