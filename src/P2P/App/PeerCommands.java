package P2P.App;

import P2P.PeerTracker.Message.MessageQuery;

public class PeerCommands {
	/**
	 * Codes for all commands supported by this shell.
	 */
	public static final byte COM_INVALID = 0;
	public static final byte COM_CONFIG = 1;
	public static final byte COM_ADDSEED = 2;
	public static final byte COM_QUERY = 3;
	public static final byte COM_DOWNLOAD = 4;
	public static final byte COM_QUIT = 5;
	public static final byte COM_SHOW = 6;
	public static final byte COM_HELP = 7;

	/**
	 * The list of valid commands that can be entered
	 * by the user of this shell, with their corresponding
	 * string and help message. Note the matching order!
	 */
	private static final Byte[] _valid_user_commands = { 
		COM_QUERY, 
		COM_DOWNLOAD,
		COM_QUIT,
		COM_SHOW, 
		COM_HELP
		};

	/**
	 * Accepted string for each command
	 */
	private static final String[] _valid_user_commands_str = {
		"query",
		"download",
		"quit",
		"list",
		"help" };

	/**
	 * Help message for each command
	 */
	private static final String[] _valid_user_commands_help = {
		"query list of available files in tracker",
		"download files identified by <hash> list",
		"disconnect from tracker and exit client ",
		"list files shared by this peer", 
		"show list of valid commands" };

	/**
	 * Translates a string to its corresponding command.
	 * @param str The string entered by the user of this shell.
	 * @return The corresponding command code (COM_xxx), or
	 *         COM_INVALID if not a valid keyword
	 *         any valid user command in _valid_user_commands_str.
	 */
	public static byte stringToCommand(String comStr) {
		for (int i = 0;
		i < _valid_user_commands_str.length; i++) {
			if (_valid_user_commands_str[i].equalsIgnoreCase(comStr)) {
				return _valid_user_commands[i];
			}
		}
		return COM_INVALID;
	}

	/**
	 * Prints the list of valid commands and brief help message for each command.
	 */
	public static void printCommandsHelp() {
		System.out.println("List of commands:");
		for (int i = 0; i < _valid_user_commands_str.length; i++) {
			System.out.println(_valid_user_commands_str[i] + " -- "
					+ _valid_user_commands_help[i]);
		}		
	}

	/**
	 * Codes for all query filter types supported by this shell.
	 */
	private static final Byte[] _valid_query_filter_types = { 
		MessageQuery.FILTERTYPE_NAME, 
		MessageQuery.FILTERTYPE_MAXSIZE,
		MessageQuery.FILTERTYPE_MINSIZE };

	/**
	 * The strings corresponding to each filter type. 
	 * Pay attention to order, must match _valid_query_filter_types
	 */
	private static final String[] _valid_query_options_str = { 
		"-n", 
		"-lt",
		"-ge" 
		};

	/**
	 * Converts a query filter option into its corresponding code.
	 * @param queryFilterTypeStr The option passed to the 'query' command
	 * @return The code represented by that option, or FILTERTYPE_INVALID
	 *         if the option does not correspond to any valid filter.
	 */
	public static byte queryFilterOptionToFilterType(String queryFilterTypeStr) {
		for (int i = 0; // 0: connect
		i < _valid_query_options_str.length; i++) {
			if (_valid_query_options_str[i].equals(queryFilterTypeStr)) {
				return _valid_query_filter_types[i];
			}
		}
		return MessageQuery.FILTERTYPE_INVALID;
	}


	private static final String[] _valid_query_options_help = {
		"query files whose file name contains substring",
		"query files whose file size is less than given size",
		"query files whose file size is greater or equal to given size",
		};
	
	public static void printQueryOptionsHelp() {
		System.out.println("List of query options:");
		for (int i = 0; i < _valid_query_options_str.length; i++) {
			System.out.println(_valid_query_options_str[i] + " -- "
					+ _valid_query_options_help[i]);
		}
	}

}
