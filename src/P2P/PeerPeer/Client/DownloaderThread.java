package P2P.PeerPeer.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

import javax.xml.bind.DatatypeConverter;

import P2P.App.Peer;
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
	int pos=0;
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
		//TODO
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
    	MessageChunk reqChunk;
    	requestChunkList();
    	receiveAndProcessChunkList();
    	bookedChunk = downloader.bookNextChunk(chunkList);
    	
    	while (!downloader.isDownloadComplete()){
    		System.out.println(Thread.currentThread().getId()+" "+bookedChunk);
	    	//Ask peer repeteadly and then ask downloader
	    	if (bookedChunk == -1) {
	    	    requestChunkList();
	    	    receiveAndProcessChunkList();
	    	    bookedChunk = downloader.bookNextChunk(chunkList);
	    	    continue;
	    	}
	    	else if (bookedChunk == -2) {
	    	   bookedChunk = downloader.bookNextChunk(chunkList);
	    	   continue;
	    	}

        	reqChunk= Message.makeReqData(bookedChunk);
        
        	try {
    			dos.write(reqChunk.toByteArray());
    			dos.flush();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        	
        	
        	//TODO ¿reqChunk.getIndex() es igual a bookedChunk?
        	int chunkSize=downloader.getChunkSize();
        	
        	if (reqChunk.getIndex()==downloader.getTotalChunks()) //if is the last chunk
        		chunkSize=downloader.getSizeLastChunk();
        	
    		writeData(Message.makeChunkData(dis,chunkSize));	
    		downloader.setChunkDownloaded(reqChunk.getIndex(),true);
        	
             		
        	receiveAndWriteChunk();
        	bookedChunk = downloader.bookNextChunk(chunkList);
    	}
    	
    	try {
			downloadSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }  

    private void writeData(MessageData message){
    
    	String path = Peer.db.getSharedFolderPath() + "/" + downloader.getTargetFile().fileName;
    		
    	byte[] data = message.getData();
    	int index=message.getIndex();
    	
    	    	
    	int parts=downloader.getTotalChunks();
		int lastSize=downloader.getSizeLastChunk();
		
		int dataSize=downloader.getChunkSize();
		//si tengo la ultima parte, descargo solo lo que pese
		if (index==parts)
			dataSize=lastSize;
		
		 	
       	File f2 = new File(path);
		try {
			if (!f2.exists())
				f2.createNewFile();
			RandomAccessFile rfo =  new RandomAccessFile(f2,"rw");
			rfo.seek(pos);
			rfo.write(data);	
			rfo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}		
		
		//printByteArray(data);
		pos+=dataSize;
    }
    
   /* private void printByteArray(byte[] data){
    	String file_string = "";

		for(int i = 0; i < data.length; i++)
		   {
				file_string += (char)data[i];
		   }

		System.out.println("cadena: " + file_string);

    }*/
    
}
