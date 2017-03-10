package P2P.PeerTracker.Server;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import P2P.PeerTracker.Message.MessageQuery;
import P2P.util.FileInfo;
import P2P.util.FileInfoSeeds;

public enum TrackerDatabase {

	db;
	
	private Map<String,FileInfoSeeds> files;

	TrackerDatabase() {
		this.files = new HashMap<String,FileInfoSeeds>();

	}

	public InetSocketAddress[] getSeeds(String fileHash) {
		FileInfoSeeds f = files.get(fileHash);
		if (f != null) {
			assert(f.seedList.size() > 0);
			return f.seedList.toArray(new InetSocketAddress[f.seedList.size()]);
		}
		else
			return new InetSocketAddress[0];
	}

	/**
	 * @return The list of ALL files in the tracker database
	 */
	public FileInfo[] getFileInfoList(String filterValue) {
		boolean noFiltering = false;
		if (filterValue.length() == 0) noFiltering = true;
		List<FileInfo> fileinfolist = new ArrayList<FileInfo>();
		Iterator<FileInfoSeeds> itr = files.values().iterator();
		while(itr.hasNext()) {
			FileInfoSeeds f = itr.next();
			if (noFiltering) {
				fileinfolist.add(f);
			}
			else {
				if (f.fileName.toLowerCase().contains(filterValue.toLowerCase())) fileinfolist.add(f);
			}
		}
		FileInfo[] array = fileinfolist.toArray(new FileInfo[fileinfolist.size()]);
		return array;
	}

	// TODO: Refactor this code, one method should be enough to apply any filter.
	/* Pass an object that implements the "Filter" interface with a method bool filter(FileInfo f), 
 	 * then have a separate class for each filter, implementing the method 'filter'
 	 * to return true when the file f passes the filter.
 	 */
	private FileInfo[] getFileInfoListFilterPerSize(boolean maxSize,
			long size) {
		List<FileInfo> fileinfolist = new ArrayList<FileInfo>();
		Iterator<FileInfoSeeds> itr = files.values().iterator();
		while(itr.hasNext()) {
			FileInfoSeeds f = itr.next();
			if (maxSize) {
				if (f.fileSize < size) fileinfolist.add(f);
			}
			else {
				if (f.fileSize >= size) fileinfolist.add(f);
			}
		}
		FileInfo[] array = fileinfolist.toArray(new FileInfo[fileinfolist.size()]);
		return array;
	}

	public void addSeedToFileList(FileInfo[] fileList, InetSocketAddress clientSockAddr) {
		for(int i=0; i < fileList.length; i++) {
			FileInfoSeeds value = files.get(fileList[i].fileHash);
			if (value != null) { // File exists in database
				if (value.seedList.contains(clientSockAddr) == false) {
					value.seedList.add(clientSockAddr);
					System.out.println("      New seed "+clientSockAddr+" added for file: "+value);
				}
				assert(value.fileName.equals(fileList[i].fileName));
				assert(value.fileSize == (fileList[i].fileSize));
				assert(files.get(fileList[i].fileHash).seedList.contains(clientSockAddr));
			}
			else {
				value = new FileInfoSeeds(fileList[i], clientSockAddr);
				files.put(fileList[i].fileHash, value);
			}
		}
	}
	
	public void removeSeedFromFileList(FileInfo[] fileList, InetSocketAddress seedId) {
		for(int i=0; i < fileList.length; i++) {
			FileInfoSeeds value = files.get(fileList[i].fileHash);
			if (value != null) { // File exists in database
				if (value.seedList.remove(seedId)) { // Seed was removed from seed list
					if (value.seedList.isEmpty()) { // Seed list becomes empty
						if (files.remove(fileList[i].fileHash) == null) {
							assert(false); // Key should exist and be removed
						}
						System.out.println("      Removed from database: "+value);
					}
					else { // More seeds remain, keep file in database 
						System.out.println("      Removed from seedlist: "+value);
					}
				}
				else { // Not removed from seed list
					System.err.println("      Peer not found in seedlist (not removed): "+value);				
				}
			} 
			else { // File not found in database
				System.err.println("      File not found in database : "+fileList[i]);				
			}
		}
	}
	public void disconnectPeer(InetSocketAddress clientSockAddr) {
		// Remove this peer from all seed lists
		Iterator<FileInfoSeeds> itr = files.values().iterator();
		while(itr.hasNext()) {
			FileInfoSeeds info = itr.next();
			if (info.seedList.remove(clientSockAddr)) {
				if (info.seedList.isEmpty()) {
					itr.remove();
					System.out.println("      Removed from database: "+info);
				}
				else {
					System.out.println("      Removed from seedlist: "+info);
				}
			}
			else {
				assert(info.seedList.isEmpty() == false);
			}
		}
	}

	public void connectPeer(InetSocketAddress clientSockAddr) {
		// Nothing to do: seed lists are updated upon disconnect
	}

	public void queryFromPeer(InetSocketAddress clientSockAddr) {
		// Nothing to do to process request, makeResponse will call getFileInfoList() 
	}

	public FileInfo[] queryFilesMatchingFilter(byte filterType, String filterValue) {
		boolean maxSize = false;
		switch(filterType) {
			case MessageQuery.FILTERTYPE_ALL:
				return getFileInfoList("");
			case MessageQuery.FILTERTYPE_NAME:
				return getFileInfoList(filterValue);
			case MessageQuery.FILTERTYPE_MAXSIZE:
				maxSize = true;
			case MessageQuery.FILTERTYPE_MINSIZE:
				return getFileInfoListFilterPerSize(maxSize, Long.parseLong(filterValue));
			default:
				System.err.println("      Invalid query filter type: "+filterType);				
				return new FileInfo[0];
		}
	}
}
