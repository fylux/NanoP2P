package P2P.App;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import P2P.PeerPeer.Client.Downloader;
import P2P.PeerPeer.Server.Seeder;
import P2P.PeerTracker.Client.Reporter;
import P2P.PeerTracker.Message.Message;
import P2P.PeerTracker.Message.MessageConf;
import P2P.PeerTracker.Message.MessageFileInfo;
import P2P.PeerTracker.Message.MessageQuery;
import P2P.PeerTracker.Message.MessageSeedInfo;
import P2P.util.FileInfo;

public class PeerController implements PeerControllerIface {
	/**
	 * The shell associated to this controller.
	 */
	private PeerShellIface shell;

	private byte currentCommand;
	private Reporter reporter;

	private int port;
	private String[] args;
	private FileInfo[] queryResult;
	private short chunkSize;
	
	private Downloader downloader;
	private Seeder seeder;

	public PeerController(Reporter reporter) {
		this.reporter = reporter;
		shell = new PeerShell();
	}
	
	public byte getCurrentCommand() {
		return currentCommand;
	}

	public void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	public void readCommandFromShell() {
		
		shell.readCommand();
		setCurrentCommand(shell.getCommand());
		setCurrentCommandArguments(shell.getCommandArguments());
	}

	public void publishSharedFilesToTracker() {
		setCurrentCommand(PeerCommands.COM_ADDSEED);
		setCurrentCommandArguments(null);
		processCurrentCommand();
	}

	public void removeSharedFilesFromTracker() {
		setCurrentCommand(PeerCommands.COM_QUIT);
		processCurrentCommand();
	}

	public void getConfigFromTracker() {
		setCurrentCommand(PeerCommands.COM_CONFIG);
		processCurrentCommand();
	}

	public boolean shouldQuit() {
		return currentCommand == PeerCommands.COM_QUIT;
	}

	@Override
	public void setCurrentCommandArguments(String[] args) {
		if (args!=null)
			this.args = Arrays.copyOf(args,args.length);
		else this.args=null;
		//para que this.args no tenga valores basura.
	}

	@Override
	public void processCurrentCommand() {
		switch (currentCommand) {
			case PeerCommands.COM_CONFIG: {
				Message m = createMessageFromCurrentCommand();
				processMessageFromTracker(reporter.conversationWithTracker(m));
				break;
			}
			case PeerCommands.COM_ADDSEED: {
				Message m = createMessageFromCurrentCommand();
				processMessageFromTracker(reporter.conversationWithTracker(m));
				break;
			}
			case PeerCommands.COM_QUERY: {
				Message m = createMessageFromCurrentCommand();
				processMessageFromTracker(reporter.conversationWithTracker(m));
				break;
			}
			case PeerCommands.COM_DOWNLOAD: {
				Message m = createMessageFromCurrentCommand();
				if (m != null)
					processMessageFromTracker(reporter.conversationWithTracker(m));
				break;
			}
			case PeerCommands.COM_SHOW: {
				FileInfo[] fileList = Peer.db.getLocalSharedFiles();
				for (FileInfo f: fileList)
					System.out.println(f.fileName);
				break;
			}
			case PeerCommands.COM_QUIT: {
				shell.close();
				Message m = createMessageFromCurrentCommand();
				processMessageFromTracker(reporter.conversationWithTracker(m));
				break;
			}
			default: ;
		}
	}

	@Override
	public Message createMessageFromCurrentCommand() {
		Message m = null;
		switch (currentCommand) {
			case PeerCommands.COM_CONFIG: {
				m = Message.makeGetConfRequest();
				break;
			}
			case PeerCommands.COM_ADDSEED: {
				FileInfo[] fileList;
				if (args == null)
					fileList = Peer.db.getLocalSharedFiles();
				else {
					fileList = new FileInfo[1];
					fileList[0] = new FileInfo(args[0],args[1],Long.parseLong(args[2]));
				}
				m = Message.makeAddSeedRequest(port, fileList);
				break;
			}
			case PeerCommands.COM_QUERY: {
				if (args!=null)
					m = createMessageQuery();
				else m = Message.makeQueryFilesRequest(MessageQuery.FILTERTYPE_ALL,"");
			
				
				break;
			}
			case PeerCommands.COM_DOWNLOAD: {
				FileInfo[] hash = lookupQueryResult(args[0]);
				if (hash.length != 1)
					System.err.println("Unknown File Hash");
				else
					m = Message.makeGetSeedsRequest(hash[0].fileHash);
				break;
			}
			case PeerCommands.COM_QUIT: {
				FileInfo[] fileList = Peer.db.getLocalSharedFiles();
				m = Message.makeRemoveSeedRequest(port, fileList);
				break;
			}
			default:
				;
		}
		return m;
	}

