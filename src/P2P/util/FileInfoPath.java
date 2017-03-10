package P2P.util;

public class FileInfoPath extends FileInfo {
	public String filePath; 
	
	public FileInfoPath(String hash, String name, long size, String path) {
		super(hash, name, size);
		filePath = path;
	}

	public FileInfoPath() {
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" Hash:"+fileHash);
		strBuf.append(String.format(" Size: %1$9s",fileSize));	
//		strBuf.append(" FileName:"+fileName);
		strBuf.append(" File:"+filePath);
		return strBuf.toString();
	}

	public String getPathFileName() {
		return filePath+System.getProperty("file.separator")+fileName;
	}
}
