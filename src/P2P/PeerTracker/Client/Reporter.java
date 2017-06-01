package P2P.PeerTracker.Client;

import java.io.IOException;
import java.net.*;
import java.util.Vector;

import P2P.PeerTracker.Message.Message;
import P2P.PeerTracker.Message.MessageFileInfo;

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
	private Vector<Message> fragments;
	
	private int attemps;
	public final int PORT = 4450;
	public final int MAX_MSG_SIZE_BYTES = 1024;
	public final int N_ATTEMPS = 12;
	

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
		fragments = new Vector<Message>();
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

			socket.setSoTimeout(100);
			socket.receive(pckt);
		} catch (IOException e) {
			return null;
		}

		return Message.parseResponse(pckt.getData());
	}

	@Override
	public Message conversationWithTracker(Message request) {
		attemps = N_ATTEMPS;
		fragments.clear();
		Message m;
		do {
			sendMessageToTracker(peerTrackerSocket, request, addr);
			do { //Send each fragment
				m = receiveMessageFromTracker(peerTrackerSocket);
			} while (!isProcessFragments(m));

			attemps--;
		} while (m == null && attemps > 0);

		if (attemps == 0)
			return null;
		else
			return buildFullMessage(m);
	}

	private boolean isProcessFragments(Message x) {

		//If the file need to be fragmented
		if (x instanceof MessageFileInfo) {
			attemps = N_ATTEMPS;
			MessageFileInfo m = (MessageFileInfo) x;
			fragments.addElement(x);
			return fragments.size() == m.getTotalFragments();
		}
		return true;
	}

	/**
	 * Assemble the fragments of the message
	 */
	private Message buildFullMessage(Message x) {
		if ((x instanceof MessageFileInfo) && fragments.size() > 1) {
			fragments.get(0).reassemble(fragments);
			return fragments.get(0);
		}
		return x;
	}
}
