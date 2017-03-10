package P2P.PeerTracker.Message;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * @author rtitos
 * 
 * Peer-tracker protocol message, format "Query" 
 * 
    1b      1b            1b           1b            filter.len
 +--------------------------------------------------------------+
 |opcode| trans_id |  filter.type |  filter.len  |   filter     |
 +------+----------+--------------+--------------+--------------+

 */
public class MessageQuery extends Message {

	public static final byte FILTERTYPE_INVALID = 0;
	/**
	 * Query filter type "all": query all files in tracker
	 */
	public static final byte FILTERTYPE_ALL = 1;
	/**
	 * Query filter type "name": query files whose name matches 
	 * pattern given as filter. 
	 */
	public static final byte FILTERTYPE_NAME = 2;
	/**
	 * Query filter type "maxsize": query files whose size (in bytes)
	 * does not exceed the value (long, 8-bytes) given in filter.
	 */
	public static final byte FILTERTYPE_MAXSIZE = 3;
	/**
	 * Query filter type "minsize": query files whose size (in bytes)
	 * is equal or above the value given in filter.
	 */
	public static final byte FILTERTYPE_MINSIZE = 4;


	/**
	 * Size of "filter.type" field: 1 byte
	 */
	private static final int FIELD_FILTERTYPE_BYTES = 1;  
	/**
	 * Size of "filter.len" field: 1 byte
	 */
	private static final int FIELD_FILTERLENGTH_BYTES = 1;

	private static final int FIELD_NUMERICAL_FILTER_BYTES = Long.SIZE / 8;

	/**
	 * The type of filter in this message, from the types above
	 */
	private byte queryFilterType;

	/**
	 * The value of the filter in this message, 
	 * can be long or string depending on type.
	 */
	private String queryFilterStringValue;
	private long querFilterLongValue;

	public MessageQuery(byte opcode, byte tid, byte filterType,
			String filterValue) {
		setOpCode(opcode);
		setTransId(tid);
		this.queryFilterType = filterType;
		if (isNumericalQueryFilterType(filterType)) {
			this.querFilterLongValue = Long.parseLong(filterValue);
		}
		else {
			if (filterType == FILTERTYPE_ALL) {
				queryFilterStringValue = new String();
			}
			else {
				this.queryFilterStringValue = filterValue;
			}
		}
		valid = true;
	}

	public MessageQuery(byte[] buf) {
		if (fromByteArray(buf) == false) {
			throw new RuntimeException("Failed to parse message: format is not Query.");
		}
		else {
			assert(valid);
		}
	}

	private static boolean isNumericalQueryFilterType(byte filterType) {
		switch (filterType) {
			case FILTERTYPE_MAXSIZE:
			case FILTERTYPE_MINSIZE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Creates a byte array ready for sending a datagram packet, from a valid message of Query format 
	 */
	public byte[] toByteArray()
	{
		int byteBufferLength = FIELD_OPCODE_BYTES + FIELD_TRANSID_BYTES 
				+ FIELD_FILTERTYPE_BYTES + FIELD_FILTERLENGTH_BYTES 
				+ this.getFilterLen(); // The actual filter of this message
		
		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		// Opcode
		buf.put((byte)getOpCode());
		
		// Trans id
		buf.put((byte)getTransId());

		// Filter type
		buf.put((byte)getFilterType());
		
		// Filter len
		buf.put((byte)getFilterLen());
		
		assert(isValidQueryFilterValue(getFilterType(), getFilterValue()));
		if (isNumericalQueryFilterType(getFilterType())) {
			// Numerical filter: message contains long value in binary (8 bytes)
			buf.putLong(getFilterValueLong());
		}
		else { // Name based filter: message contains string (pattern)
			buf.put(getFilterValue().getBytes());
		}
		return buf.array();
	}

	/**
	 * Creates a valid message of FileInfo format, from the byte array of the received packet
	 */
	protected boolean fromByteArray(byte[] array) {
		if (array.length < FIELD_OPCODE_BYTES + FIELD_TRANSID_BYTES 
				+ FIELD_FILTERTYPE_BYTES + FIELD_FILTERLENGTH_BYTES) {
			System.err.println("Contenido del byte array "+array+" no es mensaje con formato Query");
			throw new RuntimeException("Byte array no contiene un mensaje con formato Query");
		}
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			// Opcode
			setOpCode(buf.get());
			// Trans id
			setTransId(buf.get());
			// Filter type
			setFilterType(buf.get());
			// Filter length
			byte filterLen = buf.get();
			if (isNumericalQueryFilterType(getFilterType())) {
				// Filter: long value
				assert(filterLen == FIELD_NUMERICAL_FILTER_BYTES);
				setFilterValueLong(buf.getLong());
			} 
			else {
				byte[] filterValueArray = new byte[filterLen];
				// Filter: string value
				buf.get(filterValueArray, 0, filterLen);
				setFilterStringValue(new String(filterValueArray));
			}
			assert(isValidQueryFilterValue(getFilterType(), getFilterValue()));
			valid = true;		
		} catch (RuntimeException e) {
			e.printStackTrace();
			assert(valid == false);
		}
		return valid;
	}	

	@Override
	public String toString() {
		assert(valid);
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" Type:"+this.getOpCodeString());
		strBuf.append(" TransId:"+this.getTransId());
		strBuf.append(" FilterType:"+this.getFilterType());
		strBuf.append(" FilterLen:"+this.getFilterLen());
		strBuf.append(" FilterVal:"+this.getFilterValue());
		return strBuf.toString();
	}

	private byte getFilterLen() {
		if (isNumericalQueryFilterType(this.queryFilterType)) {
			return FIELD_NUMERICAL_FILTER_BYTES;
		}
		else {
			assert(this.queryFilterStringValue.getBytes().length < Byte.MAX_VALUE);
			return (byte)this.queryFilterStringValue.getBytes().length;
		}
	}

	public String getFilterValue() {
		if (isNumericalQueryFilterType(queryFilterType)) {
			return Long.toString(querFilterLongValue);
		}
		else {
			return this.queryFilterStringValue;
		}
	}
	
	private long getFilterValueLong() {
		assert(isNumericalQueryFilterType(queryFilterType));
		return querFilterLongValue;
	}

	private void setFilterStringValue(String string) {
		assert(isNumericalQueryFilterType(this.getFilterType()) == false);
		queryFilterStringValue = string;
	}

	private void setFilterValueLong(long value) {
		assert(isNumericalQueryFilterType(this.getFilterType()));
		querFilterLongValue = value;
	}

	public byte getFilterType() {
		return this.queryFilterType;
	}

	private void setFilterType(byte type) {
		queryFilterType = type;
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

	public static boolean isValidQueryFilterValue(byte filterType, String filterValue) {
		boolean valid = false;
		if (isNumericalQueryFilterType(filterType)) {
			try {
				long value = Long.parseLong(filterValue);
				if (value < 0) valid = false;
				valid = true;
			}
			catch (NumberFormatException e){
				valid = false;
			}
		}
		else {
			/* Substring-based filters: Any string is valid, even an empty string,
			 * as long as it does not exceed the maximum size (limited 
			 * by the size of filter.len field)
			 */			
			if (filterValue == null)
				valid = false;
			else if (filterValue.getBytes().length < Byte.MAX_VALUE)
				valid = true;
			else 
				valid = false; // filter too long
		}
		return valid;
	}
}
