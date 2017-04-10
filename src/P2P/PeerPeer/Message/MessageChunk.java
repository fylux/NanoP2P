package P2P.PeerPeer.Message;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class MessageChunk extends Message {

	private List<Integer> index;
	public MessageChunk(int...index) {
			
		this.index=new LinkedList<Integer>();
		for (int i : index) {
			this.index.add(i);
		}
	}
	
	public List<Integer> getIndex() {
		return index;
	}
	
	
	
	@Override
	public byte[] toByteArray() {
		int byteBufferLength = FIELD_TYPE_BYTES + index.size()*FIELD_INDEX_BYTES;

		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		buf.put((byte)this.getType());
		for (Integer i : index) {
			buf.put((byte)i.intValue());
		}

		return buf.array();
	}

	@Override
	protected boolean fromByteArray(byte[] array) {
		// TODO Auto-generated method stub
		return false;
	}

}
