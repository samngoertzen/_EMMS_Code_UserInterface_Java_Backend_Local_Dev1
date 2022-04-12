package meter;

import java.util.Arrays;

/**
 * Collection of static methods for processing meter commands and checksums.
 * @author Bennett Andrews
 */
public class Checksum 
{
    public static final String START_DELIMETER = "!";
    public static final String STOP_DELIMETER = "*";
    public static final String CHECKSUM_DELIMETER = "$";
    public static final String ARG_DELIMETER = ";";

    /**
     * Verifies whether a string command is a valid meter command.
     * @author Bennett Andrews
     * @param command Command to be verified
     * @return true/false - is a vaild command/ is not a valid command
     */
    public static boolean isValidCommand( String command )
    {
        // Guard clause against null commands
        if ( command.length() == 0 ) 
        {
            System.out.println("Checksum: Null command.");
            return false;
        }

        // Guard clause against commands missing the
        // start and stop delimeters.
        if ( !command.startsWith( START_DELIMETER ) )
        {
            System.out.println("Checksum: Invalid command syntax. '" + command + "' has no start delimeter.");
            return false;
        }

        if ( !command.endsWith( STOP_DELIMETER ) )
        {
            System.out.println("Checksum: Invalid command syntax. '" + command + "' has no stop delimeter.");
            return  false;
        }

        // Ensure that if there are multiple parameters, none of them are null
        if( command.indexOf( ARG_DELIMETER ) > -1)
        {
            
            String[] params = command.substring(1, command.length() - 1 ).split( ARG_DELIMETER );

            for( String param : params )
            {
                if( (param == null) || ( param.equals("") ) )
                {
                    System.out.println("Command has null parameters.");
                    return false;
                }
            }
        }

        // If it passes all the checks, it is a vaild command
        return true;
    }

    /**
     * Converts a meter command from the simple command syntax to
     * the checksum-implemented syntax. The checksum is a sum of
     * the non-delimeter ASCII characters in the command, delimeted
     * by a percent sign. Returns null if invalid command. <p>
     * 
     * Ex. <p>
     * command - "!Set;Lights;On*" <p>
     * checksum - "!Set;Lights;On%1266*"
     * 
     * @author Bennett Andrews
     * @param command (String) command to be sent to the meter.
     * @return (String) formatted command with the checksum value.
     */
    public static String convert( String command )
    {
        // Guard clause against invalid commands
        if( !isValidCommand( command ) )
        {
            return null;
        }

        // Guard clause against converting a command with a checksum delimeter already present.
        if( command.indexOf( CHECKSUM_DELIMETER ) != -1 )
        {
            System.out.println("Checksum: Already contains checksum delimeter.");
            return null;
        }

        int checksum = sum(command);

        String asterisklessCommand = command.substring(0, command.length() - 1);
        return  asterisklessCommand + CHECKSUM_DELIMETER + checksum + "*";
    }

    /**
     * Evaluates the checksum value of a command. Sums the ASCII values of the
     * characters in the command. Excludes the first and last
     * characters since the checksum does not include start and stop delimeters.
     * 
     * <p>
     * Ex. command "!Set;Lights;On*" <p>
     * checksum evaluates - "Set;Lights;On" <p>
     * and returns - 1266
     * 
     * @author Bennett Andrews
     * @param command (String) command to be sent to the meter.
     * @return (int) checksum value for the command.
     */
    public static int sum(String command) 
    {
        // Accumulating checksum
        int checksum = 0;

        // Loop start and stop conditions.
        int start_char = 0;
        int end_char = command.length();

        // If a checksum is already present, chop it off.
        int split_at = command.indexOf( CHECKSUM_DELIMETER );
        if( split_at != -1 )
        {
            command = command.substring(0, split_at );
        }

        // If the command starts with an '!', do not count it in the checksum.
        if( command.startsWith( START_DELIMETER ) )
        {
            start_char = 1;
        }

        // If the command ends with a '*', do not count it in the checksum.
        // If a checksum was already chopped off, this asterisk is already removed.
        if( command.endsWith( STOP_DELIMETER ) )
        {
            end_char--;
        }

        // For each letter in the command (excluding delimeters), increment
        // checksum by the ASCII value.
        for( int i = start_char; i < end_char; i++ )
        {
            checksum += command.charAt(i);
        }

        return checksum;
    }

