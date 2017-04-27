package P2P.PeerPeer.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.SynchronousQueue;

import javax.swing.plaf.basic.BasicTreeUI.TreeHomeAction;

import P2P.PeerPeer.Client.Downloader;
import P2P.PeerPeer.Message.Message;
import P2P.PeerPeer.Message.MessageChunk;
import P2P.PeerPeer.Message.MessageChunkList;
import P2P.PeerPeer.Message.MessageHash;
import P2P.util.PeerDatabase;

public class SeederThread extends Thread {
    private Socket socket;
	private Downloader downloader;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	private short chunkSize;
	private String fileHash;
	/* Global buffer for performance reasons */
    private byte[] chunkDataBuf;

    public SeederThread(Socket socket, Downloader downloader, short chunkSize) {
    	this.socket = socket;
    	this.downloader = downloader;
    	this.chunkSize = chunkSize;
    	
    	try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Error creating Seeder Thread");
		}
		
    }

	//Devuelve la lista de trozos que tiene del fichero solicitado
    public void sendChunkList(String fileHashStr) {
    	
    }
    
    //Envía por el socket el chunk solicitado por el DownloaderThread
    protected void sendChunk(int chunkNumber, String fileHashStr)  {
    }

    //Método principal que coordina la recepción y envío de mensajes
    public void run() {
    	
    	byte[] buf_list = new byte[Message.SIZE_REQ_LIST];
    	try {
			dis.read(buf_list);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	
    	MessageHash fileRequested = Message.makeReqList(buf_list);
    	fileHash = fileRequested.getHash();
 	    MessageChunkList chunkList=Message.makeChunkList(5,1,2,3,4,5);
    	
        try {
    		dos.write(chunkList.toByteArray());
    		dos.flush();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
        
        byte[] buf_chunk = new byte[Message.SIZE_REQ_DATA];
    	try {
			dis.read(buf_chunk);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	MessageChunk requestedChunk = Message.makeReqData(buf_chunk);
    	int index = requestedChunk.getIndex();

    }
 
}
    