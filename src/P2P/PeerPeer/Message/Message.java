package P2P.PeerPeer.Message;

import java.io.DataInputStream;

public abstract class Message {
	
	/**
	 * Size of "type" field: byte (1 bytes)
	 */
	protected static final int FIELD_TYPE_BYTES = 1;
	
	/**
	 * Size of "hash" field: byte (20 bytes)
	 */
	protected static final int FIELD_HASH_BYTES = 20;
	
	/**
	 * Size of "n_chunks" field: byte (5 bytes)
	 */
	protected static final int FIELD_N_CHUNKS_BYTES = 5;
	
	/**
	 * Size of "index" field: byte (4 bytes)
	 */
	protected static final int FIELD_INDEX_BYTES = 4;
	
	public static final int REQ_LIST = FIELD_TYPE_BYTES + FIELD_HASH_BYTES;
	
	public static final int TYPE_REQ_LIST = 1;
	public static final int TYPE_LIST = 2;
	public static final int TYPE_REQ_DATA = 3;
	public static final int TYPE_DATA = 4;
	
	private int type;
	
	public abstract byte[] toByteArray();
	//protected abstract boolean fromByteArray(byte[] array);
	protected abstract boolean fromByteArray(DataInputStream dis);
	
	public int getType() {
		return type;
	}
	
	protected void setType(int type) {
		this.type = type;
	}
	
	public static MessageHash makeReqList(String hash) {
		return new MessageHash(TYPE_REQ_LIST,hash);
	}
	
	public static MessageHash makeReqList(byte[] array) {
		MessageHash m = new MessageHash(TYPE_REQ_LIST,null);
		m.fromByteArray(array);
		return m;
	}
	
	public static MessageFileChunk makeChunkList(int nChunk,int ...index){
		MessageFileChunk m = new MessageFileChunk(TYPE_LIST,nChunk,index);
		return m;
	}
	
	public static MessageFileChunk makeChunkList(byte[] buf){
		MessageFileChunk m = new MessageFileChunk(buf);
		return m;
	}
	
	
	public static MessageChunk makeReqData(){
		return null;
	}
	
	public static MessageData makeChunkData(){
		return null;
	}
	
}
