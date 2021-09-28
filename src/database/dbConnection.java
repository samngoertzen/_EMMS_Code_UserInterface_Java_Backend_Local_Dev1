/**
 *  test update
 */
package database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

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
        //String test = getFrom(InfoGET.Meter_name, "AB:CD:EF:gH")[0];
        //System.out.println(test);
        // setTo("GER", InfoSET.Meter_password, "AB:CD:EF:gH");
        //insertMeter("testID");
        //deleteMeter("testID");

	}

    /**
     * Converts the InfoGET enum to string column name in the database.
     * @author Bennett Andrews
     * @param field - InfoGET value to be converted.
     * @return Database column of type "String"
     */
    public static String columnFromInfoGET(InfoGET field) {

        return field.toString();
       
    }

    /**
     * Converts the InfoSET enum to string column name in the database.
     * @author Bennett Andrews
     * @param field - InfoSET value to be converted.
     * @return Database column of type "String"
     */
    public static String columnFromInfoSET(InfoSET field) {

        return field.toString();
        
    }

    /**
     * An abstraction for setting information to the database.
     * @author Bennett Andrews
     * @param value - The desired string literal value. 
     * @param field - InfoSET value to be modified.
     * @param Meter_id - The Meter_id address of the desired meter in String format.
     * @return The return is a boolean true if the set worked, false if an error occured.
     */
    public static boolean setTo(String value, InfoSET field, String Meter_id) {
        boolean success = false;
        String sfield = columnFromInfoSET(field);
        String statement = "UPDATE Meters SET " + sfield + " = '" + value + "' WHERE (Meter_id='" + Meter_id + "')";
        try {
            sendMySQL(statement);
            success = true;
        } catch (Exception e) {
            // SQL failure
            success = false;
        }

        return success && meterTimestamp(Meter_id);
    }

    /**
     * Calling this function updates the specified meter with the time it was accessed.
     * @param Meter_id - Meter Meter_id address as a string.
     * @return true/false - Timestamp update successful/unsuccessful.
     * @author Bennett Andrews
     */
    public static boolean meterTimestamp(String Meter_id) {
        String date = timestamp();
        String statement = "UPDATE Meters SET Last_database_update='" + date + "' WHERE (Meter_id='" + Meter_id + "')";
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
     * @param Meter_id - The Meter_id address of the desired meter in String format.
     * @return The return is a ResultSet.
     */
    public static String[] getFrom(InfoGET field, String Meter_id) {
        String sfield = columnFromInfoGET(field);
        String statement = "SELECT " + sfield + " FROM Meters" + " WHERE (Meter_id ='" + Meter_id + "')";
        return sendMySQL(statement);
    }
	
    /** 
     * An abstraction for verifying that a meter exists in the database. 
     * @author Bennett Andrews
     * @param Meter_id - Desired meter Meter_id as a string.
     * @return true/false - Meter is in the database already/Meter is not in the database.
     */

     public static boolean isMeterInDB(String Meter_id) {
        String statement = "SELECT EXISTS (SELECT * FROM Meters WHERE(Meter_id='" + Meter_id + "'))";
        String[] resultSet = sendMySQL(statement); //Note: this returns exactly one cell with 0 or 1.

        return (resultSet[0].equals("1")); 

     }

    /**
     * Inserts a meter into the database in a new row for a given Meter_id address.
     * @author Bennett Andrews
     * @param Meter_id - Desired meter Meter_id as a string.
     * @return true/false - Insert was successful/unsuccessful.
     */
     public static boolean insertMeter(String Meter_id) {

        String statement = "INSERT INTO Meters(Meter_id) VALUES ('" + Meter_id + "');";

        try {
            sendMySQL(statement);
            return true;
        } catch (Exception e) {
            // SQL failure
            return false;
        }
    }

    /**
     * Deletes a meter and its row in the database for a given Meter_id address.
     * @author Bennett Andrews
     * @param Meter_id - Desired meter Meter_id as a string.
     * @return true/false - Delete was successful/unsuccessful.
     */
    public static boolean deleteMeter(String Meter_id) {

        String statement = "DELETE FROM Meters WHERE(Meter_id='" + Meter_id + "');";

        try {
            sendMySQL(statement);
            return true;
        } catch (Exception e) {
            // SQL failure
            return false;
        }
    }

    /**
     * Used to return a string format of the date. Used for MySQL timestamps.
     * @return System time in string format dd-MM-yyyy HH:mm:ss
     * @author Bennett Andrews
     */

    public static String timestamp() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
        return formatter.format(date);
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
