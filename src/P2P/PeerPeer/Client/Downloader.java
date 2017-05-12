package P2P.PeerPeer.Client;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import P2P.App.Peer;
import P2P.App.PeerCommands;
import P2P.App.PeerController;
import P2P.util.FileDigest;
import P2P.util.FileInfo;


public class Downloader implements DownloaderIface {

	private InetSocketAddress[] seedList;
	private FileInfo file;
	private short chunkSize;
	private int[] chunkState;
	
	private long init_time;
	private DownloaderThread[] threads;
	private int nChunksDownloaded = 0;
	private int totalChunks;
	
	private PeerController peerController;
	
	private static final int DOWNLOADED = 1;
	private static final int DOWNLOADING = 0;
	private static final int NO_DOWNLOADED = -1;
	
	
	public Downloader(PeerController peerController, FileInfo file, short chunkSize) {
		this.totalChunks=(int)Math.ceil(Float.valueOf(file.fileSize)/Float.valueOf(chunkSize));
		if(totalChunks==0)
			this.totalChunks = 1;
		
		this.peerController = peerController;
		this.file = file;
		this.chunkSize = chunkSize;
		this.chunkState = new int[getTotalChunks()];
		this.init_time = System.nanoTime();
		
		
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
		return totalChunks;
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
	
		//TODO si la lista esta vacia puede ser que lo tenga todo o no tenga nada
		if (list.isEmpty()){
			for (int i=0;i<chunkState.length;i++)
				if (chunkState[i] == NO_DOWNLOADED) {
					chunkState[i] = DOWNLOADING;
					return i+1;
				}
		}
		else {	
			for (int i : list)
				if (chunkState[i] == NO_DOWNLOADED) {
					chunkState[i] = DOWNLOADING;
					return i+1;
				}
		}
		for (int i : chunkState)
			if (i == NO_DOWNLOADED)
				return -1;	//The peer doesn't have a chunk that is needed
		
		return -2; //All the chunks are being downloaded
	}
	
	public void setChunkDownloaded(int chunk, boolean achieved){
		if (achieved) {
			if (nChunksDownloaded == 0) { //Add_seed
				peerController.setCurrentCommand(PeerCommands.COM_ADDSEED);
				String args[] = new String[3];
				args[0] = file.fileHash;
				args[1] = file.fileName;
				args[2] = Long.toString(file.fileSize);
				peerController.setCurrentCommandArguments(args);
				peerController.processCurrentCommand();
			} 
			nChunksDownloaded++;
			chunkState[chunk-1] = DOWNLOADED;
			System.out.println("Downloading: "+nChunksDownloaded*100/getTotalChunks()+"%");
		
			if (isDownloadComplete())
				end();
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
		return nChunksDownloaded == getTotalChunks();
		/*for (int i : chunkState)
			if (i != DOWNLOADED)
				return false;*/
		
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
	
	public void end() {
		Peer.db.addDownloadedFile(file);;
	
		String computedHash = FileDigest.getChecksumHexString(
				FileDigest.computeFileChecksum(
						Peer.db.getSharedFolderPath()+"/"+file.fileName
						)
				);

		if (!computedHash.equals(file.fileHash))
			System.err.println("File: "+file.fileName+" is corrupted");
		
		
		long elapsed_time =  System.nanoTime() - init_time;
		float seconds = (float)elapsed_time/(float)1000000000;
		System.out.println("Time: "+seconds+"s");
		float speed = (float)file.fileSize/(float)1024.0/seconds;
		System.out.printf("Speed: %.2fkb/s", speed);
	}
}
