package P2P.PeerPeer.Message;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MessageData extends Message {

	private List<Integer> index;
	private byte[] data;
	
	public List<Integer> getIndex() {
		return index;
	}

	public byte[] getData() {
		return data;
	}

	public MessageData(int type,byte[] data,int...index) {
		this.index=new LinkedList<Integer>();
		setType(type);
		this.data=Arrays.copyOf(data, data.length);
		for (Integer i : index) {
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
