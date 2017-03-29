package P2P.App;

import java.util.Arrays;
import java.util.Scanner;

public class PeerShell implements PeerShellIface {
	
	private byte command;
	private String[] args;
	
	private Scanner in;
	
	public PeerShell() {
		in = new Scanner(System.in);
	}
	
	public void close() {
		in.close();
	}
	
	@Override
	public byte getCommand() {
		return command;
	}
	

	@Override
	public String[] getCommandArguments() {
		return args;
	}

	@Override
	public void readCommand() {
		do {
		args = null;
		String[] words = in.nextLine().split(" ");
		command = PeerCommands.stringToCommand(words[0]);

		if (words.length > 1)
			args = Arrays.copyOfRange(words,1, words.length);
		
		} while(!analyzeLine());
		
	}

	private boolean analyzeLine() {
		switch(command) {
			case PeerCommands.COM_INVALID : {
				PeerCommands.printCommandsHelp();
				return false;
			}
			case PeerCommands.COM_DOWNLOAD : {
				//Show rules
				return args != null && args.length == 1;
			}
			
			default: return true;
		}
	}

}
