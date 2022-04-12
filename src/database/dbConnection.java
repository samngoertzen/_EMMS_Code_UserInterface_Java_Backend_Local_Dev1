/**
 *  test update
 */
package database;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import meter.InfoGET;
import wireless.MeterScan;

/**
 * @author ZacheryHolsinger
 *
 */
public class dbConnection 
{	
	// Driver settings
	static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    static final String DB_PORT = "3306";
    
    // Profile settings from /var/www/html/index.php
    static final String USER = "emmsdev";
    static final String PASS = "pumpkin";
    static final String DATABASE = "EMMS";
    // add table name

    static final int MAX_SEND_ATTEMPTS = 1;
    
    
	/**
     * MAIN is a sandbox function for testing database connection functions
	 * @param args
	 */
	public static void main(String[] args) 
    {
        // ---------------------------------------
        //      Test getFrom
        // ---------------------------------------
        // String test = getFrom(InfoGET.Meter_name, "AB:CD:EF:gH");
        // System.out.println(test);

        // ---------------------------------------
        //      Test setTo
        // ---------------------------------------
        // setTo("golf", InfoSET.Meter_password, "AB:CD:EF:gH");

        // ---------------------------------------
        //      Test isMeterInDB
        // ---------------------------------------
        // System.out.println( isMeterInDB("AB:CD:EF:gH") ? "yes":"no" );
        // System.out.println( isMeterInDB("fishie") ? "yes":"no" );

        // ---------------------------------------
        //      Test insertMeter and deleteMeter
        // ---------------------------------------
        // insertMeter("testID");
        // deleteMeter("testID");

        // ---------------------------------------
        //      Test getCommandsForMeter
        // ---------------------------------------
        // String[][] test = getCommandsForMeter("AB:CD:EF:gH");
        // System.out.println( Arrays.deepToString(test) );

        // ---------------------------------------
        //      Test getOnlineMeterIPs
        // ---------------------------------------
        // System.out.println( Arrays.deepToString( getOnlineMeterIPs() ) );

        // ---------------------------------------
        //      Test 
        // ---------------------------------------
        // setAllMetersOffline();

        // ---------------------------------------
        //      Test logSendAttempt
        // ---------------------------------------
        // logSendAttempt("41");
        

	}



