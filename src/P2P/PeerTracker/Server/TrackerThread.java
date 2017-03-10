package P2P.PeerTracker.Server;

import java.io.*;
import java.net.*;
import java.util.Vector;

import P2P.App.Tracker;
import P2P.PeerTracker.Message.Message;
import P2P.PeerTracker.Message.MessageConf;
import P2P.PeerTracker.Message.MessageControl;
import P2P.PeerTracker.Message.MessageFileInfo;
import P2P.PeerTracker.Message.MessageSeedInfo;
import P2P.PeerTracker.Message.MessageQuery;
//import P2P.PeerTracker.Message.ProtocolState;
import P2P.util.FileInfo;


public class TrackerThread extends Thread {

	protected DatagramSocket socket = null;
	protected boolean running = true;
	protected double messageCorruptionProbability;
	protected long peerToPeerProtocolChunkSize;
	/**
	 * 
	 * @param name Human readable name for this thread
	 * @param trackerPort Port number where the tracker thread will listen for datagrams
	 * @param corruptionProbability Probability of corruption for received datagrams
	 * @param maxChunkSize Maximum chunk size (in bytes) used by peers
	 * @throws SocketException when the datagram socket cannot be created
	 */
	public TrackerThread(String name, int trackerPort, double corruptionProbability, int maxChunkSize) throws SocketException {
		super(name);
		InetSocketAddress serverAddress = new InetSocketAddress(trackerPort);
		socket = new DatagramSocket(serverAddress);
		messageCorruptionProbability = corruptionProbability;
		peerToPeerProtocolChunkSize = maxChunkSize;
		running = true;
	}

	public void run() {
		byte[] buf = new byte[Message.MAX_UDP_PACKET_LENGTH];

		System.out.println("Tracker starting...");

		while(running) {
			try {

				// 1) Receive request
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);

				// 2) Figure out client 
				InetSocketAddress clientId = (InetSocketAddress) packet.getSocketAddress();

				// 3) Parse request and get type  
				Message request = Message.parseRequest(buf);
				
				if (request == null) {
					// Request failed to be parsed (e.g. due to CRC failure): Discard
					// Client will retransmit when timeout expires
					continue;
				}

				System.out.println("Tracker received request "+request+" from "+clientId);

				double rand = Math.random();
				if (rand < messageCorruptionProbability) {
					System.err.println("Tracker DISCARDED corrupt request "+request+" from "+clientId);					
					continue;
				}
				processRequestFromPeer(request, clientId.getAddress());				

				
				// 4) Stateless tracker: always accepts requests 

				// 5) Make response from this request (may comprise multiple fragments)
				Message [] response = makeResponse(request);

				for (int i=0; i < response.length; i++) {
					// 6) Create datagram packet from response 
					byte [] responseBuf = response[i].toByteArray();
					// 7) Send response back to client at address
					packet = new DatagramPacket(responseBuf, responseBuf.length, clientId);
					rand = Math.random();
					if (rand < messageCorruptionProbability) {
						System.err.println("Tracker DISCARDED corrupt response "+response[i]+" to "+clientId);					
						continue;
					}
					socket.send(packet);
					System.out.println("Tracker sent response: "+response[i]);
				}

			} catch (IOException e) {
				e.printStackTrace();
				running = false;
			}
		}
		socket.close();
	}
	
	/**
	 * Method that returns the corresponding tracker response message to a given request.
	 * @param request The request received by the tracker
	 * @param clientId The client peer that sent this request
	 * @return The response message that should be sent to the client
	 */
	public Message[] makeResponse(Message request) {
		byte respOpcode = request.getResponseOpcode();
		byte respTransId = request.getTransId();
		switch (respOpcode) {
			case Message.OP_REMOVE_SEED_ACK:
			case Message.OP_ADD_SEED_ACK:
				{
					Message[] response = new Message[1]; // Single fragment
					response[0] = new MessageControl(respOpcode, respTransId);
					return response;
				}
			case Message.OP_FILE_LIST:
				{
					assert(request instanceof MessageQuery); // Query files
					MessageQuery query = ((MessageQuery)request);
					FileInfo[] fileList = TrackerDatabase.db.queryFilesMatchingFilter(query.getFilterType(), query.getFilterValue());
					Vector <FileInfo []> fileListFragments = MessageFileInfo.computeFragments(fileList);
					Message[] response = new Message[fileListFragments.size()];
					assert(response.length < Byte.MAX_VALUE);
					for(byte i=0; i < response.length; i++) {
						response[i] = new MessageFileInfo(respOpcode, respTransId, 
								(byte)response.length, i, fileListFragments.get(i));
					}
					return response;
				}
			case Message.OP_SEED_LIST:
				{
					Message[] response = new Message[1]; // Single fragment???
					assert(request instanceof MessageSeedInfo); // Get seeds
					String fileHash = ((MessageSeedInfo)request).getFileHash();
					InetSocketAddress[] seedList = TrackerDatabase.db.getSeeds(fileHash);
					response[0] = new MessageSeedInfo(respOpcode, respTransId, seedList, fileHash);
					return response;
				}
			case Message.OP_SEND_CONF:
			{
				Message[] response = new Message[1]; // Single fragment
				response[0] = new MessageConf(respOpcode, respTransId, Tracker.p2pChunkSize);
				return response;
				
			}
			default:
				throw new IllegalArgumentException("Invalid response opcode: "+ respOpcode);
		}
	}

	/**
	 * Process a control request received by the tracker, from the given client. 
	 * Updates tracker database if necessary (currently, only upon disconnect.
	 */
	public void processRequestFromPeer(Message request, InetAddress clientAddr) {
		switch(request.getOpCode()) {
		case Message.OP_ADD_SEED:
		case Message.OP_REMOVE_SEED:
			int seederPort = ((MessageFileInfo)request).getPort();
			InetSocketAddress seedId = new InetSocketAddress(clientAddr, seederPort);
			FileInfo[] fileList = ((MessageFileInfo)request).getFileList();
			if (request.getOpCode() == Message.OP_ADD_SEED) {
				/* Tracker database uses socket address (IP+port where each peer listens
				 * for connections from other peers) as seed identifiers
				 */
				TrackerDatabase.db.addSeedToFileList(fileList, seedId);
			}
			else if (fileList.length > 0) {
				/* Update database, removing this seed from the given files' seed lists */
				TrackerDatabase.db.removeSeedFromFileList(fileList, seedId);
			}
			else {
				/* Update database, removing this seed from all seed lists, removing 
				 * record for the file if seed list empty */
				TrackerDatabase.db.disconnectPeer(seedId);
			}
			break;
		case Message.OP_QUERY_FILES:
		case Message.OP_GET_SEEDS:
			/* These requests do not require any update in database 
			 * */
		default:
		}
	}	
}
