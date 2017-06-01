package P2P.PeerPeer.Message;

import java.nio.ByteBuffer;

public class MessageChunk extends Message {

	private int index;

	public MessageChunk(byte[] array) {
		fromByteArray(array);
	}

	public MessageChunk(int index) {
		setType(TYPE_REQ_DATA);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer buf = ByteBuffer.allocate(SIZE_REQ_DATA);

		buf.put((byte) this.getType());
		buf.putInt(getIndex());

		return buf.array();
	}

	protected boolean fromByteArray(byte[] array) {
		if (array.length != SIZE_REQ_DATA || ((int) array[0] != TYPE_REQ_DATA)) {
			// Invalid CHUNK message
			return false;
		}
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			setType((int) buf.get());
			index = buf.getInt();
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
