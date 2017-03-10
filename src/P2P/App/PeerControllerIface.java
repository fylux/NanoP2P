package P2P.App;

import java.net.InetSocketAddress;

import P2P.PeerTracker.Message.Message;
import P2P.util.FileInfo;

public interface PeerControllerIface {
	/**
	 * Gets the current command assigned to this controller.
	 * 
	 * @return The current command assigned to this controller
	 */
	public byte getCurrentCommand();

	/**
	 * Sets the current command to be processed by this controller.
	 */
	public void setCurrentCommand(byte command);

	/**
	 * Sets the current command's arguments, as returned by the shell.
	 * 
	 * @param args
	 *            The validated arguments given by the user.
	 */
	public void setCurrentCommandArguments(String[] args);

	/**
	 * Process the current command with its arguments, if any. Note that
	 * setCurrentCommand and setCurrentCommandArguments must have been called
	 * before the command can be processed. This method must create the
	 * appropriate message, send it to the tracker, receive the corresponding
	 * response(s), and process the response to perform any actions required
	 * to complete the command. 
	 */
	public void processCurrentCommand();

	/***
	 * Creates the appropriate type of request message depending on the current
	 * command.
	 * 
	 * @return The request message created, null for commands that don't need
	 *         communication with tracker
	 */
	public Message createMessageFromCurrentCommand();

	/***
	 * Process response messages from tracker, performing the appropriate
	 * actions according to the type of the message.
	 * 
	 * @param response
	 *            The response message to be processed
	 */
	public void processMessageFromTracker(Message response);

	/***
	 * Save the list of files sent by the tracker in response to a query
	 * request, excluding those files that are already shared by this peer. The
	 * result of each query is used for selecting the file to download.
	 * 
	 * @param fileList
	 *            The full list of files returned by the tracker
	 */

	public void recordQueryResult(FileInfo[] fileList);

	/**
	 * Prints the list of files obtained from last query (previously saved by
	 * recordQueryResult) which are available for download
	 */
	public void printQueryResult();

	/**
	 * Looks up the recorded file list from last query, searching for files
	 * whose hash contain the given substring. Used for searching the file to
	 * download amongst the files returned by last query.
	 * 
	 * @param hashSubstr
	 *            String given by the user to the 'download' command.
	 * @return A list of the files whose hash contain the substring.
	 */
	public FileInfo[] lookupQueryResult(String hashSubstr);

	/***
	 * Download file from the list of seeds provided, by creating a downloader
	 * object for this target file identified by its hash.
	 * 
	 * @param inetSocketAddresses
	 *            The list of peers that currently share the file.
	 * @param targetFileHash
	 *            The target file to download
	 */
	public void downloadFileFromSeeds(InetSocketAddress[] seedList,
			String targetFileHash);

	/**
	 * Sets COM_ADDSEED as current command and process it, in order
	 * to send all shared files to the tracker.
	 */
	public void publishSharedFilesToTracker();

	/**
	 * Sets COM_CONFIG as current command and process it, in order
	 * to obtain the configuration (chunk size) from the tracker.
	 */
	public void getConfigFromTracker();

}
