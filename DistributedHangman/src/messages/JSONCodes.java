package messages;

public class JSONCodes {
	public static final String 	
								
								
								
								// CONFIGURATION INFO
								serverIP			= "SERVER_IP",
								registryName		= "REGISTRY_NAME",
								registryPort		= "REGISTRY_PORT",
								masterServerPort	= "MASTER_SERVER_PORT",
								guesserServerPort	= "GUESSER_SERVER_PORT",
								maxNumberOfGames	= "MAX_NUM_GAMES",
								baseMulticastAddr	= "BASE_MULT_ADDR",
								maxMulticastAddr	= "MAX_MULT_ADDR",
								timeoutLength		= "TIMEOUT_LEN",
								
								// GAME CREATION
								roomName			= "ROOMNAME", // used by the master to create a waiting room
								numberOfGuessers	= "NUMBER_OF_GUESSERS",
								
								message				= "MSG", // field that regulates the communication between client/server during the game creation
									connectionClosed 	= "SOCKET_CLOSED",	// server timeout expired
									guesserJoinError	= "GUESSER_ERROR",	// room was full or name was wrong
									roomJoined			= "ROOM_JOINED",	// notify having joined the room
									roomClosed			= "ROOM_CLOSED",	// master left
									gameStarting		= "GAME_STARTING",
										roomMulticast		= "ROOM_MULTICAST_ADDR",
										roomPassword		= "ROOM_PASSWORD",
								
								
								// GAME MANAGEMENT
								guesserTimeout		= "GUESSER_TIMEOUT",
								ack					= "ACK",
								word				= "WORD",
								guess				= "GUESS",
								previousGuesses		= "PREVIOUS_GUESSES",
								senderNick			= "SENDER_NICK",
								replyTo				= "REPLY_TO",
								guesserLeft         = "GUESSER_LEFT", // boolean field
								
								role 				= "ROLE",
									master 				= "MASTER",
									guesser				= "GUESSER",
									
								wordHint 			= "WORD_HINT", 	// fields used in the initialization message
								attempts			= "ATTEMPTS",	// fields used in the initialization message
								
								gameStatus			= "GAME_STATUS",
									correctGuess		= "CORRECT_GUESS",
									wrongGuess			= "WRONG_GUESS",
									repeatedGuess		= "REPEATED_GUESS",
									masterLost			= "MASTER_LOST",
									masterWon			= "MASTER_WON",
									initialization		= "INITIALIZATION",
									masterLeft			= "MASTER_LEFT";
								
	
}
