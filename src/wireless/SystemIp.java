package wireless;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class SystemIp {
	// I did not make this code, gathered from the internet and modified to add more linux IPV4 Systems
	public static String getSystemIP(){
        try{
            String sysIP="";
            String OSName=  System.getProperty("os.name");
	    if(OSName.contains("Windows")){
                sysIP   =InetAddress.getLocalHost().getHostAddress();
	    }
	    else{
	    	sysIP=getSystemIP4Linux("wlp2s0");
	    	if(sysIP==null){
                    sysIP=getSystemIP4Linux("wlp2s0");
		    if(sysIP==null){
		  	sysIP=getSystemIP4Linux("wlan0");
                        if(sysIP==null){
                            sysIP=getSystemIP4Linux("usb0");
                        }
                    }
	   	}
	    }
	    return sysIP;
	}
	catch(Exception E){
            return null;
	}
    }	
	
	private static String getSystemIP4Linux(String name){
        try{
            String ip="";
            NetworkInterface networkInterface = NetworkInterface.getByName(name);
            Enumeration<InetAddress> inetAddress = networkInterface.getInetAddresses();
            InetAddress currentAddress = inetAddress.nextElement();
            while(inetAddress.hasMoreElements()){
                currentAddress = inetAddress.nextElement();
                if(currentAddress instanceof Inet4Address && !currentAddress.isLoopbackAddress()){
                    ip = currentAddress.toString();
                    break;
                }
            }
            if(ip.startsWith("/")){
                ip=ip.substring(1);
            }
            return ip;
        } 
        catch (Exception E) {
            System.err.println("System Linux IP Exp : "+E.getMessage());
            return null;
        }
    }
}
