package P2P.util;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import P2P.App.Peer;

public class FileInfo {
	public String fileHash;
	public String fileName;
	public long fileSize;
	
	public FileInfo() {
	}
	
	public FileInfo(String hash, String name, long size) {
		fileHash = hash;
		fileName = name;
		fileSize = size;
	}
	
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" FileHash:"+fileHash);
		strBuf.append(String.format(" FileSize: %1$9s",fileSize));
		strBuf.append(" FileName: "+fileName);
		return strBuf.toString();
	}

	/**
	 * Scans the given directory and returns an array of FileInfo objects, one for
	 * each file recursively found in the given folder and its subdirectories.
	 * @param sharedFolderPath The folder to be scanned
	 * @return An array of file metadata (FileInfo) of all the files found 
	 */
	public static FileInfo[] loadFilesFromFolder(String sharedFolderPath) {
		File folder = new File(sharedFolderPath);
		
		Map<String,FileInfoPath> files = loadFileMapFromFolder(folder);
		
	    FileInfo[] fileinfoarray = new FileInfo [files.size()];
		Iterator<FileInfoPath> itr = files.values().iterator();
		int numFiles=0;
		while(itr.hasNext()) {
			fileinfoarray[numFiles++] = itr.next();
		}
		return fileinfoarray;
	}

	/**
	 * Scans the given directory and returns a map of <filehash,FileInfo> pairs. 
	 * @param folder The folder to be scanned
	 * @return A map of the metadata (FileInfo) of all the files recursively found 
	 * in the given folder and its subdirectories.
	 */
	public static Map<String,FileInfoPath> loadFileMapFromFolder(final File folder) {
		Map<String,FileInfoPath> files = new HashMap<String,FileInfoPath>();
		scanFolderRecursive(folder, files);
		return files;
	}
		
	private static void scanFolderRecursive(final File folder, Map<String,FileInfoPath> files) {
		if (folder.exists() == false) {
			System.err.println("scanFolder cannot find folder "+folder.getPath());
			return;
		}
		if (folder.canRead() == false) {
			System.err.println("scanFolder cannot access folder "+folder.getPath());
			return;
		}
		
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            scanFolderRecursive(fileEntry, files);
	        } else {
	            String fileName = fileEntry.getName();
	            String filePath = fileEntry.getPath();
	    		String fileHash = FileDigest.getChecksumHexString(FileDigest.computeFileChecksum(filePath)); 
	    		long fileSize = fileEntry.length();
	    		if (fileSize > 0) {
	    			files.put(fileHash, new FileInfoPath(fileHash, fileName, fileSize, filePath));
	    		}
	    		else {
	    			System.out.println("Ignoring empty file found in shared folder: "+filePath);	    			
	    		}
	        }
	    }
	}

	/***
	 * Filters the given list of files, removing those shared by this 
	 * peer (both complete files and files that are being downloaded)
	 * @param fileList The full list of files returned by the tracker
	 * @return The list of remote files for this peer (not shared this peer)
	 */
	public static FileInfo[] removeLocalFilesFromFileList(FileInfo[] fileList) {
		Vector<FileInfo> remoteFiles = new Vector<FileInfo>();
		for (int i = 0; i < fileList.length; i++) {
			FileInfo f = fileList[i];
			// Only print files that are NOT in the local database (shared
			// folder)
			if (Peer.db.lookupFilePath(f.fileHash) == null) {
				remoteFiles.add(f);
			}
		}
		FileInfo[] queryResult = new FileInfo[remoteFiles.size()];
		remoteFiles.toArray(queryResult);
		return queryResult;
	}
}
