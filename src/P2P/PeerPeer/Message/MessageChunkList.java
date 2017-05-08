package P2P.PeerPeer.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import P2P.util.FileDigest;

public class MessageChunkList extends Message {

	private int nChunk;
	private boolean all;
	private LinkedList<Integer> index;
	
	
	public MessageChunkList(DataInputStream dis) {
		this.index=new LinkedList<Integer>();
		fromStream(dis);
	}
	
	public MessageChunkList(int nChunk,int ... index) {
		this.index=new LinkedList<Integer>();
		this.nChunk=nChunk;
		all = nChunk == -1;
		setType(TYPE_LIST);
		
		for (int i : index) {
			this.index.add(i);
		}
		
		
	}
	
	public boolean isAll(){
		return all;
	}
	
	public int getNChunk() {
		return nChunk;
	}

	public LinkedList<Integer> getIndex() {
		return index;
	}

	public void setNChunk(int nChunk) {
		this.nChunk = nChunk;
	}
	
	
	@Override
	public byte[] toByteArray() {
		int byteBufferLength;
		if (all)
			byteBufferLength = FIELD_TYPE_BYTES + FIELD_N_CHUNKS_BYTES;
		else
			byteBufferLength = FIELD_TYPE_BYTES + FIELD_N_CHUNKS_BYTES + nChunk*FIELD_INDEX_BYTES;

		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		buf.put((byte)this.getType());
		buf.putInt(nChunk);
		
		//si all está activado index será tendrá length cero
		for (Integer i : index){
			buf.putInt(i);
		}

		return buf.array();
	}


	protected boolean fromStream(DataInputStream dis) {
		try {
			if (dis.read() != (byte)TYPE_LIST) {
				System.err.println("Error: invalid FileList message");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		byte[] nChunk_bytes = new byte[FIELD_N_CHUNKS_BYTES];
		try {
			dis.read(nChunk_bytes);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		int nChunk = ByteBuffer.wrap(nChunk_bytes).getInt();
		all = nChunk == -1;

		if (all)
			return true;
		
		for (int i = 0; i < nChunk; i++) {
			try {
				index.add(dis.readInt());
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		return true;
	}
}