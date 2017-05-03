package P2P.PeerPeer.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import javax.xml.bind.DatatypeConverter;

import P2P.PeerPeer.Message.Message;
import P2P.PeerPeer.Message.MessageChunk;
import P2P.PeerPeer.Message.MessageChunkList;
import P2P.PeerPeer.Message.MessageData;
import P2P.PeerPeer.Message.MessageHash;
import P2P.util.FileDigest;

/**
 * @author rtitos
 * 
 * Threads of this class handle the download of file chunks from a given seed
 * through a TCP socket established to the seed socket address provided 
 * to the constructor.
 */
public class DownloaderThread  extends Thread {
	private Downloader downloader; 
	private Socket downloadSocket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	private int numChunksDownloaded;
	
	private LinkedList<Integer> chunkList;

	public DownloaderThread(Downloader downloader, InetSocketAddress seed) {
		this.downloader = downloader;
		this.chunkList = new LinkedList<Integer>();
		try {
			downloadSocket = new Socket(seed.getAddress(),seed.getPort());
			dos = new DataOutputStream(downloadSocket.getOutputStream());
			dis = new DataInputStream(downloadSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Error creating Download Thread");
		}
	}

	//It receives a message containing a chunk and it is stored in the file
	private void receiveAndWriteChunk() {
    }

	private void requestChunkList() {
		MessageHash reqList = Message.makeReqList(downloader.getTargetFile().fileHash);
    	try {
			dos.write(reqList.toByteArray());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void receiveAndProcessChunkList() {
    	chunkList = Message.makeChunkList(dis).getIndex();
    }
		
	//Number of chunks already downloaded by this thread
    public int getNumChunksDownloaded() {
    	return numChunksDownloaded;
    }

    //Main code to request chunk lists and chunks
    public void run() {
    	int bookedChunk = -1;
    	
    	requestChunkList();
    	receiveAndProcessChunkList();
    	bookedChunk = downloader.bookNextChunk(chunkList);
    		
    	while (!downloader.isDownloadComplete()) {
    		if (bookedChunk == -1) {
    			//Ask peer repeteadly and then ask downloader
    		}
    		else if (bookedChunk == -2) {
    			//Ask downloader repeteadly if is complete, if so break
    		}
    		
    		//Req data bookedChunk
    		//Write bookedChunk
    		//bookedChunk()
    	}
    	
    	MessageChunk reqChunk;
    	MessageData dataChunk;
    	//calcular los chunk totales del fichero
    	
    	//while haya chunk sin descargar
    	reqChunk= Message.makeReqData(processIndex());
    	
    	try {
			dos.write(reqChunk.toByteArray());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	//dataChunk=new MessageData(dis);
    	
    	
    	//actualizar chunk pendientes de descarga
    	//end while
    	
    }
    
    
    
    public int processIndex(){
    //TODO calcular tamaño archivo, elegir index aleatorio
    	
    	
    	return 1;
    }
    
}


/**
-hay que descargar la lista de trozos enteras no solo la 1
-faltaría recibir los datos el trozo pedido

**/