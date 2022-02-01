import meter.InfoGET;
import meter.Meter;
import wireless.MeterScan;

import java.util.Arrays;
import java.util.HashMap;

public class Main 
{
    MeterScan meterScan;
    HashMap<String, Meter> metersConnected;
    

    private static final int LOOP_DELAY = 5000;

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

            // Time the length of this loop and waith if it was too fast.
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

        meterScan.setN1S( 192 );
        meterScan.setN1E( 192 );
        meterScan.setN2S( 168 );
        meterScan.setN2E( 168 );
        meterScan.setN3S( 1 );
        meterScan.setN3E( 1 );
        meterScan.setN4S( 7 );
        meterScan.setN4E( 10 );
    }

    public void runLoop()
    {
        Meter[] meterList = meterScan.getMeters( false );

        for( Meter meter : meterList )
        {
            String ID = meter.getDatum( InfoGET.Meter_id );

            if( metersConnected.containsKey( ID ) )
            {
                System.out.println("Duplicate meter: " + ID );
                // do nothing else with duplicates
            }
            else 
            {
                System.out.println("Inserting new meter: " + ID );
                metersConnected.put( ID, meter );
            }
        }

        for( Meter meter : metersConnected.values() )
        {
            boolean connected = meter.run();

            if( !connected )
            {
                // TODO finish this
                // meter.remove from db();   
            }
        }
    }
}