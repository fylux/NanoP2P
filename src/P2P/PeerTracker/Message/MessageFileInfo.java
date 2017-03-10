package P2P.PeerTracker.Message;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import P2P.util.FileDigest;
import P2P.util.FileInfo;

/**
 * @author rtitos
 * 
 * Peer-tracker protocol data message, format "FileInfo" 
 * 
    1b      1b         1b        1b      2b      2b       20b       8b           4b        0 .. long.filename-1   
 +----------------------------------------------------------------------------------------------------+
 |opcode| trans_id |  total  |  seqnum | port |list.len| hash   | filesize  |filename.len | filename   |
 +------+----------+---------+---------+------+--------+--------+-----------+-------------+------------+
                                                       <------------ FileInfo (list.len times)-------->
                 
 */
public class MessageFileInfo extends Message {

	/**
	 * Size of "filesize" field: long (8 bytes)
	 */
	private static final int FIELD_FILESIZE_BYTES = Long.SIZE / 8;  
	/**
	 * Size of "filename.len" field: int (4 bytes)
	 */
	private static final int FIELD_FILENAMELEN_BYTES = Integer.SIZE / 8;
	/**
	 * Size of "total fragments" field: 1 byte
	 */
	private static final int FIELD_TOTALFRAGMENTS_BYTES = 1;
	/**
	 * Size of "fragment sequence number" field: 1 byte
	 */
	private static final int FIELD_FRAGMENTSEQNUM_BYTES = 1;

	/**
	 * Number of fileList elements printed by toString method
	 */
	private static final int MAX_FILELIST_ELEMENTS_PRINTED = 10;
	
	/**
	 * Message opcodes that use the FileInfo format
	 */
	private static final Byte[] _datafile_opcodes = {
			OP_ADD_SEED, 
			OP_FILE_LIST,
			OP_REMOVE_SEED};
	
	/**
	 * The port number where this seeder listens
	 */
	private int seederPort;

	/**
	 * The list of "FileInfo" entries contained in the message.
	 */
	private FileInfo[] fileList;
	/**
	 * The sequence number of this fragment in a multi-fragment message
	 */
	private byte fragmentSeqNum;
	/**
	 * The total number of fragments that form this message
	 * */
	private byte totalFragments;
	
	/**
	 */
	public MessageFileInfo(byte opCode, byte tid, int port, FileInfo[] fileList) {
		setOpCode(opCode);
		setTransId(tid);
		this.seederPort = port;
		this.fileList = fileList;
		this.fragmentSeqNum = 0;
		this.totalFragments =  MessageFileInfo.computeTotalFragments(fileList);
		assert(this.totalFragments == 1);
		valid = true;
		// Constructor used when creating ADD_SEED/QUERY_FILES requests (port info required)
	}

	/**
	 * Constructor used by tracker when creating message response after receiving
	 * @param buf
	 */
	public MessageFileInfo(byte[] buf) {
		if (fromByteArray(buf) == false) {
			throw new RuntimeException("Failed to parse message: format is not DataFileInfo.");
		}
		else {
			assert(valid);
		}
	}

	/**
	 * Constructor used by tracker when creating fragmented response
	 * @param opCode
	 * @param tid
	 * @param numFragments
	 * @param seqNum
	 * @param fragmentFileList
	 */
	public MessageFileInfo(byte opCode, byte tid, byte numFragments,
			byte seqNum, FileInfo[] fragmentFileList) {
		setOpCode(opCode);
		setTransId(tid);
		this.fragmentSeqNum = 0;
		this.totalFragments = numFragments;
		this.fragmentSeqNum = seqNum;
		this.fileList = fragmentFileList;
		valid = true;
	}

	/**
	 * Creates a byte array ready for sending a datagram packet, from a valid message of FileInfo format 
	 */
	public byte[] toByteArray()
	{
		int fileInfoBytes=0;
		// FileInfo fields with constant size: hash, filesize and filename length
		fileInfoBytes += fileList.length*(FIELD_FILEHASH_BYTES + FIELD_FILESIZE_BYTES + 
				FIELD_FILENAMELEN_BYTES);

		for(int i=0; i < fileList.length; i++) {
			// Variable filename length
			fileInfoBytes += fileList[i].fileName.getBytes().length;
		}
		int byteBufferLength = FIELD_OPCODE_BYTES + FIELD_TRANSID_BYTES + FIELD_TOTALFRAGMENTS_BYTES +
				FIELD_FRAGMENTSEQNUM_BYTES + FIELD_PORT_BYTES + FIELD_LONGLIST_BYTES + fileInfoBytes;
		
		ByteBuffer buf = ByteBuffer.allocate(byteBufferLength);

		// Opcode
		buf.put((byte)this.getOpCode());
		
		// Trans id
		buf.put((byte)this.getTransId());

		// Total fragments
		buf.put((byte)this.getTotalFragments());
		
		// Fragment sequence number
		buf.put((byte)this.getFragmentSeqNum());

		// Seeder port
		buf.putShort((short)this.getPort());
		
		// List length
		buf.putShort((short)fileList.length);

		for(int i=0; i < fileList.length;i++) {
			// File hash
			buf.put(FileDigest.getDigestFromHexString(fileList[i].fileHash));

			// File size
			buf.putLong(fileList[i].fileSize);
			
			// Filename length 
			buf.putInt(fileList[i].fileName.length());
			
			// Filename
			buf.put(fileList[i].fileName.getBytes());
		}

		return buf.array();
	}

