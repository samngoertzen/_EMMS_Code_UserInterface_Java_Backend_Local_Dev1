package meter;

public class Checksum {

    /**
     * Converts a meter command from the simple command syntax to
     * the checksum-implemented syntax. The checksum is a sum of
     * the non-delimeter ASCII characters in the command, delimeted
     * by a percent sign. <p>
     * 
     * Ex. <p>
     * simple - "!Set;Lights;On*" <p>
     * checksum - "!Set;Lights;On%1266*"
     * 
     * @author Bennett Andrews
     * @param command (String) command to be sent to the meter.
     * @return (String) formatted command with the checksum value.
     */
    public static String convert(String command) {

        // Guard clause against null commands
        if ( command.length() == 0 ) {
            System.out.println("Null command.");
            return null;
        }

        // Guard clause against commands missing the
        // start and stop delimeters.
        char firstChar = command.charAt(0);
        char lastChar = command.charAt( command.length() - 1 );

        if (firstChar != '!') {
            System.out.println("Invalid command syntax. '" + command + "' has no start delimeter.");
            return null;
        }

        if (lastChar != '*') {
            System.out.println("Invalid command syntax. '" + command + "' has no stop delimeter.");
            return null;
        }

        int checksum = sum(command);

        return command.substring(0, command.length() - 1) + "%" + checksum + "*";
    }

    /**
     * Evaluates the checksum value of a command. Sums the ASCII values of the
     * characters in the command. Excludes the first and last
     * characters since the checksum does not include start and stop delimeters.
     * <p>
     * Ex. command "!Set;Lights;On*" <p>
     * checksum evaluates - "Set;Lights;On" <p>
     * and returns - 1266
     * 
     * @author Bennett Andrews
     * @param command (String) command to be sent to the meter.
     * @return (int) checksum value for the command.
     */
    public static int sum(String command) {

        // Accumulating checksum
        int checksum = 0;

        // For each letter in the command (excluding delimeters), increment
        // checksum by the ASCII value.
        for (int i = 1; i < command.length() - 1; i++) {
            checksum += command.charAt(i);
        }

        return checksum;
    }


    public static void main(String[] args) {

        String action;

        // Invalid command syntax
        action = "!Set;Lights;On";
        System.out.println( convert(action) );

        // Invalid command syntax
        action = "Set;Lights;On*";
        System.out.println( convert(action) );

        // Null command error
        action = "";
        System.out.println( convert(action) );

        // ASCII value of a = 97
        action = "!a*";
        System.out.println( convert(action) );

        // ASCII value of b = 98
        // a + b = 95
        action = "!ab*";
        System.out.println( convert(action) );

        action = "!Set;Lights;On*";
        System.out.println( convert(action) );
    }
}
