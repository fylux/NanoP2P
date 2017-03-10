package P2P.App;

import java.net.InetSocketAddress;

import P2P.PeerTracker.Message.Message;
import P2P.util.FileInfo;

public class PeerController implements PeerControllerIface {
	/**
	 * The shell associated to this controller.
	 */
	private PeerShellIface shell;

	private byte currentCommand;

    public PeerController() {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processCurrentCommand() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Message createMessageFromCurrentCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processMessageFromTracker(Message response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recordQueryResult(FileInfo[] fileList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void printQueryResult() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FileInfo[] lookupQueryResult(String hashSubstr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void downloadFileFromSeeds(InetSocketAddress[] seedList,
			String targetFileHash) {
		// TODO Auto-generated method stub
		
	}

}