package P2P.PeerPeer.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageData extends Message {

	private int index;
	private byte[] data;

	public byte[] getData() {
		return data;
	}

	public int getIndex() {
		return index;
	}

	public MessageData() {
		setType(TYPE_DATA);
	}

	public MessageData(int index) {
		this.index = index;
		setType(TYPE_DATA);
	}

	public MessageData(DataInputStream dis, int chunkSize) {
		fromStream(dis, chunkSize);
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buf = ByteBuffer.allocate(FIELD_TYPE_BYTES + FIELD_INDEX_BYTES + data.length);

		buf.put((byte) getType());
		buf.putInt(index);
		buf.put(data);

		return buf.array();
	}

	protected boolean fromByteArray(byte[] dataArray) {

		ByteBuffer buf = ByteBuffer.wrap(dataArray);
		try {
			data = new byte[dataArray.length];
			buf.get(data);
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean fromStream(DataInputStream dis, int chunkSize) {
		try {
			if (dis.read() != (byte) TYPE_DATA) {
				// Error: invalid FileData message
				return false;
			}
		} catch (IOException e1) {
			return false;
		}

		byte[] index_bytes = new byte[FIELD_INDEX_BYTES];
		try {
			dis.readFully(index_bytes);
		} catch (IOException e1) {
			return false;
		}
		index = ByteBuffer.wrap(index_bytes).getInt();
		data = new byte[chunkSize];

		try {
			dis.readFully(data);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

}