package P2P.PeerPeer.Message;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import P2P.App.PeerController;
import P2P.util.FileDigest;

public class MessageData extends Message {

	private int index;
	private byte[] data;
	

	public byte[] getData() {
		return data;
	}
	
	public int getIndex() {
		return index;
	}

	public MessageData(byte[] dataArray,int index) {
		this.index=index;
		setType(TYPE_DATA);
		fromByteArray(dataArray);
	}
	
	public MessageData(DataInputStream dis){
		fromStream(dis);
	}
	
	
	@Override
	public byte[] toByteArray() {
		ByteBuffer buf = ByteBuffer.allocate(FIELD_TYPE_BYTES+FIELD_INDEX_BYTES+data.length);
		
		buf.put((byte)getType());
		buf.putInt(index);
		buf.put(data);
		
		return buf.array();
	}

	protected boolean fromByteArray(byte[] dataArray) {
		
		ByteBuffer buf = ByteBuffer.wrap(dataArray);
		try {
			data = new byte[dataArray.length];
			buf.get(data);
			return true;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	protected boolean fromStream(DataInputStream dis) {
		try {
			if (dis.read() != (byte)TYPE_DATA) {
				System.err.println("Error: invalid FileData message");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		byte[] index_bytes = new byte[FIELD_INDEX_BYTES];
		try {
			dis.read(index_bytes);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		index = ByteBuffer.wrap(index_bytes).getInt();
		
		//TODO como calcular el tamaño de data
		int data_size=23;
		int bytesLeidos=0;
		data = new byte[data_size];
			try {
				bytesLeidos=dis.read(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		System.out.println("bytes leidos: " + bytesLeidos);
		//writeFile(data,bytesLeidos); //funcion de prueba
		return true;
	}

//función de prueba..
	/*
	private void writeFile(byte[] data,int bytesLeidos){
		File f2 = new File("C:/Users/daniel/Desktop/share/P1/funciona.txt");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f2);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		}
	}	*/
}