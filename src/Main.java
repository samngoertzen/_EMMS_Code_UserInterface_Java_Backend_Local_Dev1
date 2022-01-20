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
        meterScan.setN4S( 2 );
        meterScan.setN4E( 15 );
    }

    public void runLoop()
    {
        Meter[] meterList = meterScan.getMeters( false );
        System.out.println( Arrays.deepToString( meterList ) );

        for( Meter meter : meterList )
        {

            if( metersConnected.containsKey( meter.Meter_id ) )
            {
                System.out.println("Duplicate meter found: " + meter.Meter_id );
                // do nothing else
            }
            else 
            {
                System.out.println("Inserting new: " + meter.Meter_id );
                metersConnected.put( meter.Meter_id, meter );
            }
        }

        for( Meter meter : metersConnected.values() )
        {
            // TODO ping meter (method in Meter)
            // TODO if connected, run meter (another method in Meter)
            // TODO if disconnected, remove meter (update in database)
        }
    }
}
