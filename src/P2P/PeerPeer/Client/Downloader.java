package P2P.PeerPeer.Client;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import P2P.util.FileInfo;


public class Downloader implements DownloaderIface {

	private InetSocketAddress[] seedList;
	private FileInfo file;
	private short chunkSize;
	private int[] chunkState;
	
	private DownloaderThread[] threads;
	private int nChunksDownloaded = 0;
	
	private static final int DOWNLOADED = 1;
	private static final int DOWNLOADING = 0;
	private static final int NO_DOWNLOADED = -1;
	
	public Downloader(FileInfo file, short chunkSize) {
		this.file = file;
		this.chunkSize = chunkSize;
		this.chunkState = new int[getTotalChunks()];
		for (int i = 0; i < chunkState.length ; i++)
			chunkState[i] = NO_DOWNLOADED;
	}
	
	@Override
	public FileInfo getTargetFile() {
		return file;
	}

	@Override
	public InetSocketAddress[] getSeeds() {
		return seedList;
	}

	@Override
	public int getTotalChunks() {
		return (int)Math.ceil(file.fileSize/(long)chunkSize);
	}
	
	public int getSizeLastChunk() {
		int size = (int) (file.fileSize % (long)chunkSize);
		if (size == 0)
			return chunkSize;
		else
			return size;
	}

	@Override
	public boolean downloadFile(InetSocketAddress[] seedList) {
		/*threads = new DownloaderThread[seedList.length];
		for (int i = 0; i < seedList.length; i++) {
			threads[i] = new DownloaderThread(this,seedList[i]);
			threads[i].start();
		}*/
		DownloaderThread d = new DownloaderThread(this,seedList[0]);
		d.start();
		joinDownloaderThreads();
		return false;
	}

	public int bookNextChunk(List<Integer> list) {
		for (int i : list)
			if (chunkState[i] == NO_DOWNLOADED) {
				chunkState[i] = DOWNLOADING;
				return i;
			}
		
		for (int i : chunkState)
			if (i == NO_DOWNLOADED)
				return -1;	//The peer doesn't have a chunk that is needed
		
		return -2; //All the chunks are being downloaded
	}
	
	public void setChunkDownloaded(int chunk, boolean achieved){
		if (achieved) {
			//if (nChunksDownloaded == 0) {} //Add_seed
			nChunksDownloaded++;
			chunkState[chunk] = DOWNLOADED;
		}
		else
			chunkState[chunk] = NO_DOWNLOADED;
	}
	
	@Override
	public int[] getChunksDownloadedFromSeeders() {
		LinkedList<Integer> chunks = new LinkedList<Integer>();
		for (int i : chunkState)
			if (i == DOWNLOADED)
				chunks.add(i);
		
		int[] array = new int[chunks.size()];
		for (int i = 0; i < chunks.size(); i++)
		    array[i] = chunks.get(i); 
		
		return array;
	}

	@Override
	public boolean isDownloadComplete() {
		for (int i : chunkState)
			if (i != DOWNLOADED)
				return false;
		
		return true;
	}

	@Override
	public void joinDownloaderThreads() {
		/*try {
			for (DownloaderThread t : threads)
					t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}

	@Override
	public short getChunkSize() {
		return chunkSize;
	}

}
