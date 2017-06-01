package P2P.PeerPeer.Client;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import P2P.App.Peer;
import P2P.App.PeerCommands;
import P2P.App.PeerController;
import P2P.PeerPeer.Message.MessageChunkList;
import P2P.util.FileDigest;
import P2P.util.FileInfo;

public class Downloader implements DownloaderIface {

	private InetSocketAddress[] seedList;
	private PeerController peerController;
	private FileInfo file;
	private DownloaderThread[] threads;
	private short chunkSize;
	
	private int nChunksDownloaded;
	private int totalChunks;
	private int sizeLastChunk;

	private int progresStep;

	//Statistics
	private long init_time;
	private HashMap<InetSocketAddress, Integer> statsChunks;
	private HashMap<InetSocketAddress, Long> statsTime;

	private int[] chunkState;
	private static final int DOWNLOADED = 1;
	private static final int DOWNLOADING = 0;
	private static final int NO_DOWNLOADED = -1;

	public static final int NEW_LIST = -1;
	public static final int ALL_ASSIGNED = -2;

	public Downloader(PeerController peerController, FileInfo file, short chunkSize) {
		this.nChunksDownloaded = 0;
		this.totalChunks = (int) Math.ceil(Float.valueOf(file.fileSize) / Float.valueOf(chunkSize));
		if (totalChunks == 0) //Is only one chunk
			this.totalChunks = 1;

		this.peerController = peerController;
		this.file = file;
		this.chunkSize = chunkSize;
		this.chunkState = new int[getTotalChunks()];

		this.init_time = System.nanoTime();
		this.statsChunks = new HashMap<InetSocketAddress, Integer>();
		this.statsTime = new HashMap<InetSocketAddress, Long>();
		
		//Size of the last chunk
		int size = (int) (file.fileSize % (long) chunkSize);
		if (size == 0)
			this.sizeLastChunk = chunkSize;
		else
			this.sizeLastChunk = size;

		for (int i = 0; i < chunkState.length; i++)
			chunkState[i] = NO_DOWNLOADED;

		//Measure percentage per nChunks
		progresStep = totalChunks / 100;
		if (progresStep == 0)
			progresStep = 1;
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
		return sizeLastChunk;
	}

	@Override
	public boolean downloadFile(InetSocketAddress[] seedList) {
		threads = new DownloaderThread[seedList.length];
		for (int i = 0; i < seedList.length; i++) {
			threads[i] = new DownloaderThread(this, seedList[i]);
			threads[i].start();
		}

		joinDownloaderThreads();
		end();
		return isDownloadComplete();
	}

	public synchronized int bookNextChunk(MessageChunkList message) {
		List<Integer> list = message.getIndex();
		if (message.isAll()) { //The seed has the complete file
			for (int i = 0; i < chunkState.length; i++)
				if (chunkState[i] == NO_DOWNLOADED) {
					chunkState[i] = DOWNLOADING;
					return i + 1;
				}
		} else { //The seed has some chunks
			for (int i : list)
				if (chunkState[i] == NO_DOWNLOADED) {
					chunkState[i] = DOWNLOADING;
					return i + 1;
				}
		}
		for (int i : chunkState)
			if (i == NO_DOWNLOADED)
				return NEW_LIST; // The peer doesn't have a chunk that is needed

		return ALL_ASSIGNED; // All the chunks are being downloaded
	}

	public synchronized void setChunkDownloaded(int chunk, boolean achieved) {
		if (achieved) {
			// Send Add_Seed if is the first chunk downloaded
			if (nChunksDownloaded == 0) { 
				peerController.setCurrentCommand(PeerCommands.COM_ADDSEED);
				String args[] = new String[3];
				args[0] = file.fileHash;
				args[1] = file.fileName;
				args[2] = Long.toString(file.fileSize);
				peerController.setCurrentCommandArguments(args);
				peerController.processCurrentCommand();
			}
			nChunksDownloaded++;
			chunkState[chunk - 1] = DOWNLOADED;
			
			//Progress
			if (nChunksDownloaded % (progresStep * 5) == 0) {
				System.out.println("Downloading: " + nChunksDownloaded * 100 / getTotalChunks() + "%, "
						+ (nChunksDownloaded * chunkSize) + "/" + file.fileSize);
			}
		} else //The chunk could not be downloaded
			chunkState[chunk] = NO_DOWNLOADED;
	}

	@Override
	public synchronized int[] getChunksDownloadedFromSeeders() {
		LinkedList<Integer> chunks = new LinkedList<Integer>();
		for (int i = 0; i < chunkState.length; i++)
			if (chunkState[i] == DOWNLOADED)
				chunks.add(i);
			

		int[] array = new int[chunks.size()];
		for (int i = 0; i < chunks.size(); i++)
			array[i] = chunks.get(i);

		return array;
	}

	@Override
	public synchronized boolean isDownloadComplete() {
		return nChunksDownloaded == getTotalChunks();
	}

	@Override
	public void joinDownloaderThreads() {
		try {
			for (DownloaderThread t : threads)
				t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public short getChunkSize() {
		return chunkSize;
	}

	public void end() {

		
		if (isDownloadComplete()) { //Check corruption errors
			String computedHash = FileDigest.getChecksumHexString(
					FileDigest.computeFileChecksum(Peer.db.getSharedFolderPath() + "/" + file.fileName));

			if (!computedHash.equals(file.fileHash))
				System.out.println("File: " + file.fileName + " is corrupted");
			else
				Peer.db.addDownloadedFile(file);
		} else
			System.out.println("File: " + file.fileName + " can't be downloaded from seeds");

		//Global statistics
		System.out.println("Chunks: " + nChunksDownloaded);

		long elapsed_time = System.nanoTime() - init_time;
		float seconds = (float) elapsed_time / (float) 1000000000;
		System.out.println("Time: " + seconds + "s");
		float speed = (float) sizeDownloaded() / (float) 1024.0 / seconds;
		System.out.printf("Speed: %.2fkb/s\n", speed);

		
		//Seeds statistics
		Set<InetSocketAddress> keys = statsChunks.keySet();
		for (InetSocketAddress seed : keys) {
			elapsed_time = statsTime.get(seed) - init_time;
			seconds = (float) elapsed_time / (float) 1000000000;
			System.out.println("Seed " + seed.toString() + " Chunks:" + statsChunks.get(seed) + ", Speed: "
					+ ((float) statsChunks.get(seed) * chunkSize) / (float) 1024.0 / seconds + "kb/s");
		}
	}

	/**
	 * Calculate the bytes downloaded considering if the last chunk
	 * has been downloaded
	 * @return Bytes downloaded
	 */
	public long sizeDownloaded() {
		boolean lastDownloaded = chunkState[chunkState.length - 1] == DOWNLOADED;
		if (isDownloadComplete())
			return file.fileSize;

		if (lastDownloaded)
			return (nChunksDownloaded - 1) * chunkSize + sizeLastChunk;
		else
			return nChunksDownloaded * chunkSize;
	}

	public synchronized void updateStats(InetSocketAddress seed, int numChunksDownloaded) {
		statsChunks.put(seed, numChunksDownloaded);
		statsTime.put(seed, System.nanoTime());

	}
}
