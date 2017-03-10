package P2P.PeerTracker.Client;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import P2P.PeerTracker.Message.Message;

public interface ReporterIface {


	/***
	 * Sends a request message to the tracker.
	 * @param socket
	 *            The datagram socket used for communication with the tracker 
	 * @param request
	 *            The request message to be sent to the tracker
	 * @param trackerAddress
	 *            The destination socket address where the tracker listens for
	 *            requests
	 * @return True if the request is sent successfully
	 */
	public boolean sendMessageToTracker(DatagramSocket socket, Message request,
			InetSocketAddress trackerAddress);
	
	/***
	 * Receives a response message from the tracker.
	 * @param socket
	 *            The datagram socket used for communication with the tracker,
	 * @return The response message received from the tracker, or null if
	 *         it was not possible to receive any message (for whatever reason)
	 */
	public Message receiveMessageFromTracker(DatagramSocket socket);

	/***
	 * Sends a request message to the tracker and receives the corresponding
	 * response(s), reassembling all datagrams into a single message if the
	 * response consists of several fragments. This method must handle
	 * datagram loss using timeout and request retransmission.
	 * @param request The request message to be sent to the tracker.
	 * @return The response message to the request received from the tracker.
	 */
	public Message conversationWithTracker(Message request);

}
