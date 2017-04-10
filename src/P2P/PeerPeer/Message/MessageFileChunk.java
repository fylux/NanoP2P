package P2P.PeerPeer.Message;

import java.util.LinkedList;
import java.util.List;

public class MessageFileChunk extends Message {

	private int nChunk;
	private List<Integer> index;
	
	public MessageFileChunk(int type,int nChunk,int ... index) {
		this.index=new LinkedList<Integer>();
		this.nChunk=nChunk;
		setType(type);
		for (int i : index) {
			this.index.add(i);
		}
	}
	
	@Override
	public byte[] toByteArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean fromByteArray(byte[] array) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	

}
