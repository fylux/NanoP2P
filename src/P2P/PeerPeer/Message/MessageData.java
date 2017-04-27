package P2P.PeerPeer.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import P2P.App.PeerController;
import P2P.util.FileDigest;

public class MessageData extends Message {

	private byte[] data;

	public byte[] getData() {
		return data;
	}

	public MessageData(byte[] array) {
		fromByteArray(array);
	}
	
	@Override
	public byte[] toByteArray() {
		ByteBuffer buf = ByteBuffer.allocate(FIELD_TYPE_BYTES+data.length);
		
		buf.put((byte)getType());
		buf.put(data);
		
		return buf.array();
	}

	protected boolean fromByteArray(byte[] array) {
		if ( (int)array[0] != TYPE_DATA) {
			throw new RuntimeException("Invalid DATA message");
		}
		
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			setType((int)buf.get());
			data = new byte[array.length-1];
			buf.get(data);
			
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}
}
