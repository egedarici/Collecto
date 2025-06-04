package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import exceptions.*;
import game.Game;
import game.Move;
import game.Player;
import protocols.ClientProtocol;
import protocols.ProtocolMessages;

public class Client implements ClientProtocol, Runnable {
	
	private String description;
	private ClientTUI tui;
	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	
	private volatile boolean loggedIn = false;
	private String[] serverExtensions;
	
	private String username;
	private Game game;
	private boolean inGame = false;
	private volatile boolean aiMode = false;
	private volatile boolean isAIModeChosen = false;
	
	/**
	 * @requires description != null
	 * @requires socket != null
	 * @requires tui != null
	 * @ensures Constructor for Client. description, tui, socket, reader and writer are initialized
	 * @param description
	 * @param socket
	 * @param tui
	 * @throws IOException
	 */
	public Client(String description, Socket socket, ClientTUI tui) throws IOException {
		this.description = description;
		this.tui = tui;
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	/**
	 * Establishes the connection between sockets.
	 */
	public void run() {
		try {
			sendMessage(ProtocolMessages.HELLO + ProtocolMessages.DELIM + description + 
					ProtocolMessages.DELIM + ProtocolMessages.RANK +  ProtocolMessages.DELIM + ProtocolMessages.CHAT);
			String helloMessage = receiveMessage();
			handleHello(helloMessage);
			
			while (!loggedIn) {
				printTUI("Please enter a username to login.");
				username = receiveTUICommand();
				sendMessage(ProtocolMessages.LOGIN + ProtocolMessages.DELIM + username);
				String loginMessage = receiveMessage();
				loggedIn = handleLogin(loginMessage);
			}
			
			while (true) {
				String message = receiveMessage();
				handleServerMessage(message);
			}
		} catch (IOException e) {
			printTUI("Connection failed.");
		}
		try {
			socket.close();
		} catch (IOException e) {
			printTUI("Socket couldn't be closed.");
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures Handles client if hello command is sent
	 */
	public synchronized void handleHello(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length < 2) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.HELLO)) {
			sendMessage(ProtocolMessages.ERROR);
		} else {
			String[] extensions = new String[params.length - 2];
			for (int i = 0; i < extensions.length; i++) {
				extensions[i] = params[i+2];
			}
			setServerExtensions(extensions);
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures Handles client if login command is sent
	 */
	public synchronized boolean handleLogin(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params[0].equals(ProtocolMessages.ERROR)) {
			printTUI("Login failed due to server error. Enter username again.");
			return false;
		} else if (params.length != 1) {
			printTUI("Login failed due to server error. Enter username again.");
			sendMessage(ProtocolMessages.ERROR);
			return false;
		} else if (params[0].equals(ProtocolMessages.LOGIN)) {
			printTUI("Login successful.");
			return true;
		} else if (params[0].equals(ProtocolMessages.ALREADYLOGGEDIN)) {
			printTUI("Username already exists. Enter another username.");
			return false;
		} else {
			printTUI("Login failed due to server error. Enter username again.");
			sendMessage(ProtocolMessages.ERROR);
			return false;
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures List of player is printed to client's screen.
	 */
	public synchronized void handleList(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length < 2) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.LIST)) {
			sendMessage(ProtocolMessages.ERROR);		
		} else {
			printTUI("List of players: ");
			for (int i = 1; i < params.length; i++) {
				printTUI(params[i]);
			}
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures Starts a new game by setting the board and the players
	 * @ensures Asks client for AI command 
	 */
	public synchronized void handleNewGame(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length != 52) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.NEWGAME)) {
			sendMessage(ProtocolMessages.ERROR);		
		} else {
			try {
				int[] initialBoard = new int[49];
				for (int i = 0; i < initialBoard.length; i++) {
					int ballNum = Integer.parseInt(params[i+1]);
					if (ballNum < 0 || ballNum > 6) {
						sendMessage(ProtocolMessages.ERROR);
						return;
					}
					initialBoard[i] = ballNum;
				}
				
				String player1 = params[50];
				String player2 = params[51];
				if (!username.equals(player1) && !username.equals(player2)) {
					sendMessage(ProtocolMessages.ERROR);
					return;
				}
				if (username.equals(player1) && username.equals(player2)) {
					sendMessage(ProtocolMessages.ERROR);
					return;
				}
				
				game = new Game(player1, player2, initialBoard);
				inGame = true;
				isAIModeChosen = false;
				
				printTUI("New game started. First player: " + player1 + " Second player: " + player2);
				printTUI("Please give an AI command to choose an AI strategy or play yourself. (type help to see possible commands)");
				printTUI(game.getBoard().toString());
			} catch (NumberFormatException e) {
				sendMessage(ProtocolMessages.ERROR);
			}
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures Handles the moves of the user considering if it's an AI or not
	 */
	public synchronized void handleMove(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (!(params.length == 2 || params.length == 3)) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.MOVE)) {
			sendMessage(ProtocolMessages.ERROR);		
		} else {
			try {
				int move1 = Integer.parseInt(params[1]);
				int move2 = -1;
				if (params.length == 3) move2 = Integer.parseInt(params[2]);
				Move move = new Move(move1, move2);
				game.makeMove(game.getOpponent(username), move);
				
				printTUI("Opponent made this move: " + move.toStringReadable());
				printTUI(game.getBoard().toString());
				
				if (isAIModeChosen && aiMode) {
					try {
						Move aiMove = game.getPlayer(username).getHint(game.getBoard());
						if (aiMove != null) {
							game.makeMove(username, aiMove);
							sendMessage(ProtocolMessages.MOVE + aiMove);
							
							printTUI("AI made this move: " + aiMove.toStringReadable());
							printTUI(game.getBoard().toString());
						}
					} catch (IllegalMoveException e) {
						printTUI("Error");
					} catch (WrongTurnException e) {
						printTUI("Error");
					} catch (PlayerNotFoundException e) {
						printTUI("Error");
					}
				}
			} catch (NumberFormatException e) {
				sendMessage(ProtocolMessages.ERROR);
			} catch (IllegalMoveException e) {
				sendMessage(ProtocolMessages.ERROR);
			} catch (WrongTurnException e) {
				sendMessage(ProtocolMessages.ERROR);
			} catch (PlayerNotFoundException e) {
				printTUI("Error");
			}
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures Ends the game and prints the result
	 */
	public synchronized void handleGameOver(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (!(params.length == 2 || params.length == 3)) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.GAMEOVER)) {
			sendMessage(ProtocolMessages.ERROR);		
		} else {
			String reason = params[1];
			if (params.length == 2) {
				if (!reason.equals(ProtocolMessages.DRAW)) {
					sendMessage(ProtocolMessages.ERROR);
				} else {
					inGame = false;
					isAIModeChosen = false;
					printTUI("Game ended with a draw.");
				}
			} else {
				if (reason.equals(ProtocolMessages.VICTORY)) {
					inGame = false;
					isAIModeChosen = false;
					printTUI("Game over. Winner is " + params[2]);
				} else if (reason.equals(ProtocolMessages.DISCONNECT)) {
					inGame = false;
					isAIModeChosen = false;
					printTUI("Game ended because of a connection error. Winner is " + params[2]);
				}
			}
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures Ranking list is printed
	 */
	public synchronized void handleRank(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length < 1 || params.length % 2 == 0) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.RANK)) {
			sendMessage(ProtocolMessages.ERROR);		
		} else {
			try {
				String rankings = "Rankings: \n";
				for (int i = 1; i < params.length; i += 2) {
					rankings += params[i] + ": " + Integer.parseInt(params[i+1]) + "\n";
				}
				printTUI(rankings);
			} catch (NumberFormatException e) {
				sendMessage(ProtocolMessages.ERROR);
			}
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures A message is sent to the global chat from the user
	 */
	public synchronized void handleChat(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM, 3);
		if (params.length != 3) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.CHAT)) {
			sendMessage(ProtocolMessages.ERROR);		
		} else {
			printTUI(params[1] + ": " + params[2]);
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures A message is sent privately to a user
	 */
	public synchronized void handleWhisper(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM, 3);
		if (params.length != 3) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.WHISPER)) {
			sendMessage(ProtocolMessages.ERROR);		
		} else {
			printTUI(params[1] + " whispers: " + params[2]);
		}
	}
	
	/**
	 * 
	 * @requires message != null
	 * @ensures Handles when a user cannot get private message
	 * @throws IOException
	 */
	public synchronized void handleCannotWhisper(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length != 2) {
			sendMessage(ProtocolMessages.ERROR);
		} else if (!params[0].equals(ProtocolMessages.CANNOTWHISPER)) {
			sendMessage(ProtocolMessages.ERROR);		
		} else {
			printTUI(params[1] + " cannot receive your whisper.");
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures The messages are handled between the client and the server
	 * @param message
	 * @throws IOException
	 */
	public void handleServerMessage(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM, 2);
		String command = params[0];
		switch (command) {
		case ProtocolMessages.LIST:
			handleList(message);
			break;
		case ProtocolMessages.NEWGAME:
			handleNewGame(message);
			break;
		case ProtocolMessages.MOVE:
			handleMove(message);
			break;
		case ProtocolMessages.GAMEOVER:
			handleGameOver(message);
			break;
		case ProtocolMessages.RANK:
			handleRank(message);
			break;
		case ProtocolMessages.CHAT:
			handleChat(message);
			break;
		case ProtocolMessages.WHISPER:
			handleWhisper(message);
			break;
		case ProtocolMessages.CANNOTWHISPER:
			handleCannotWhisper(message);
			break;
		case ProtocolMessages.ERROR:
			printTUI("Server error occured.");
			break;
		}
	}
	
	/**
	 * @requires serverExtensions != null
	 * @ensures serverExtensions are initialized
	 * @param serverExtensions
	 */
	public void setServerExtensions(String[] serverExtensions) {
		this.serverExtensions = serverExtensions;
	}
	
	/**
	 * @requires extension != null
	 * @ensures Checks if a server contains a specific extension
	 * @param extension
	 * @return true if contains, false otherwise
	 */
	public boolean serverContainsExtension(String extension) {
		for (int i = 0; i < serverExtensions.length; i++) {
			if (serverExtensions[i].equals(extension)) return true;
		}
		return false;
	}
	
	/**
	 * @ensures if user logged in or not
	 * @return true if logged in, false otherwise
	 */
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	/**
	 * @requires message != null
	 * @ensures sends message to server
	 * @param message
	 * @throws IOException
	 */
	public synchronized void sendMessage(String message) throws IOException {
		if (message == null) return;
		writer.write(message + "\n");
		writer.flush();
	}
	
	/**
	 * @ensures receives and reads messages from server
	 * @return message
	 * @throws IOException
	 */
	public String receiveMessage() throws IOException {
		String message = reader.readLine();
		if (message == null) throw new IOException();
		return message;
	}
	
	/**
	 * @requires message != null
	 * @ensures prints the message to screen
	 * @param message
	 */
	public void printTUI(String message) {
		tui.print(message);
	}
	
	/**
	 * @ensures receives message from screen
	 * @return
	 */
	public String receiveTUICommand() {
		return tui.scan();
	}
	
	/**
	 * @ensures Handles when List command is sent.
	 * @throws IOException
	 */
	public synchronized void handleListCommand() throws IOException  {
		sendMessage(ProtocolMessages.LIST);
	}
	
	/**
	 * @ensures Handles when Queue command is sent.
	 * @throws IOException
	 */
	public synchronized void handleQueueCommand() throws IOException  {
		if (inGame) {
			printTUI("Already in game.");
		} else {
			sendMessage(ProtocolMessages.QUEUE);	
		}
	}
	 
	/**
	 * @requires line != null
	 * @ensures Handles the moves of user considering if it's an AI or not
	 * @param line
	 * @throws IOException
	 */
	public synchronized void handleMoveCommand(String line) throws IOException  {
		String[] params = line.split(" ");
		if (!inGame || game == null) {
			printTUI("You are not in a game.");
		} else if (!isAIModeChosen) {
			printTUI("Enter an AI command to choose a strategy or play yourself.");
		} else if (aiMode) {
			printTUI("Computer is playing.");
		} else if (!(params.length == 2 || params.length == 3)) {
			printTUI("Please enter a valid move.");	
		} else {
			try {
				int move1 = Integer.parseInt(params[1]);
				int move2 = -1;
				if (params.length == 3) move2 = Integer.parseInt(params[2]);
				Move move = new Move(move1, move2);
				game.makeMove(username, move);
				sendMessage(ProtocolMessages.MOVE + move);
			} catch (NumberFormatException e) {
				printTUI("Please enter a valid move.");
			} catch (PlayerNotFoundException e) {
				printTUI("You are not in a game.");
			} catch (WrongTurnException e) {
				printTUI("It's your opponent's turn.");
			} catch (IllegalMoveException e) {
				printTUI("Please enter a valid move.");
			}
		}
	}
	
	/**
	 * @requires line != null
	 * @ensures Handles the given AI command by the user and notifies user 
	 * @param line
	 * @throws IOException
	 */
	public synchronized void handleAICommand(String line) throws IOException {
		String[] params = line.split(" ");
		if (!inGame || game == null) {
			printTUI("You are not in a game yet.");
		} else if (isAIModeChosen) {
			printTUI("AI command already given for this game.");
		} else if (!(params.length == 2)) {
			printTUI("Please enter a valid AI command.");	
		} else if (params[1].equals("none")) {
			isAIModeChosen = true;
			aiMode = false;
		} else {
			try {
				game.setStrategy(username, params[1]);
				
				if (game.isPlayersTurn(username)) {
					Move move = game.getPlayer(username).getHint(game.getBoard());
					if (move != null) {
						game.makeMove(username, move);
						sendMessage(ProtocolMessages.MOVE + move);
						
						printTUI("AI made this move: " + move.toStringReadable());
						printTUI(game.getBoard().toString());
					}
				}
		
				isAIModeChosen = true;
				aiMode = true;
			} catch (StrategyNotFoundException e) {
				printTUI("Please enter a valid AI command.");
			} catch (PlayerNotFoundException e) {
				printTUI("You are not in a game.");
			} catch (IllegalMoveException e) {
				printTUI("Please enter another AI command.");
			} catch (WrongTurnException e) {
				printTUI("Please enter another AI command.");
			}
		}
	}
	
	/**
	 * @ensures Handles hint command.
	 * @throws IOException
	 */
	public synchronized void handleHintCommand() throws IOException  {
		if (!inGame || game == null) {
			printTUI("You are not in a game.");
		} else if (!isAIModeChosen) {
			printTUI("Enter an AI command to choose a strategy or play yourself.");
		} else if (aiMode) {
			printTUI("Computer is playing.");
		} else {
			Player p = game.getPlayer(username);
			if (p == null) {
				printTUI("You are not in a game.");
			} else {
				Move hintMove = p.getHint(game.getBoard());
				printTUI("Try this move: " + hintMove.toStringReadable());
			}
		}
	}
	
	/**
	 * @ensures Handles rank command if rank extension is supported by the server.
	 * @throws IOException
	 */
	public synchronized void handleRankCommand() throws IOException  {
		if (serverContainsExtension(ProtocolMessages.RANK)) {
			sendMessage(ProtocolMessages.RANK);
		} else {
			printTUI("Rank is not supported by the server.");
		}
	}
	
	/**
	 * @ensures Handles chat command if chat extension is supported by the server. 
	 * @param line
	 * @throws IOException
	 */
	public synchronized void handleChatCommand(String line) throws IOException  {
		String[] params = line.split(" ", 2);
		if (!serverContainsExtension(ProtocolMessages.CHAT)) {
			printTUI("Chat is not supported by the server.");
		} else if (params.length != 2) {
			printTUI("Please enter a message to send");
		} else {
			sendMessage(ProtocolMessages.CHAT + ProtocolMessages.DELIM + params[1]);
		}
	}
	
	/**
	 * @ensures Handles whisper command if chat extension is supported by the server. 
	 * @param line
	 * @throws IOException
	 */
	public synchronized void handleWhisperCommand(String line) throws IOException  {
		String[] params = line.split(" ", 3);
		if (!serverContainsExtension(ProtocolMessages.CHAT)) {
			printTUI("Chat is not supported by the server.");
		} else if (params.length != 3) {
			printTUI("Please enter a recipient and a message to whisper");
		} else {
			sendMessage(ProtocolMessages.WHISPER + ProtocolMessages.DELIM + params[1] + ProtocolMessages.DELIM + params[2]);
		}
	}
}
