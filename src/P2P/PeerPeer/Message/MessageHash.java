package P2P.PeerPeer.Message;

import java.io.DataInputStream;
import java.nio.ByteBuffer;

import P2P.util.FileDigest;

public class MessageHash extends Message{
	
	private String hash;
	
	public MessageHash(byte[] array) {
		fromByteArray(array);
	}
	
	public MessageHash(String hash) {
		setType(TYPE_REQ_LIST);
		setHash(hash);
	}
	
	public String getHash(){
		return hash;
	}
	
	private void setHash(String hash) {
		this.hash = hash;
	}
	
	@Override
	public byte[] toByteArray() {
		int byteBufferLength = SIZE_REQ_LIST;

		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		buf.put((byte)this.getType());
		buf.put(FileDigest.getDigestFromHexString(getHash()));

		return buf.array();
	}

	protected boolean fromByteArray(byte[] array) {

		if (array.length != SIZE_REQ_LIST || ((int)array[0] != TYPE_REQ_LIST)) {
			throw new RuntimeException("Invalid HASH message");
		}
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			setType((int)buf.get());
			
			byte[] hasharray = new byte[FIELD_HASH_BYTES];
			buf.get(hasharray, 0, hasharray.length);
			setHash(new String(FileDigest.getChecksumHexString(hasharray)));
			
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}
}
