package P2P.PeerPeer.Message;

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
		
		int byteBufferLength = FIELD_TYPE_BYTES + FIELD_N_CHUNKS_BYTES + index.size()*FIELD_INDEX_BYTES;

		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		buf.put((byte)this.getType());
		byte[] nchunk = new byte[FIELD_N_CHUNKS_BYTES];
		
		//¿como pasas ese número a byte?
		//¿y si ocupa más de un byte?
		nchunk[0]=(byte)this.nChunk;
		buf.put(nchunk);
		for (Integer i : index) {
			buf.put((byte)i.intValue());
		}

		return buf.array();
	}

	@Override
	protected boolean fromByteArray(byte[] array) {
		if (array.length < FIELD_TYPE_BYTES + FIELD_N_CHUNKS_BYTES + FIELD_INDEX_BYTES) {
			System.err.println("Contenido del byte array "+array+" no es mensaje con formato FileChunk");
			throw new RuntimeException("Byte array no contiene un mensaje con formato FileChunk");
		}
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			setType((int)buf.get());
	
			byte[] nChunkArray = new byte[FIELD_N_CHUNKS_BYTES-1];
			buf.get(nChunkArray, 0, nChunkArray.length);
			System.out.println("dsfdsafsa");
			
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
