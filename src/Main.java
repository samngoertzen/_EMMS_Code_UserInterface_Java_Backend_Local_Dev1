import meter.Meter;
import wireless.MeterScan;

import java.util.Arrays;
import java.util.HashMap;

public class Main 
{
    MeterScan meterScan;
    HashMap<String, Meter> metersConnected;

    private static final int LOOP_DELAY = 5000;
    // TODO const ip ranges

    public static void main(String[] args)
    {
        Main main = new Main();

        while( true )
        {

            main.runLoop();

            try 
            {
                Thread.sleep( LOOP_DELAY );
            } 
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }   
    }

    Main()
    {
        metersConnected = new HashMap<String, Meter>();
        meterScan = new MeterScan();

        // TODO set ip ranges
    }

    public void runLoop()
    {
        // iterate through metersConnected
            // ping meter (method in Meter)
            // if connected, run meter (another method in Meter)
            // if disconnected, remove meter (update in database)

        Meter[] meterList = meterScan.getMeters( false );
        System.out.println( Arrays.deepToString( meterList ) );

        for( Meter meter : meterList )
        {
            String ip = meter.IP;

            if( metersConnected.containsKey( ip ) )
            {
                // TODO - if already in metersConnected, do nothing
                System.out.println("Duplicate found: " + meter.IP );
            }
            else 
            {
                // TODO - if not in metersConnected, insert
                System.out.println("Inserting new: " + meter.IP );
            }
        }


    }
}
