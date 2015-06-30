package messages;

public class JSONCodes {
	public static final String 	role 				= "ROLE",
									master 				= "MASTER",
									guesser				= "GUESSER",
								
								roomName			= "ROOMNAME",
								numberOfGuessers	= "NUMBER_OF_GUESSERS",
								
								// CONFIGURATION INFO
								serverIP			= "SERVER_IP",
								registryName		= "REGISTRY_NAME",
								registryPort		= "REGISTRY_PORT",
								serverPort			= "SERVER_PORT",
								maxNumberOfGames	= "MAX_NUM_GAMES",
								baseMulticastAddr	= "BASE_MULT_ADDR",
								maxMulticastAddr	= "MAX_MULT_ADDR",
								
								// GAME CREATION
								// TODO CHECK CODES
								connectionClosed 	= "SOCKET_CLOSED",
								waitingRoomsFull	= "WAITINGROOMS_FULL",
								newRoomCreated		= "NEW_ROOM_CREATED",
								roomJoined			= "ROOM_JOINED",
								guesserJoinError	= "GUESSER_ERROR",
								roomClosed			= "ROOM_CLOSED",
								message				= "MSG",
								userName			= "USERNAME",
								gameStarting		= "GAME_STARTING",
								roomMulticast		= "ROOM_MULTICAST_ADDR",
								roomPassword		= "ROOM_PASSWORD",
								cleanupCompleted	= "CLEANUP_COMPLETED",
								timeoutLength		= "TIMEOUT_LEN",
								
								// GAME MANAGEMENT
								guesserTimeout		= "GUESSER_TIMEOUT",

								ack					= "ACK",
								word				= "WORD",
								guess				= "GUESS",
								senderNick			= "SENDER_NICK",
								replyTo				= "REPLY_TO",
								
								gameStatus			= "GAME_STATUS",
									correctGuess		= "CORRECT_GUESS",
									wrongGuess			= "WRONG_GUESS",
									repeatedGuess		= "REPEATED_GUESS",
									masterLost			= "MASTER_LOST",
									masterWon			= "MASTER_WON",
									masterLeft			= "MASTER_LEFT";
								
	
}