	/**
	 * Creates a valid message of FileInfo format, from the byte array of the received packet
	 */
	protected boolean fromByteArray(byte[] array) {
		if (array.length < FIELD_OPCODE_BYTES + FIELD_TRANSID_BYTES + FIELD_TOTALFRAGMENTS_BYTES +
				FIELD_FRAGMENTSEQNUM_BYTES + FIELD_PORT_BYTES + FIELD_LONGLIST_BYTES ) {
			System.err.println("Contenido del byte array "+array+" no es mensaje con formato FileInfo");
			throw new RuntimeException("Byte array no contiene un mensaje con formato FileInfo");
		}
		ByteBuffer buf = ByteBuffer.wrap(array);
		try {
			// Opcode
			setOpCode(buf.get());
			// Trans id
			setTransId(buf.get());
			// Total fragments
			setTotalFragments(buf.get());
			// Seq num
			setFragmentSeqNum(buf.get());
			// Seeder port
			setPort(buf.getShort());
			// List length
			int listLength = buf.getShort();

			this.fileList = new FileInfo[listLength];
			byte[] hasharray = new byte[FIELD_FILEHASH_BYTES];
			for(int i=0; i < listLength;i++) {
				FileInfo info = new FileInfo();
				// File hash
				buf.get(hasharray, 0, FIELD_FILEHASH_BYTES);
				info.fileHash = new String(FileDigest.getChecksumHexString(hasharray));
				// File size
				info.fileSize = buf.getLong();
				// Filename length
				int filenameLen = buf.getInt();				
				byte[] filenamearray = new byte[filenameLen];
				// Filename
				buf.get(filenamearray, 0, filenameLen);				
				info.fileName = new String(filenamearray);
				this.fileList[i] = info;
			}
			valid = true;		
		} catch (RuntimeException e) {
			e.printStackTrace();
			assert(valid == false);
		}
		return valid;
	}	

	public int getTotalFragments() {
		return (int)this.totalFragments;
	}

	public void setTotalFragments(byte total) {
		this.totalFragments = total;
	}
	
	public byte getFragmentSeqNum() {
		return this.fragmentSeqNum;
	}
	
	public void setFragmentSeqNum(byte seqNum) {
		this.fragmentSeqNum = seqNum;
	}

	public int getPort() {
		return this.seederPort;
	}

	private void setPort(int port) {
		this.seederPort = port;
	}

	public FileInfo[] getFileList() {
		return this.fileList;
	}
	
	public String toString() {
		assert(valid);
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(" Type:"+this.getOpCodeString());
		strBuf.append(" TransId:"+this.getTransId());
		strBuf.append(" Total:"+this.getTotalFragments());
		strBuf.append(" SeqNum:"+this.getFragmentSeqNum());
		strBuf.append(" SeedPort:"+this.getPort());
		strBuf.append(" LongLista:"+this.fileList.length);
		strBuf.append(" FileInfoList:");
		for(int i=0; i < this.fileList.length;i++) {
			if (i == MAX_FILELIST_ELEMENTS_PRINTED) {
				strBuf.append(System.lineSeparator()+"      [...] ("+(this.fileList.length-MAX_FILELIST_ELEMENTS_PRINTED)+" list elements remaining, skipping)");
				
				break;
			}
			strBuf.append(System.lineSeparator()+"      ["+this.fileList[i]+"]");
		}
		return strBuf.toString();
	}

	/**
	 * For checking opcode validity. 	
	 */
	private static final Set<Byte> datafile_opcodes =
		Collections.unmodifiableSet(new HashSet<Byte>(Arrays.asList(_datafile_opcodes)));

	protected void _check_opcode(byte opcode)
	{
		if (!datafile_opcodes.contains(opcode))
			throw new RuntimeException("Opcode " + opcode + " is not DataFile type.");
	}

	private static int getFileInfoPayloadBytes(FileInfo fileInfo) {
		return FIELD_FILEHASH_BYTES + FIELD_FILESIZE_BYTES + 
				FIELD_FILENAMELEN_BYTES + fileInfo.fileName.getBytes().length;
	}

