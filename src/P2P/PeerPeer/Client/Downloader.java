package P2P.PeerPeer.Client;

import java.net.InetSocketAddress;

import P2P.util.FileInfo;

//nuevo
public class Downloader implements DownloaderIface {

	private InetSocketAddress[] seedList;
	
	@Override
	public FileInfo getTargetFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InetSocketAddress[] getSeeds() {
		return seedList;
	}

	@Override
	public int getTotalChunks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean downloadFile(InetSocketAddress[] seedList) {
		DownloaderThread d = new DownloaderThread(this,seedList[0]);
		d.start();
		return false;
	}

	@Override
	public int[] getChunksDownloadedFromSeeders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDownloadComplete() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void joinDownloaderThreads() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public short getChunkSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
