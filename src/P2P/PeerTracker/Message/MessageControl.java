/**
 *
 */
package P2P.PeerTracker.Message;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * @author rtitos
 * 
 * Peer-tracker protocol control message 
 * 
    1b       1b
 +-------+--------+
 |opcode | transId|
 +-------+--------+
             
 */

public class MessageControl extends Message 
{
	/**
	 * Control message opcodes 
	 */
	protected static final Byte[] _control_opcodes = {
			OP_QUERY_FILES,			
			OP_REMOVE_SEED_ACK,
			OP_GET_CONF,
			OP_ADD_SEED_ACK
			};

	/** 
	 * Constructor
	 * @param opcode opcode indicating the type of message
	 * @param tid 
	 */
	public MessageControl(byte opcode, byte tid) {
		setOpCode(opcode);
		setTransId(tid);
		valid = true;
	}

	/**
	 * Constructor used by tracker when creating message response after receiving
	 * @param buf
	 */
	public MessageControl(byte[] buf) {
		if (fromByteArray(buf) == false) {
			throw new RuntimeException("Failed to parse message: format is not Control.");
		}
		else {
			assert(valid);
		}
	}

	/**
	 * Constructor used by tracker when creating message response after receiving
	 * @param buf The byte array containing the packet buffer
	 */
	protected boolean fromByteArray(byte[] array) {
		if (array.length < FIELD_OPCODE_BYTES + FIELD_TRANSID_BYTES) {
			System.err.println("Contenido del byte array "+array+" no es mensaje con formato Control");
			throw new RuntimeException("Byte array no contiene un mensaje con formato Control");
		}
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			setOpCode(buf.get());
			setTransId(buf.get());
			valid = true;		
		} catch (RuntimeException e) {
			e.printStackTrace();
			assert(valid == false);
		}
		return valid;
	}

	/**
	 * Creates a byte array ready for sending a datagram packet, from a valid control message  
	 */
	public byte[] toByteArray() {
		ByteBuffer buf = ByteBuffer.allocate(FIELD_OPCODE_BYTES+FIELD_TRANSID_BYTES);

		// Opcode
		buf.put((byte)this.getOpCode());
		// Trans id
		buf.put((byte)this.getTransId());
		return buf.array();
	}

	public String toString() {
		assert(valid);
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" Type:"+this.getOpCodeString()+" TransId:"+this.getTransId());
		return strBuf.toString();
	}

	/* For checking opcode validity */
	private static final Set<Byte> control_opcodes =
			Collections.unmodifiableSet(new HashSet<Byte>(Arrays.asList(_control_opcodes)));

	protected void _check_opcode(byte opcode)
	{
		if (!control_opcodes.contains(opcode))
			throw new RuntimeException("Opcode " + opcode + " no es de tipo control.");
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
