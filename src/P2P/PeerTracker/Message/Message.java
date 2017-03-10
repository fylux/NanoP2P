
package P2P.PeerTracker.Message;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import P2P.util.FileDigest;
import P2P.util.FileInfo;

/**
* Abstract class that models peer-tracker messages without a specific format
*
* @author rtitos
*
*/

public abstract class Message
{
	/**
	 * Maximum size of the buffer used to received packets 
	 */
	public static final int MAX_UDP_PACKET_LENGTH = 1450;

	/**
	 * Size of "opcode" field: byte (1 bytes)
	 */
	protected static final int FIELD_OPCODE_BYTES = 1;

	/**
	 * Size of "list-len" field: short (2 bytes)
	 */
	protected static final int FIELD_LONGLIST_BYTES = Short.SIZE / 8;
	/**
	 * Size of "hash" field(s) used by subclasses (160 bits in SHA-1, 20 bytes)
	 */
	protected static final int FIELD_FILEHASH_BYTES = FileDigest.getFileDigestSize();
	/**
	 * Size of "port" field: short (2 bytes)
	 */
	protected static final int FIELD_PORT_BYTES = Short.SIZE / 8;  
	/**
	 * Size of "trans_id" field: 1 byte
	 */
	protected static final byte FIELD_TRANSID_BYTES = 1;

	/**
	 * Opcodes in the peer-tracker protocol of nanoP2P
	 */
	public static final byte INVALID_OPCODE = 0;
	public static final byte OP_ADD_SEED = 1;
	public static final byte OP_ADD_SEED_ACK = 2;
	public static final byte OP_QUERY_FILES = 3;
	public static final byte OP_FILE_LIST = 4;
	public static final byte OP_GET_SEEDS = 5;
	public static final byte OP_SEED_LIST = 6;
	public static final byte OP_REMOVE_SEED = 7;
	public static final byte OP_REMOVE_SEED_ACK = 8;
	public static final byte OP_GET_CONF = 9;
	public static final byte OP_SEND_CONF = 10;
	
	/**
	 * Message opcode.     
	 */
	private byte opCode;
	
	/**
	 * Message transaction ID.     
	 */
	private byte transId;

	/**
	 * Current transaction ID.     
	 */
	private static byte nextTransId = 0;
	
	/*
	 * Validity flag used for correctness check (asserts)     
	 */
	protected boolean valid; // Flag set by fromByteArray upon success 


	/*
	 * Abstract methods whose implementation depends on the message format.     
	 */
	protected abstract boolean fromByteArray(byte[] buf);

	public abstract byte[] toByteArray();

	public abstract String toString();

	/**
	 * @return The total number of fragments the original message was split into.
	 */
	public abstract int getTotalFragments();

	/**
	 * Reassembles a set of fragments into one of the fragments,
	 * obtaining the original message before it was split. 
	 * @param fragments All the fragments of the message, including this object
	 */
	public abstract void reassemble(Vector<Message> fragments);

	/**
	 * @return True if this message consists of several fragments.
	 */
	public abstract boolean fragmented();
	/**
	 * Default class constructor, creates "empty" message in invalid state 
	 */
	public Message()
	{
		opCode = INVALID_OPCODE;
		valid = false;
	}

	private final void sanityCheck() {
		if (!valid) throw new RuntimeException("Message object accessed before correct initialization");
	}
	public final byte getOpCode() {
		sanityCheck();
		return opCode;
	}

	public static synchronized byte fetchAndIncrementTransId() {
		if (nextTransId+1 < nextTransId) {
			System.err.println("Rollover occured in TransId counter");
		}
		return nextTransId++;
	}

	public final byte getTransId() {
		sanityCheck();
		return transId;
	}

	public final String getOpCodeString() {
		sanityCheck();
		switch (opCode) {
		case OP_REMOVE_SEED:
			return "REMOVE_SEED";
		case OP_REMOVE_SEED_ACK:
			return "REMOVE_SEED_ACK";
		case OP_ADD_SEED:
			return "ADD_SEED";
		case OP_ADD_SEED_ACK:
			return "ADD_SEED_ACK";
		case OP_QUERY_FILES:
			return "QUERY_FILES";
		case OP_FILE_LIST:
			return "FILE_LIST";
		case OP_GET_SEEDS:
			return "GET_SEEDS";
		case OP_SEED_LIST:
			return "SEED_LIST";
		case OP_GET_CONF:
			return "GET_CONF";
		case OP_SEND_CONF:
			return "SEND_CONF";
		default:
			return "INVALID_TYPE";
		}
	}
	
	/**
	 * @param opCode
	 */

	protected final void setOpCode(byte opCode) {
		assert(!valid);
		_check_opcode(opCode);

		this.opCode = opCode;
	}

	protected final void setTransId(byte id) {
		this.transId = id;
	}

