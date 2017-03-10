package P2P.PeerTracker.Message;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


public class MessageConf extends Message {

	/**
	 * @author rtitos
	 * 
	 * Peer-tracker protocol data message, format "Conf" 
	 * 
	    1b      1b          2b          
	 +-----------------------------+
	 |opcode| trans_id | chunksize |
	 +------+----------+-----------+
	 */
	/**
	 * Size of "chunksize" field: short (2 bytes)
	 */
	private static final int FIELD_CHUNKSIZE_BYTES = Short.SIZE / 8;  

	/**
	 * Message opcodes that use the Conf format
	 */
	private static final Byte[] _conf_opcodes = {
		OP_SEND_CONF};


	/**
	 * The chunk size.
	 */
	private short chunkSize;

	/**
	 * Constructor used by tracker
	 * @param opCode Message type
	 * @param p2pChunkSize The chunk size
	 */
	public MessageConf(byte opCode, byte tid, short p2pChunkSize) {
		setOpCode(opCode);
		setTransId(tid);
		this.chunkSize = p2pChunkSize;
		valid = true;
	}

	/**
	 * Constructor used by client when creating message response after receiving
	 * @param buf
	 */
	public MessageConf(byte[] buf) {
		if (fromByteArray(buf) == false) {
			throw new RuntimeException("Failed to parse message: format is not Conf.");
		}
		else {
			assert(valid);
		}
	}

	/**
	 * Creates a byte array ready for sending a datagram packet, from a valid message of Conf format 
	 */
	public byte[] toByteArray()
	{
		int byteBufferLength = FIELD_OPCODE_BYTES + FIELD_TRANSID_BYTES + FIELD_CHUNKSIZE_BYTES;

		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		// Opcode
		buf.put((byte)this.getOpCode());

		// Trans id
		buf.put((byte)this.getTransId());

		// Chunk size
		buf.putShort((short)this.getChunkSize());

		return buf.array();
	}

	/**
	 * Creates a valid message of Conf format, from the byte array of the received packet
	 */
	protected boolean fromByteArray(byte[] array) {
		if (array.length < FIELD_OPCODE_BYTES + FIELD_CHUNKSIZE_BYTES + FIELD_TRANSID_BYTES) {
			System.err.println("Contenido del byte array "+array+" no es mensaje con formato Conf");
			throw new RuntimeException("Byte array no contiene un mensaje con formato Conf");
		}
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			// Opcode
			setOpCode(buf.get());
			// Trans id
			setTransId(buf.get());
			// Chunk size
			setChunkSize(buf.getShort());
			// Finally set valid flag
			valid = true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			assert(valid == false);
		}
		return valid;
	}	


	public short getChunkSize() {
		return this.chunkSize;
	}

	private void setChunkSize(short size) {
		this.chunkSize= size;
	}

	public String toString() {
		assert(valid);
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" Type:"+this.getOpCodeString());
		strBuf.append(" TransId:"+this.getTransId());
		strBuf.append(" ChunkSize:"+this.getChunkSize());
		return strBuf.toString();
	}

	/**
	 * For checking opcode validity. 	
	 */
	private static final Set<Byte> conf_opcodes =
			Collections.unmodifiableSet(new HashSet<Byte>(Arrays.asList(_conf_opcodes)));

	protected void _check_opcode(byte opcode)
	{
		if (!conf_opcodes.contains(opcode))
			throw new RuntimeException("Opcode " + opcode + " no es de tipo Conf.");
	}

	@Override
	public int getTotalFragments() {
		return 1;
	}

	@Override
	public void reassemble(Vector<Message> fragments) {
	}

	@Override
	public boolean fragmented() {
		return false;
	}
}