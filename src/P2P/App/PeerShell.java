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
			String[] words;
			do {
				System.out.print("> ");
				words = in.nextLine().split(" ");
			} while (words[0].isEmpty()); //Avoid empty input

			command = PeerCommands.stringToCommand(words[0]);

			if (words.length > 1)
				args = Arrays.copyOfRange(words, 1, words.length);

		} while (!analyzeLine());
	}

	private boolean analyzeLine() {
		switch (command) {
		case PeerCommands.COM_INVALID: {
			PeerCommands.printCommandsHelp();
			return false;
		}
		case PeerCommands.COM_DOWNLOAD: {
			// Show rules
			return args != null && args.length == 1;
		}

		case PeerCommands.COM_QUERY: {
			if (args != null && !analizeArgs()) {
				PeerCommands.printQueryOptionsHelp();
				return false;
			} else
				return true;
		}
		default:
			return true;
		}
	}

	private String argsToString() {
		String s = "";
		for (String i : args) {
			s += i + " ";
		}
		s = s.substring(0, s.length() - 1);
		return s;
	}

	private boolean analizeArgs() {
		String[] op = argsToString().split("-");

		int ini = 0;
		if (op[0].equals(""))
			ini = 1;

		for (int i = ini; i < op.length; i++) {
			if (!op[i].matches("(?i)(n ([^ ])+ ?)") && !op[i].matches("(lt|ge) [0-9]+ ([KMG])?B ?"))
				return false;
		}
		return true;
	}
}