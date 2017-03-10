package P2P.App;

import java.net.SocketException;

import P2P.PeerTracker.Server.TrackerThread;

public class Tracker {
	public static final int TRACKER_PORT = 4450;
	public static final int DEFAULT_P2P_CHUNK_SIZE = 4096;
	public static final double DEFAULT_CORRUPTION_PROBABILITY = 0.0;

	public static short p2pChunkSize = DEFAULT_P2P_CHUNK_SIZE;

	public static void main(String[] args) {
		double datagramCorruptionProbability = DEFAULT_CORRUPTION_PROBABILITY;
		
		/**
		 * Command line arguments to tracker are optional, if not specified, default values are used:
		 *  -chunk: size in bytes of the chunks used by peers when transferring files
		 *  -loss:  probability of corruption of received datagrams 
		 */
        int i = 0;
        String arg;

        while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];

            if (arg.equals("-loss") || arg.equals("-chunk")) {
                if (i < args.length) {
                	try {
                		if (arg.equals("-loss")) {		
                			datagramCorruptionProbability = Double.parseDouble(args[i++]);
                		}
                		else if (arg.equals("-chunk")) {
                			int val = Integer.parseInt(args[i++]); 
                			if (val < Short.MAX_VALUE && val > 0) 
                				p2pChunkSize = (short) val;
                			else {
            					System.err.println("Out of range value passed to option "+arg+", range: (0,"+Short.MAX_VALUE+"]");
            					return;
                			}
                		}
                	}
    				catch (NumberFormatException e) {
    					System.err.println("Wrong value passed to option "+arg);
    					return;
    				}
                }
                else
                    System.err.println("option "+arg+" requires a value");
            }
            else {
            	  System.err.println("Illegal option " + arg);
                  break;
            }
        }
		System.out.println("Probability of corruption for received datagrams: "+datagramCorruptionProbability);
		System.out.println("Maximum chunk size used by peers: "+p2pChunkSize);
		TrackerThread tt;
		try {
			tt = new TrackerThread("Tracker", TRACKER_PORT, datagramCorruptionProbability, p2pChunkSize);
			tt.start();
		} catch (SocketException e) {
			System.err.println("Tracker cannot create UDP socket on port "+TRACKER_PORT);
			System.err.println("Most likely a Tracker process is already running and listening on that port...");
			System.exit(-1);
		}
	}
}