	public static int computeNextFragmentNumFiles(FileInfo[] files, int currentIndex) {
		int fragmentPayloadSize = 0;
		int i;
		for(i = currentIndex; i < files.length; i++) {
			fragmentPayloadSize += getFileInfoPayloadBytes(files[i]);
			if (fragmentPayloadSize > Message.MAX_UDP_PACKET_LENGTH) break;
		}
		return i - currentIndex;
	}

	private static byte computeTotalFragments(FileInfo[] fileList) {
		int numFragments = 0;
		int currentIndex = 0;
		do {
			// Determine how many files fit in the next add_seed datagram
			int fragmentNumFiles = MessageFileInfo.computeNextFragmentNumFiles(
					fileList, currentIndex);
			currentIndex += fragmentNumFiles;
			numFragments++;
		} while (currentIndex < fileList.length);
		assert(numFragments < Byte.MAX_VALUE);
		return (byte)numFragments;
	}

	public static Vector<FileInfo[]> computeFragments(FileInfo[] files) {
		Vector<FileInfo []> fragments = new Vector<FileInfo []>();
		int numFragments = 0;
		int currentIndex = 0;
		do {
			// Determine how many files fit in each fragment
			int fragmentNumFiles = MessageFileInfo.computeNextFragmentNumFiles(
					files, currentIndex);
			FileInfo [] fragmentFiles = new FileInfo[fragmentNumFiles];
			System.arraycopy(files, currentIndex, fragmentFiles, 0, fragmentNumFiles);
			fragments.add(fragmentFiles);
			currentIndex += fragmentNumFiles;
			numFragments++;
		} while (currentIndex < files.length);
		assert(numFragments < Byte.MAX_VALUE);
		return fragments;
	}

	@Override
	public void reassemble(Vector<Message> fragments) {
		// Sanity check: make sure we have all fragments before merging them
		String errorStr="Failed to reassemble fragments: ";
		if (this.getTotalFragments() <= 1) throw new IllegalArgumentException(errorStr+"invalid fragment count in 'this'");
		if (fragments.size() < this.getTotalFragments()) throw new IllegalArgumentException(errorStr+"fragments missing, expected "+this.getTotalFragments()+" but vector has "+fragments.size());
		if (fragments.size() > this.getTotalFragments()) throw new IllegalArgumentException(errorStr+"too many fragments, expected "+this.getTotalFragments()+" but vector has "+fragments.size());

		// Bit-array to keep track of fragment seqNums in vector
		boolean[] seqNumsPresent = new boolean[fragments.size()]; // Init to false by default
		int totalFiles = 0;	// Count of all fileLists combined
		for (int i = 0; i < fragments.size(); i++)
		{
			MessageFileInfo f = (MessageFileInfo) fragments.get(i);
			// Check fragment transId matches this
			if (f.getTransId() != this.getTransId()) throw new IllegalArgumentException(errorStr+"transId mismatch at index "+i+", this.transId="+this.getTransId());
			// Check fragment totalFragments matches this
			if (f.getTotalFragments() != this.getTotalFragments()) throw new IllegalArgumentException(errorStr+"total mismatch at index "+i+", this.total="+this.getTotalFragments());
			int seqNum = f.getFragmentSeqNum();
			// Check range for fragment seqNum
			if (seqNum < 0 || seqNum >= this.getTotalFragments()) throw new IllegalArgumentException(errorStr+"invalid seqNum at index "+i);
			// Check for duplicated fragment seqNums
			if (seqNumsPresent[seqNum]) throw new IllegalArgumentException(errorStr+"duplicated fragment seqNum "+seqNum);

			// All sanity checks passed
			seqNumsPresent[seqNum] = true;
			// Add up files carried by this fragment
			totalFiles += f.getFileList().length;
		}
		// Now make sure all fragments have been received
		for (int i = 0; i < fragments.size(); i++) { 
			if (seqNumsPresent[i] == false) throw new IllegalArgumentException(errorStr+"missing fragment seqNum "+i);
		}	
		// Allocate merged file list
		FileInfo [] mergedFileList = new FileInfo[totalFiles];
		int currentIndex = 0;
		for (int i = 0; i < fragments.size(); i++)
		{
			MessageFileInfo f = (MessageFileInfo) fragments.get(i);
			System.arraycopy(f.getFileList(), 0, mergedFileList, currentIndex, f.getFileList().length);
			// Advance index in merged file list
			currentIndex += f.getFileList().length;
		}
		// Replace this fragment's file list with merged file list
		this.fileList = mergedFileList;
		// Reset total fragments and seqNum
		this.setTotalFragments((byte)1);
		this.setFragmentSeqNum((byte)0);
	}

	@Override
	public boolean fragmented() {
		return this.totalFragments > 1;
	}
}
