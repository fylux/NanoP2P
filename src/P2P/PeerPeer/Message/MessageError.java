package P2P.PeerPeer.Message;

import java.nio.ByteBuffer;

public class MessageError extends Message{
	
	
	public MessageError(byte[] array) {
		fromByteArray(array);
	}
	
	public MessageError() {
		setType(TYPE_ERROR);
	}
	

	@Override
	public byte[] toByteArray() {
		int byteBufferLength = SIZE_ERROR;

		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		buf.put((byte)this.getType());

		return buf.array();
	}

	protected boolean fromByteArray(byte[] array) {

		if (array.length != SIZE_ERROR || ((int)array[0] != TYPE_ERROR)) {
			//Invalid ERROR message
			return false;
		}
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			setType((int)buf.get());
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}
}