    /**
     * Converts the InfoGET enum to string column name in the database.
     * @author Bennett Andrews
     * @param field - InfoGET value to be converted.
     * @return Database column of type "String"
     */
    public static String columnFromInfoGET(InfoGET field) 
    {
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
    public static boolean setTo(String value, InfoGET field, String Meter_id) 
    {
        boolean success = false;
        String sfield = columnFromInfoGET(field);
        String statement = "UPDATE Meters SET " + sfield + " = '" + value + "' WHERE (Meter_id='" + Meter_id + "')";

        try 
        {
            sendMySQL(statement);
            meterTimestamp(Meter_id);
            success = true;
        } 
        catch (Exception e) 
        {
            // SQL failure
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    /**
     * Calling this function updates the specified meter with the time it was accessed.
     * @param Meter_id - Meter Meter_id address as a string.
     * @return true/false - Timestamp update successful/unsuccessful.
     * @author Bennett Andrews
     */
    public static boolean meterTimestamp(String Meter_id) 
    {
        String date = timestamp();
        String statement = "UPDATE Meters SET Last_database_update='" + date + "' WHERE (Meter_id='" + Meter_id + "')";
        try 
        {
            sendMySQL(statement);
            return true;
        } 
        catch (Exception e) 
        {
            // SQL failure
            return false;
        }
    }

    /**
     * Sets all meters in the database offline.
     * @author Bennett Andrews
     */
    public static void setAllMetersOffline()
    {
        String statement = "UPDATE Meters SET Online='0'";

        try
        {
            sendMySQL( statement );
        }
        catch( Exception e )
        {
            System.out.println("Unable to set all meters as offline.");
        }
    }

    /**
     * An abstraction for getting information from the database.
     * @author Bennett Andrews
     * @param field - InfoGET value to be fetched
     * @param Meter_id - The Meter_id address of the desired meter in String format.
     * @return The return is a ResultSet.
     */
    public static String getFrom(InfoGET field, String Meter_id) 
    {
        String sfield = columnFromInfoGET(field);
        String statement = "SELECT " + sfield + " FROM Meters" + " WHERE (Meter_id ='" + Meter_id + "')";
        String[][] results = sendMySQL(statement);
        return results[0][0];
    }
	
    /** 
     * An abstraction for verifying that a meter exists in the database. 
     * @author Bennett Andrews
     * @param Meter_id - Desired meter Meter_id as a string.
     * @return true/false - Meter is in the database already/Meter is not in the database.
     */
     public static boolean isMeterInDB(String Meter_id) 
     {
        String statement = "SELECT EXISTS (SELECT * FROM Meters WHERE(Meter_id='" + Meter_id + "'))";
        String[][] resultSet = sendMySQL(statement); //Note: this returns exactly one cell with 0 or 1.

        return resultSet[0][0].equals("1");
     }

    /**
     * Inserts a meter into the database in a new row for a given Meter_id address.
     * @author Bennett Andrews
     * @param Meter_id - Desired meter Meter_id as a string.
     * @return true/false - Insert was successful/unsuccessful.
     */
     public static boolean insertMeter(String Meter_id) 
     {
        String statement = "INSERT INTO Meters (Meter_id) VALUES (\"" + Meter_id + "\")";
        System.out.println("add sql: " + statement);

        try 
        {
            sendMySQL(statement);
            return true;
        } 
        catch (Exception e) 
        {
            // SQL failure
            System.out.println("Meter add unsuccessful for > " + Meter_id);
            return false;
        }
    }

    /**
     * Deletes a meter and its row in the database for a given Meter_id address.
     * @author Bennett Andrews
     * @param Meter_id - Desired meter Meter_id as a string.
     * @return true/false - Delete was successful/unsuccessful.
     */
    public static boolean deleteMeter(String Meter_id) 
    {
        String statement = "DELETE FROM Meters WHERE(Meter_id='" + Meter_id + "');";

        try 
        {
            sendMySQL(statement);
            return true;
        } 
        catch (Exception e) 
        {
            // SQL failure
            return false;
        }
    }

    /**
     * Fetches all the commands queued to be sent to a specified meter. Return is
     * formatted in an array of the form String[row][column]. <p>
     * 
     * Column return order:<p>
     * [Action index, Meter id, Command]
     * 
     * 
     * @author Bennett Andrews
     * @param Meter_id {@code String} - Meter identifier
     * @return Two dimensional array of values returned from the database query.
     */
    public static String[][] getCommandsForMeter(String Meter_id) {
        String statement = "SELECT i, Meter_id, Command FROM Actions WHERE(Meter_id='" + Meter_id + "' AND Send_attempts<" + MAX_SEND_ATTEMPTS + " AND Command<>'');";

        try 
        {
            String[][] response = sendMySQL(statement);
            return response;
        } 
        catch (Exception e) 
        {
            // SQL failure
            return new String[][]{{"error"}};
        }
    }

    /**
     * Fetches all meter IPs of meters that are flagged online in the database.
     * @author Bennett Andrews
     * @return Array of string meter ids
     */
    public static String[] getOnlineMeterIPs()
    {
        String statement = "SELECT IP_address FROM Meters WHERE( Online='1' );";

        try 
        {
            String[][] response = sendMySQL(statement); // This returns in array format [ [ip1], [ip2], [ip3], ... ]
                                                        // We want to convert this to [ip1, ip2, ip3... ]

            String[] parsedResponse = new String[ response.length ];

            for( int i = 0; i < response.length; i++ )
            {
                parsedResponse[i] = response[i][0];
            }
            // now it is good to go!

            return parsedResponse;
        } 
        catch (Exception e) 
        {
            // SQL failure
            return new String[]{"error"};
        }
    }


    /**
     * Call this function when an Action command is sent to the WiFi board.
     * Two rows are affected. <p>
     * Send_attempts -> incremented by one <p>
     * Send_time     -> updated with the current time
     * 
     * @author Bennett Andrews
     * @param Action_index - Database index of the action to be incremented.
     * @return true/false - increment successful/increment failure
     */
    public static boolean logSendAttempt(String Action_index) 
    {
        boolean success = false;

        String now = timestamp();

        String statement = "UPDATE Actions SET Send_attempts=Send_attempts+1, Send_time=CAST('" + now + "' AS DATETIME) WHERE (i='" + Action_index + "')";

        try 
        {
            sendMySQL(statement);
            success = true;
        } 
        catch (Exception e) 
        {
            // SQL failure
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    public static boolean logSuccess(String Action_index) 
    {
        boolean updated = false;

        String now = timestamp();

        String statement = "UPDATE Actions SET Sent=1, Send_time=CAST('" + now + "' AS DATETIME) WHERE (i='" + Action_index + "')";
        
        try 
        {
            sendMySQL(statement);
            updated = true;
        } 
        catch (Exception e) 
        {
            // SQL failure
            updated = false;
            e.printStackTrace();
        }
        return updated;
    }


    /**
     * Used to return a string format of the date. Used for MySQL timestamps.
     * @return System time in string format dd-MM-yyyy HH:mm:ss
     * @author Bennett Andrews
     */
    public static String timestamp() 
    {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
    

    /**
	 * Executes mysql query on database and returns response.
     * Response is a 2D array of the form: <p>
     * [ [ value , value, value,  ...  ],<p>
     *   [  value , value, value,  ...  ],<p>
     *   [  value , value, value,  ...  ] ]<p>
     * 
     * NOTE: The order of the columns matters and is dependent
     * on the SQL query.
     * 
     * @author Zachary Holsinger
     * @author Bennett Andrews
	 * @param statement - String SQL query
	 * @return Two dimensional array of values returned from the database query.
	 */
	public static String[][] sendMySQL(String statement) 
    {
        String DB_IP = "127.0.0.1";

		Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        int columnsNumber;
        int rowsNumber;

        String[][] resultString;
        String[] rowValues;

        try 
        {
            // Register JDBC driver
            Class.forName( JDBC_DRIVER );

            // Open a connection
            conn = DriverManager.getConnection(
                    "jdbc:mariadb://"+ DB_IP + ":" + DB_PORT + "/" + DATABASE, USER, PASS);

            // Generate SQL statement object
            stmt = conn.createStatement();

            // Send query to the database and receive ResultSet
            rs = stmt.executeQuery(statement);

            // Get the number of columns using the ResultSetMetaData object
            ResultSetMetaData rsmd = rs.getMetaData();
            columnsNumber = rsmd.getColumnCount();
            
            // Get the number of rows by setting the cursor to the last row,
            // returning the row number, then setting the cursor back to 
            // the beginning.
            rs.last();
            rowsNumber = rs.getRow();
            rs.beforeFirst();

            // Temporary array to store one row of values as the ResultSet is parsed.
            rowValues = new String[columnsNumber];

            // Initialize return results.
            resultString = new String[rowsNumber][columnsNumber];
            
            // For each row, add the row data to resultString
            for(int j = 0; j < rowsNumber; j++) {

                // Move the cursor to the next line
                rs.next();

                // For each item in the row, add a new value to the
                // rowValues array.
                for (int i = 1; i <= columnsNumber; i++) {
                    String columnValue = rs.getString(i);
                    rowValues[i-1] = columnValue;
                }

                // append rowValues to resultString
                resultString[j] = Arrays.copyOf(rowValues, rowValues.length);
            }
            
        } 
        catch (SQLException se) 
        {
            //Handle errors for JDBC
            se.printStackTrace();
            return null;
        } 
        catch (Exception e) 
        {
            //Handle errors for Class.forName
            e.printStackTrace();
            return null;
        }
        finally 
        {
            //STEP 7: Close the connection by any means necessary.
            try 
            {
                if (stmt != null) 
                {
                    conn.close();
                }

            } 
            catch (SQLException se) 
            {
            	return null;
            }// do nothing


            try 
            {
                if (conn != null)
                {
                    conn.close();
                }
            } 
            catch (SQLException se) 
            {
                se.printStackTrace();
            }

            
            if (rs != null) 
            {
                try 
                {
                    rs.close();
                } 
                catch (SQLException sqlEx) { } // ignore

                rs = null;
            }

            if (stmt != null) {
                try 
                {
                    stmt.close();
                } 
                catch (SQLException sqlEx) { } // ignore

                stmt = null;
            }
        }
		return resultString;
	}
	
	
	/**
	 * Tests to see if connection can be established to configured database
	 * @return true if connection can be established
	 */
	public static boolean testConnection() 
    {
        String DB_IP = "127.0.0.1";

		Connection conn = null;
        Statement stmt = null;
        try 
        {
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(
                    "jdbc:mariadb://"+ DB_IP + ":" + DB_PORT + "/" + DATABASE, USER, PASS);
            System.out.println("Connected database successfully...");
        } 
        catch (SQLException se) 
        {
            //Handle errors for JDBC
            se.printStackTrace();
            return false;
        } 
        catch (Exception e) 
        {
            //Handle errors for Class.forName
            e.printStackTrace();
            return false;
        } 
        finally 
        {
            //finally block used to close resources
        	//BENNETT YOU ARE AWESOME ALSO THIS COMMENT IS FOR GITHUB TESTING DELETE MEEEE
            try 
            {
                if (stmt != null) 
                {
                    conn.close();
                }
            } 
            catch (SQLException se) 
            {
            	return false;
            }// do nothing
            try 
            {
                if (conn != null) 
                {
                    conn.close();
                }
            } 
            catch (SQLException se) 
            {
                se.printStackTrace();
            }//end finally try

        }//end try

        System.out.println("Goodbye!"); 
		
		return true;
	}

}
