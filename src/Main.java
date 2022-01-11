import meter.Meter;
import wireless.MeterScan;
import java.util.HashMap;

public class Main 
{
    HashMap<String, Meter> metersConnected;

    Main()
    {
        metersConnected = new HashMap<String, Meter>();
    }



    public static void main(String[] args) 
    {
        while( true )
        {
            // iterate through metersConnected
                // ping meter
                // if connected, run meter
                // if disconnected, remove meter (update in database)

            Object[] meterList = MeterScan.getMeters();

            // iterate through meterList
                // if already in metersConnected, do nothing
                // if not in metersConnected, insert

            try 
            {
                Thread.sleep( 5000 );
            } 
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }   
    }
}