	@Override
	public void processMessageFromTracker(Message response) {
		if (response == null) {
			System.err.println("Tracker unreachable");
		}
		else {
		switch (response.getOpCode()) {
			case Message.OP_SEND_CONF: {
				chunkSize=((MessageConf) response).getChunkSize();
				break;
			}
			case Message.OP_ADD_SEED_ACK: {
				break;
			}
			case Message.OP_FILE_LIST: {
				recordQueryResult(((MessageFileInfo)response).getFileList());
				printQueryResult();
				break;
			}
			case Message.OP_REMOVE_SEED_ACK: {
				seeder.closeSocket();
				break;
			}
			
			case Message.OP_SEED_LIST: {
				downloadFileFromSeeds( ((MessageSeedInfo)response).getSeedList(), ((MessageSeedInfo)response).getFileHash());
				break;
			}
			default: ;
		}
		}
	}

	@Override
	public void recordQueryResult(FileInfo[] fileList) {
		 queryResult=FileInfo.removeLocalFilesFromFileList(fileList);
	
	}

	@Override
	public void printQueryResult() {
		
			int nlocal=0;
			for (FileInfo s : queryResult) {
			//	if (!isLocalFile(s))
					System.out.println(s);
				//else nlocal++;
			}
			if (queryResult.length==0) System.out.println("Empty Query Result");
			
	}

	
	public boolean isLocalFile(FileInfo other){
		for (FileInfo f : Peer.db.getLocalSharedFiles()){
			//System.out.println("local");
			// System.out.println(f); 
			//q System.out.println("other: "+other.fileHash);
			if (f.fileHash.equals(other.fileHash))
				 	return true;
			 
		}
		return false;
	}
	
	@Override
	public FileInfo[] lookupQueryResult(String hashSubstr) {
		ArrayList<FileInfo> matched = new ArrayList<FileInfo>();
		for (FileInfo s : queryResult)
			if (s.fileHash.startsWith(hashSubstr))
				matched.add(s);

		return matched.toArray(new FileInfo[matched.size()]);
	}

	@Override
	public void downloadFileFromSeeds(InetSocketAddress[] seedList, String targetFileHash) {
		downloader = new Downloader(this,lookupQueryResult(targetFileHash)[0],chunkSize);
		seeder.setCurrentDownloader(downloader);
		downloader.downloadFile(seedList);
		seeder.setCurrentDownloader(null);
	}

	public void listenSeeder() {
		seeder = new Seeder(chunkSize);
		port = seeder.getSeederPort();
		seeder.start();
		seeder.setCurrentDownloader(null);
	}
	
	private Message createMessageQuery(){
		
		for (int i=0;i<args.length;i+=3) {
			switch (args[i]) {
				case "-n":
					return Message.makeQueryFilesRequest(MessageQuery.FILTERTYPE_NAME,args[i+1]);
				case "-lt":
					return Message.makeQueryFilesRequest(MessageQuery.FILTERTYPE_MAXSIZE,sizeInByte(args[i+1],args[i+2]));
				case "-ge":
					return Message.makeQueryFilesRequest(MessageQuery.FILTERTYPE_MINSIZE,sizeInByte(args[i+1],args[i+2]));
				}
		}
		return null;
	}

	private String sizeInByte(String value,String size){
		
		switch (size) {
		case "KB":
			return Integer.toString(Integer.valueOf(value)* 1024);
		case "MB":
			return Integer.toString(Integer.valueOf(value)* 1024*1024);
		case "GB":
			return Integer.toString(Integer.valueOf(value)* 1024*1024*1024);
		}
		return value;
	}

}