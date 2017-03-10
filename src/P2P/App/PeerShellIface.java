package P2P.App;

public interface PeerShellIface {

	/**
	 * Gets the current command typed by the user
	 * 
	 * @return The current command using the PeerCommands.java code values
	 */
	byte getCommand();

	/**
	 * Gets the arguments for the current commend
	 * 
	 * @return Arguments as an array of Strings
	 */
	String[] getCommandArguments();

	/**
	 * Interaction and interpretation of the commands typed by the user
	 * 
	 */
	void readCommand();

}