	public final void setNewTransId() {
		this.transId = fetchAndIncrementTransId();
	}

	public static Message makeAddSeedRequest(int seederPort, FileInfo[] fileList) {
		byte requestOpcode = OP_ADD_SEED;
		byte tid = fetchAndIncrementTransId();
		return new MessageFileInfo(requestOpcode, tid, seederPort, fileList);
	}

	public static Message makeRemoveSeedRequest(int seederPort, FileInfo[] fileList) {
		byte requestOpcode = OP_REMOVE_SEED;
		byte tid = fetchAndIncrementTransId();
		return new MessageFileInfo(requestOpcode, tid, seederPort, fileList);
	}

	public static Message makeQueryFilesRequest(byte filterType, String filter) {
		byte tid = fetchAndIncrementTransId();
		return new MessageQuery(OP_QUERY_FILES, tid, filterType, filter);
	}

	public static Message makeGetSeedsRequest(String fileHash) {
		InetSocketAddress[] seedList = new InetSocketAddress[0];
		byte tid = fetchAndIncrementTransId();
		return new MessageSeedInfo(OP_GET_SEEDS, tid, seedList, fileHash);
	}

	public static Message makeGetConfRequest() {
		byte tid = fetchAndIncrementTransId();
		return new MessageControl(OP_GET_CONF, tid);
	}

	/**
	 * Class method to parse a request message received by the tracker
	 * @param buf The byte array of the received packet 
	 * @return A message of the appropriate format representing this request 
	 */
	public static Message parseRequest(byte[] buf)
	{ 
		if (buf.length < FIELD_OPCODE_BYTES + FIELD_TRANSID_BYTES) {
			throw new IllegalArgumentException("Failed to parse request: byte[] argument has length "+buf.length);
		}
		byte reqOpcode = buf[0];
		byte transId = buf[1];
		switch(reqOpcode) {
			case OP_QUERY_FILES:
				return new MessageQuery(buf); 
			case OP_GET_CONF:
				return new MessageControl(reqOpcode, transId); 
			case OP_REMOVE_SEED:
			case OP_ADD_SEED:
				return new MessageFileInfo(buf); 
			case OP_GET_SEEDS:
				return new MessageSeedInfo(buf);
			default:
				throw new IllegalArgumentException("Invalid request opcode: "+reqOpcode);
		}
	}

	/**
	 * Class method to parse a response message received by the client
	 * @param buf The byte array of the packet received from the tracker 
	 * @return A message of the appropriate format representing this response 
	 */
	public static Message parseResponse(byte[] buf)
	{ 
		if (buf.length < FIELD_OPCODE_BYTES + FIELD_TRANSID_BYTES) {
			throw new IllegalArgumentException("Failed to parse response: buffer has length "+buf.length);
		}
		byte respOpcode = buf[0];

		switch(respOpcode) {
			case OP_REMOVE_SEED_ACK:
			case OP_ADD_SEED_ACK:
			return new MessageControl(buf); 
			case OP_FILE_LIST:
				return new MessageFileInfo(buf); 
			case OP_SEED_LIST:
				return new MessageSeedInfo(buf);
			case OP_SEND_CONF:
				return new MessageConf(buf);
			default:
				throw new IllegalArgumentException("Failed to parse message: Invalid response opcode "+respOpcode);
		}
	}	
	

	public byte getResponseOpcode() {
		assert(valid);
		if (opCode == OP_REMOVE_SEED) {
			return OP_REMOVE_SEED_ACK;
		}
		else if (opCode == OP_ADD_SEED) {
			return OP_ADD_SEED_ACK;
		}
		else if (opCode == OP_QUERY_FILES) {
			return OP_FILE_LIST;
		}
		else if (opCode == OP_GET_SEEDS) {
			return OP_SEED_LIST;
		}
		else if (opCode == OP_GET_CONF) {
			return OP_SEND_CONF;
		}
		else {
			throw new RuntimeException("Opcode " + opCode + " is not a valid request code or it has no response.");
		}
	}

	/* To check opcode validity */
	private static final Byte[] _valid_opcodes = {
			OP_REMOVE_SEED,
			OP_REMOVE_SEED_ACK,
			OP_ADD_SEED,
			OP_ADD_SEED_ACK,
			OP_QUERY_FILES,
			OP_FILE_LIST,
			OP_GET_SEEDS,
			OP_SEED_LIST,
			OP_GET_CONF,
			OP_SEND_CONF,
			};
	private static final Set<Byte> valid_opcodes =
		Collections.unmodifiableSet(new HashSet<Byte>(Arrays.asList(_valid_opcodes)));

	// Protected to allow overriding in subclasses
	protected void _check_opcode(byte opcode)
	{
		if (!valid_opcodes.contains(opcode))
			throw new RuntimeException("Opcode " + opcode + " no es válido.");
	}
}
