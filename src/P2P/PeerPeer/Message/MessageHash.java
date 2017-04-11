package P2P.PeerPeer.Message;

import java.nio.ByteBuffer;

import P2P.util.FileDigest;

public class MessageHash extends Message{
	
	private String hash;
	
	public MessageHash(int type, String hash) {
		setType(type);
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
		int byteBufferLength = FIELD_TYPE_BYTES + FIELD_HASH_BYTES;

		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		buf.put((byte)this.getType());
		buf.put(FileDigest.getDigestFromHexString(getHash()));

		return buf.array();
	}

	@Override
	protected boolean fromByteArray(byte[] array) {
		if (array.length < FIELD_TYPE_BYTES + FIELD_HASH_BYTES) {
			System.err.println("Contenido del byte array "+array+" no es mensaje con formato Hash");
			throw new RuntimeException("Byte array no contiene un mensaje con formato Hash");
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
