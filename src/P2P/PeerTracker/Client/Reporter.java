package P2P.PeerTracker.Client;

import java.net.*;

import P2P.PeerTracker.Message.Message;

public class Reporter implements ReporterIface {

	/**
	 * Tracker hostname, used for establishing connection
	 */
	private String trackerHostname;

	/**
	 * UDP socket for communication with tracker
	 */
	private DatagramSocket peerTrackerSocket;

	/***
	 * 
	 * @param tracker
	 *            Tracker hostname or IP
	 */
	public Reporter(String tracker) {
		trackerHostname = tracker;
		try {
			peerTrackerSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.err
					.println("Reporter cannot create datagram socket for communication with tracker");
			System.exit(-1);
		}
	}

	public void end() {
		// Close datagram socket with tracker
		peerTrackerSocket.close();
	}

	@Override
	public boolean sendMessageToTracker(DatagramSocket socket, Message request,
			InetSocketAddress trackerAddress) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Message receiveMessageFromTracker(DatagramSocket socket) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message conversationWithTracker(Message request) {
		// TODO Auto-generated method stub
		return null;
	}


}
