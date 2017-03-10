package P2P.util;

import java.net.InetSocketAddress;
import java.util.HashSet;

public class FileInfoSeeds extends FileInfo {
	public HashSet<InetSocketAddress> seedList; 
	
	public FileInfoSeeds(String hash, String name, long size, InetSocketAddress[] seeds) {
		super(hash, name, size);
		seedList = new HashSet<InetSocketAddress>();
		for(int i=0; i<seeds.length; i++) {
			seedList.add(seeds[i]);			
		}
	}

	public FileInfoSeeds(FileInfo fileInfo, InetSocketAddress seed) {
		super(fileInfo.fileHash, fileInfo.fileName, fileInfo.fileSize);
		seedList = new HashSet<InetSocketAddress>();
		seedList.add(seed);	
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" FileHash:"+fileHash);
		strBuf.append(" FileSize:"+fileSize);
		strBuf.append(" FileName:"+fileName);
		strBuf.append(" SeedList:"+seedList);
		return strBuf.toString();
	}
}
