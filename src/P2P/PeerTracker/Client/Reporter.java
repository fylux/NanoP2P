package P2P.PeerTracker.Client;

import java.io.IOException;
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

	private InetSocketAddress addr;
	public final int PORT = 4450;
	public final int MAX_MSG_SIZE_BYTES = 1024;

	/***
	 * 
	 * @param tracker
	 *            Tracker hostname or IP
	 */
	public Reporter(String tracker) {
		trackerHostname = tracker;
		addr = new InetSocketAddress(trackerHostname, PORT);
		try {
			peerTrackerSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.err.println("Reporter cannot create datagram socket for communication with tracker");
			System.exit(-1);
		}
	}

	public void end() {
		// Close datagram socket with tracker
		peerTrackerSocket.close();
		
	}

	@Override
	public boolean sendMessageToTracker(DatagramSocket socket, Message request, InetSocketAddress trackerAddress) {
		
		 byte[] buf = request.toByteArray();
		
		
		DatagramPacket pckt = new DatagramPacket(buf, buf.length, addr);
		try {
			socket.send(pckt);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public Message receiveMessageFromTracker(DatagramSocket socket) {

		byte[] buf = new byte[Message.MAX_UDP_PACKET_LENGTH];

		DatagramPacket pckt = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(pckt);
		} catch (IOException e) {
		}

		return Message.parseResponse(pckt.getData());
	}

	@Override
	public Message conversationWithTracker(Message request) {
		sendMessageToTracker(peerTrackerSocket, request, addr);
		return receiveMessageFromTracker(peerTrackerSocket);
	}

}
