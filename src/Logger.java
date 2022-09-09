public class Logger 
{
    public static final int DEFAULT_LOG_LEVEL = 0B0000;
    public static final int LOG_ERRORS = 0B0001;
    public static final int LOG_RUNTIME = 0B0010;
    public static final int LOG_VERBOSE = 0B0100;

    // Add a comment here!

    private int log_level;

    public Logger() 
    {
        this( DEFAULT_LOG_LEVEL ); 
    }

    public Logger( int log_level )
    {
        this.log_level = log_level;
    }

    public void setLevel( int log_level )
    {
        this.log_level = log_level;
    }

    /**
     * Prints the output string to the console depending on the level of the logger. If the
     * log level of the output string is not enabled for the logger, the string will not print.
     * @author Bennett Andrews
     * @param output - String to be printed
     * @param output_level - Output level of the string to be printed
     */
    public void out( String output, int output_level )
    {
        if( (output_level & log_level) != 0 )
        {
            System.out.println( output );
        }
    }

    public static void main( String[] args )
    {
        Logger logger = new Logger();
    }    
}
