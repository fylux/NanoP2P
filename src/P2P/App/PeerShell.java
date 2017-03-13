package P2P.App;

import java.util.Scanner;

public class PeerShell implements PeerShellIface {
	
	private String line;
	
	private Scanner in;
	
	public PeerShell() {
		in = new Scanner(System.in);
	}
	
	/*public void close() {
		in.close();
	}*/
	
	@Override
	public byte getCommand() {
		if (line.equals("query")) {
			return PeerCommands.COM_QUERY;
		}
		else if (line.equals("quit"))
			return PeerCommands.COM_QUIT;
		else {
			return 0;
		}
	}

	@Override
	public String[] getCommandArguments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readCommand() {
		line = in.nextLine();
	}

}
