package P2P.PeerPeer.Message;

import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import P2P.util.FileDigest;

public class MessageFileChunk extends Message {

	private int nChunk;
	private List<Integer> index;
	
	public MessageFileChunk(byte[] buf){
		index=new LinkedList<Integer>();
		fromByteArray(buf);
	}
	
	public MessageFileChunk(int type,int nChunk,int ... index) {
		this.index=new LinkedList<Integer>();
		this.nChunk=nChunk;
		setType(type);
		for (int i : index) {
			this.index.add(i);
		}
	}
	
	public int getnChunk() {
		return nChunk;
	}

	public List<Integer> getIndex() {
		return index;
	}

	public void setNChunk(int nChunk) {
		this.nChunk = nChunk;
	}
	
	
	@Override
	public byte[] toByteArray() {
		//TODO nChunk = -1
		int byteBufferLength = FIELD_TYPE_BYTES + FIELD_N_CHUNKS_BYTES + nChunk*FIELD_INDEX_BYTES;

		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		buf.put((byte)this.getType());
		byte[] field_n_chunk = ByteBuffer.allocate(FIELD_N_CHUNKS_BYTES).putInt(nChunk).array();
		
		buf.put(field_n_chunk);
		
		for (Integer i : index) {
			byte[] chunk_index = ByteBuffer.allocate(FIELD_INDEX_BYTES).putInt(i).array();
			buf.put(chunk_index);
		}

		return buf.array();
	}


	protected boolean fromByteArray(DataInputStream dis) {
		if (dis.read() != (byte)TYPE_LIST) {
			System.err.println("Error: invalid FileList message");
		}
		byte[] nChunk_bytes = new byte[FIELD_N_CHUNKS_BYTES];
		dis.read(nChunk_bytes);
		long aux = ByteBuffer.wrap(nChunk_bytes).getLong();
		if (aux == -1) {
			
		}
		else {
			nChunk = (int) aux;
		}
		/*if (array.length < FIELD_TYPE_BYTES + FIELD_N_CHUNKS_BYTES + FIELD_INDEX_BYTES) {
			System.err.println("Contenido del byte array "+array+" no es mensaje con formato FileChunk");
			throw new RuntimeException("Byte array no contiene un mensaje con formato FileChunk");
		}*/
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			setType((int)buf.get());
	
			byte[] nChunkArray = new byte[FIELD_N_CHUNKS_BYTES];
			buf.get(nChunkArray, 0, nChunkArray.length);
			
			byte[] nIndexArray;
			for(int i=0;i<nChunk;i++){
			nIndexArray =new byte[FIELD_INDEX_BYTES];
			buf.get(nIndexArray, 0, nIndexArray.length);
			//index.add(Integer.decode(FileDigest.getChecksumHexString(nIndexArray)));
			}	
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}
}
