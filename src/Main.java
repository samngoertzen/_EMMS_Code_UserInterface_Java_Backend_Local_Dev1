import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import database.dbConnection;
import meter.Meter;
import wireless.MeterScan;

//Hi josh

public class Main 
{
    private static final int  IPV4_ADDRESS_1_START = 192;
    private static final int  IPV4_ADDRESS_1_END   = 192;
    private static final int  IPV4_ADDRESS_2_START = 168;
    private static final int  IPV4_ADDRESS_2_END   = 168;
    private static final int  IPV4_ADDRESS_3_START = 77;
    private static final int  IPV4_ADDRESS_3_END   = 77;
    private static final int  IPV4_ADDRESS_4_START = 10;
    private static final int  IPV4_ADDRESS_4_END   = 209;

    private static final int VERBOSITY = 2; // Global variable for how much output we want. 0 = none, 1 = errors only, 2 = all output.


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
                if( VERBOSITY >= 2 )
                {
                    System.out.println("Duplicate meter: " + meter.id() );
                }
                // do nothing else with meters that are already set up
            }
            else 
            {
                if( VERBOSITY >= 2 )
                {
                    System.out.println("Inserting new meter: " + meter.id() );
                }
                metersConnected.put( meter.id(), meter );
            }
        }

        Iterator<String> iterator = metersConnected.keySet().iterator();

        while( iterator.hasNext() )
        {
            String id = iterator.next();
            Meter meter = metersConnected.get( id );

            boolean connected = meter.run();

            if( !connected )
            {
                if( VERBOSITY >= 2 )
                {
                    System.out.println("Meter " + meter.id() + " offline. Removing.");
                }
                meter.setOfflineInDB();
                iterator.remove();
            }

            if( VERBOSITY >= 2 )
            {
                System.out.println( "\n- Checking for unsent commands -\n" );
            }

            // THIS section is to early-connect meters that need to have commands
            // pushed to them.
            
            String[] ids = dbConnection.getCommandMeter_ids();
            if( VERBOSITY >= 2 )
            {
                System.out.println( Arrays.toString( ids ) );
            } 

            for( int i = 0; i < ids.length; i++ )
            {
                if( metersConnected.containsKey( ids[i] ) )
                {
                    connected = metersConnected.get( ids[i] ).run();

                    if( !connected )
                    {
                        if( VERBOSITY >= 2 )
                        {
                            System.out.println("Meter " + meter.id() + " offline. Removing.");
                        }
                        meter.setOfflineInDB();
                        iterator.remove();
                    }
                }
            }
        }
    }
}