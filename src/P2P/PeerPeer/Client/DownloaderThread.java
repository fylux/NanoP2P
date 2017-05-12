package P2P.PeerPeer.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import P2P.App.Peer;
import P2P.PeerPeer.Message.Message;
import P2P.PeerPeer.Message.MessageChunk;
import P2P.PeerPeer.Message.MessageChunkList;
import P2P.PeerPeer.Message.MessageData;
import P2P.PeerPeer.Message.MessageHash;

/**
 * @author rtitos
 * 
 *         Threads of this class handle the download of file chunks from a given
 *         seed through a TCP socket established to the seed socket address
 *         provided to the constructor.
 */
public class DownloaderThread extends Thread {
	private Downloader downloader;
	private Socket downloadSocket;
	protected DataOutputStream dos;
	protected DataInputStream dis;
	private int numChunksDownloaded;
	private InetSocketAddress seed;

	private MessageChunkList chunkList;
	private int bookedChunk;

	public DownloaderThread(Downloader downloader, InetSocketAddress seed) {
		this.seed = seed;
		this.numChunksDownloaded = 0;
		this.downloader = downloader;
		try {
			downloadSocket = new Socket(seed.getAddress(), seed.getPort());
			dos = new DataOutputStream(downloadSocket.getOutputStream());
			dis = new DataInputStream(downloadSocket.getInputStream());
		} catch (IOException e) {
			System.out.println("Error creating Download Thread");
		}
	}

	// It receives a message containing a chunk and it is stored in the file
	private boolean receiveAndWriteChunk() {
		int chunkSize = downloader.getChunkSize();

		if (bookedChunk == downloader.getTotalChunks())
			chunkSize = downloader.getSizeLastChunk();

		MessageData response = Message.makeChunkData(dis, chunkSize);
		if (response == null) {
			downloader.setChunkDownloaded(bookedChunk, false);
			return false;
		}

		writeData(response);
		downloader.setChunkDownloaded(bookedChunk, true);
		numChunksDownloaded++;

		return true;
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

	private boolean receiveAndProcessChunkList() {
		chunkList = Message.makeChunkList(dis);
		return chunkList != null;
	}

	// Number of chunks already downloaded by this thread
	public int getNumChunksDownloaded() {
		return numChunksDownloaded;
	}

	// Main code to request chunk lists and chunks
	public void run() {
		boolean error = false;
		bookedChunk = -1;

		MessageChunk reqChunk;
		requestChunkList();
		if (receiveAndProcessChunkList())
			bookedChunk = downloader.bookNextChunk(chunkList);

		while (!downloader.isDownloadComplete() && !error) {
	
			// Ask peer repeteadly and then ask downloader
			if (bookedChunk == -1) {
				requestChunkList();
				if (!receiveAndProcessChunkList())
					error = true;
				else
					bookedChunk = downloader.bookNextChunk(chunkList);
				
				continue;
			} else if (bookedChunk == -2) {
				bookedChunk = downloader.bookNextChunk(chunkList);
				continue;
			}

			reqChunk = Message.makeReqData(bookedChunk);

			try {
				dos.write(reqChunk.toByteArray());
				dos.flush();
			} catch (IOException e) {
				downloader.setChunkDownloaded(reqChunk.getIndex(), false);
				break;
			}

			if (!receiveAndWriteChunk())
				error = true;
			else
				bookedChunk = downloader.bookNextChunk(chunkList);
		}

		try {
			sendStatistics();
			downloadSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendStatistics() {
		downloader.updateStats(seed, numChunksDownloaded);
	}

	private void writeData(MessageData message) {

		String path = Peer.db.getSharedFolderPath() + "/" + downloader.getTargetFile().fileName;

		byte[] data = message.getData();
		int index = message.getIndex();
		int pos = (index - 1) * downloader.getChunkSize();

		File f = new File(path);
		try {
			if (!f.exists())
				f.createNewFile();
			RandomAccessFile rfo = new RandomAccessFile(f, "rw");
			rfo.seek(pos);
			rfo.write(data);
			rfo.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