    /**
     * Verifies whether a command is valid has a valid checksum.
     * <p>* Checks start, stop, and checksum delimeters
     * <p>* Checks against too many delimeters
     * <p>* Checks for potential null parameters
     * <p>* Verifies that the command checksum is a valid numeric type that
     * matches the checksum of the command given by sum().
     * @author Bennett Andrews
     * @param command Command to be checked.
     * @return true/false - is valid/is not valid
     * @see sum()
     */
    public static boolean isVerified( String command )
    {
        // Guard clause against invalid commands
        if( !isValidCommand( command ) )
        {
            return false;
        }

        String[] checksum_split = command.split( "\\" + CHECKSUM_DELIMETER ); // curse you regex

        // Guard clause against no checksum delimeter or too many checksum delimeters.
        if( checksum_split.length != 2 )
        {
            System.out.println("Checksum: Incorrect number of delimeters.");
            return false;
        }

        
        int calculated = sum( checksum_split[0] );
        int expected = calculated + 1; // Assume that the checksum is incorrect
                                       // until the actual expected checksum is parsed.

        checksum_split[1] = checksum_split[1].replaceAll("[^\\d.]", ""); // keep only numbers in the string

        try
        {
            expected = Integer.parseInt( checksum_split[1] );
        }
        catch( NumberFormatException e )
        {
            System.out.println("Checksum: Non-numeric checksum");
            return false;
        }

        if( calculated == expected )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //  !!!
    public static String[] separateMultipleCommands( String commandstr )
    {
        return commandstr.split( "(?=\\" + START_DELIMETER + ")" ); // regex is pain. this is a zero-width lookahead?
        // https://stackoverflow.com/questions/4416425/how-to-split-string-with-some-separator-but-without-removing-that-separator-in-j
    }


    public static void main(String[] args) 
    {
        // /**
        //  * 
        //  * CONVERT TESTS
        //  * 
        //  */
        // // Invalid command syntax
        // String action = "!Set;Lights;On";
        // System.out.println("\nCommand to check: " + action );
        // System.out.println( convert(action) );

        // // Invalid command syntax
        // action = "Set;Lights;On*";
        // System.out.println("\nCommand to check: " + action );
        // System.out.println( convert(action) );

        // // Null command error
        // action = "";
        // System.out.println("\nCommand to check: " + action );
        // System.out.println( convert(action) );

        // // ASCII value of a = 97
        // action = "!a*";
        // System.out.println("\nCommand to check: " + action );
        // System.out.println( convert(action) );

        // // ASCII value of b = 98
        // // a + b = 95
        // action = "!ab*";
        // System.out.println("\nCommand to check: " + action );
        // System.out.println( convert(action) );

        // action = "!Set;Lights;On*";
        // System.out.println("\nCommand to check: " + action );
        // System.out.println( convert(action) );

        // /**
        //  * 
        //  * CHECK TESTS
        //  * 
        //  */
        // // Too many delimeters
        // String response = "!Read;CBv$er$905*";
        // System.out.println( "\nResponse to check: " + response );
        // System.out.println( isVerified(response) );

        // // Too few delimeters
        // response = "!Read;CBver905*";
        // System.out.println( "\nResponse to check: " + response );
        // System.out.println( isVerified(response) );

        // // Null parameters
        // response = "!Read;;CBver$905*";
        // System.out.println( "Response to check: " + response );
        // System.out.println( isVerified(response) );

        // // Non-numeric checksum
        // response = "!Read;CBver$*";
        // System.out.println( "\nResponse to check: " + response );
        // System.out.println( isVerified(response) );

        // // Correct format, incorrect sum
        // response = "!Read;CBver$904*";
        // System.out.println( "\nResponse to check: " + response );
        // System.out.println( isVerified(response) );

        // // Correct format and sum
        // response = "!Read;CBver$905*";
        // System.out.println( "\nResponse to check: " + response );
        // System.out.println( isVerified(response) );

        System.out.println( Arrays.deepToString( separateMultipleCommands( "!Read;CBver$905*!Read;CBver$905*" ) ) );
    }
}
