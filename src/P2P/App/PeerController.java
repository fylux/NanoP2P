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
		default: System.out.println("default CurrentCommand");
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
				FileInfo[] fileList = Peer.db.getLocalSharedFiles();
				m = Message.makeAddSeedRequest(port, fileList);
				break;
			}
			case PeerCommands.COM_QUERY: {
				m = Message.makeQueryFilesRequest((byte)1, "");
				break;
			}
			case PeerCommands.COM_DOWNLOAD: {
				FileInfo[] hash = lookupQueryResult(args[0]);
				if (hash.length != 1)
					System.out.println("Unknown File Hash");
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

		switch (response.getOpCode()) {
		case Message.OP_SEND_CONF: {
			chunkSize=((MessageConf) response).getChunkSize();
			System.out.println(chunkSize);
			break;
		}
		case Message.OP_ADD_SEED_ACK: {
			System.out.println(response.getOpCodeString());
			break;
		}
		case Message.OP_FILE_LIST: {
			recordQueryResult(((MessageFileInfo)response).getFileList());
			printQueryResult();
			break;
		}
		case Message.OP_REMOVE_SEED_ACK: {
			System.out.println(response.getOpCodeString());
			break;
		}
		
		case Message.OP_SEED_LIST: {
			downloadFileFromSeeds( ((MessageSeedInfo)response).getSeedList(), ((MessageSeedInfo)response).getFileHash());
			break;
		}
		default:
			;
		}

	}

	@Override
	public void recordQueryResult(FileInfo[] fileList) {
		 queryResult= Arrays.copyOf(fileList,fileList.length);
	}

	@Override
	public void printQueryResult() {
		for (FileInfo s : queryResult) {
				System.out.println(s);
		}

	}

	@Override
	public FileInfo[] lookupQueryResult(String hashSubstr) {
		ArrayList<FileInfo> matched = new ArrayList<FileInfo>();
		for (FileInfo s : queryResult)
			if (s.fileHash.contains(hashSubstr))
				matched.add(s);

		return matched.toArray(new FileInfo[matched.size()]);
	}

	@Override
	public void downloadFileFromSeeds(InetSocketAddress[] seedList, String targetFileHash) {
		Downloader d = new Downloader();
		d.downloadFile(seedList);
		System.out.println("my port: "+port);
	}

	public void listenSeeder() {
		Seeder d = new Seeder(chunkSize);
		port = d.getSeederPort();
		System.out.println("my port: "+port);
		d.start();
	}

}