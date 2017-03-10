package P2P.util;

import java.io.File;
import java.util.Iterator;
import java.util.Map;


public class PeerDatabase {
	
	private Map<String,FileInfoPath> files;
	public final String sharedFolderPath;

	public PeerDatabase(String sharedFolder) {
		if (new File(sharedFolder).isAbsolute()) {
			sharedFolderPath = sharedFolder;
		}
		else {
			sharedFolderPath = new File(System.getProperty("user.home"),sharedFolder).getPath();
		}

		this.files = FileInfo.loadFileMapFromFolder(new File(sharedFolderPath));
	}


	public void addDownloadedFile(FileInfo newFile) {
        String fileName = newFile.fileName;
        String filePath = new File(sharedFolderPath, fileName).getPath();
		String fileHash = newFile.fileHash; 
		assert(fileHash.equals(FileDigest.getChecksumHexString(FileDigest.computeFileChecksum(filePath)))); 
		long fileSize = newFile.fileSize;
		files.put(fileHash, new FileInfoPath(fileHash, fileName, fileSize, filePath));
	}
	
	/**
	 * Obtain metadata info of files on this peer's shared folder. The
	 * list is returned as a FileInfo array, which can be directly passed as input
	 * argument to make the initial ADD_SEED request when connecting to tracker.
	 * @return An array of FileInfo containing the metadata of each file. 
	 */
	public FileInfo[] getLocalSharedFiles() {
		FileInfo[] fileinfoarray = new FileInfo [files.size()];
		Iterator<FileInfoPath> itr = files.values().iterator();
		int numFiles=0;
		while(itr.hasNext()) {
			fileinfoarray[numFiles++] = itr.next();
		}
		return fileinfoarray;
	}

	public String lookupFilePath(String fileHash) {
		FileInfoPath f = files.get(fileHash);		
		if (f != null) {
			return f.filePath;
		}
		return null;
	}
	
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		Iterator<FileInfoPath> itr = files.values().iterator();
		while(itr.hasNext()) {
			FileInfo file = itr.next();
			strBuf.append(file+System.lineSeparator());
		}
		return strBuf.toString(); 
		
	}


	public String getSharedFolderPath() {
		return sharedFolderPath;
	}
}
