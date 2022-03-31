import java.util.HashMap;

import database.dbConnection;
import meter.Meter;
import wireless.MeterScan;



public class Main 
{
    private static final int  IPV4_ADDRESS_1_START = 192;
    private static final int  IPV4_ADDRESS_1_END   = 192;
    private static final int  IPV4_ADDRESS_2_START = 168;
    private static final int  IPV4_ADDRESS_2_END   = 168;
    private static final int  IPV4_ADDRESS_3_START = 1;
    private static final int  IPV4_ADDRESS_3_END   = 1;
    private static final int  IPV4_ADDRESS_4_START = 3;
    private static final int  IPV4_ADDRESS_4_END   = 5;


    MeterScan meterScan;
    HashMap<String, Meter> metersConnected;
    
    private static final int LOOP_DELAY = 5000; // ensure that loops take at least this long (msec)

    
    public static void main(String[] args)
    {
        Main main = new Main();
        long time_current;
        long time_elapsed;
        long delay;

        while( true )
        {
            time_current = System.currentTimeMillis();

            main.runLoop();

            // Time the length of this loop and wait if it was too fast.
            time_elapsed = System.currentTimeMillis() - time_current;
            delay = LOOP_DELAY - time_elapsed;
            
            if( delay > 0 )
            {
                try 
                {
                    Thread.sleep( delay );
                } 
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }   
    }

    Main()
    {
        metersConnected = new HashMap<String, Meter>();
        meterScan = new MeterScan();

        // Configure ranges for IP scanning
        meterScan.setN1S( IPV4_ADDRESS_1_START );
        meterScan.setN1E( IPV4_ADDRESS_1_END );
        meterScan.setN2S( IPV4_ADDRESS_2_START );
        meterScan.setN2E( IPV4_ADDRESS_2_END );
        meterScan.setN3S( IPV4_ADDRESS_3_START );
        meterScan.setN3E( IPV4_ADDRESS_3_END );
        meterScan.setN4S( IPV4_ADDRESS_4_START );
        meterScan.setN4E( IPV4_ADDRESS_4_END );

        // flag all meters offline. because we don't know the status
        // of meters when the program starts, assume all meters are offline
        // until we can contact them and check for sure.
        dbConnection.setAllMetersOffline();
    }

    public void runLoop()
    {
        Meter[] meterList = meterScan.getMeters( false );

        for( Meter meter : meterList )
        {

            if( metersConnected.containsKey( meter.id() ) )
            {
                System.out.println("Duplicate meter: " + meter.id() );
                // do nothing else with meters that are already set up
            }
            else 
            {
                System.out.println("Inserting new meter: " + meter.id() );
                metersConnected.put( meter.id(), meter );
            }
        }

        for( Meter meter : metersConnected.values() )
        {
            boolean connected = meter.run();

            if( !connected )
            {
                System.out.println("Meter " + meter.id() + " offline. Removing.");
                meter.setOfflineInDB();
                metersConnected.remove( meter.id() );
            }
        }
    }
}