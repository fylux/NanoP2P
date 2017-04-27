package P2P.PeerPeer.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.xml.bind.DatatypeConverter;

import P2P.PeerPeer.Message.Message;
import P2P.PeerPeer.Message.MessageChunk;
import P2P.PeerPeer.Message.MessageChunkList;
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

	public DownloaderThread(Downloader downloader, InetSocketAddress seed) {
		this.downloader = downloader;
		//new IllegalAccessError();
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

	//It receives a message containing a chunk and it is stored in the file
	private void receiveAndProcessChunkList() {
    }
		
	//Number of chunks already downloaded by this thread
    public int getNumChunksDownloaded() {
    	return numChunksDownloaded;
    }

    //Main code to request chunk lists and chunks
    public void run() {
        	
    	MessageHash reqList = Message.makeReqList(downloader.getTargetFile().fileHash);
    	try {
			dos.write(reqList.toByteArray());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	MessageChunkList chunkList = Message.makeChunkList(dis);
    	
    	MessageChunk reqChunk = Message.makeReqData(1);
    	
    	try {
			dos.write(reqChunk.toByteArray());
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}