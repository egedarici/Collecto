package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import exceptions.*;
import game.Game;
import game.Move;
import protocols.ProtocolMessages;
import protocols.ServerProtocol;

public class Server implements ServerProtocol, Runnable {
	
	private int port;
	private String serverDescription;
	private ServerTUI tui;
	
	/**
	 * Holds the active handlers connected to the server
	 */
	private Map<String, ClientHandler> activeHandlers = new HashMap<String, ClientHandler>();
	
	/**
	 * Holds the active games
	 */
	private Map<String, Game> activeGames = new HashMap<String, Game>();
	
	/**
	 * Holds the user in queue
	 */
	private String waitingUser;
	
	/**
	 * Holds the wins of players
	 */
	private Map<String, Integer> wins = new HashMap<String, Integer>();
	
	/**
	 * @requires port != 0
	 * @requires description != null
	 * @requires tui != null
	 * @ensures Constructor for Server
	 * @param port
	 * @param description
	 * @param tui
	 */
	public Server(int port, String description, ServerTUI tui) {
		this.port = port;
		this.serverDescription = description;
		this.tui = tui;
	}
	
	/**
	 * Accepts the clientHandler socket to the server
	 */
	public void run() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			printTUI(serverDescription + ": Server socket couldn't be created.");
		}
		
		while (ss != null) {
			try {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket, this);
				new Thread(handler).start();
			} catch (IOException e) {
				printTUI(serverDescription + ": Server thread shutting down...");
			}
		}
	}
	
	/**
	 * @requires ch != null
	 * @requires message != null
	 * @ensures handles the Hello message from the clientHandler
	 */
	public synchronized String handleHello(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length < 2) return ProtocolMessages.ERROR;
		if (!params[0].equals(ProtocolMessages.HELLO)) return ProtocolMessages.ERROR;
		
		ch.setDescription(params[1]);
		String[] extensions = new String[params.length - 2];
		for (int i = 0; i < extensions.length; i++) {
			extensions[i] = params[i+2];
		}
		ch.setExtentions(extensions);
		return ProtocolMessages.HELLO + ProtocolMessages.DELIM + serverDescription + ProtocolMessages.DELIM +
				ProtocolMessages.RANK + ProtocolMessages.DELIM + ProtocolMessages.CHAT;
	}
	
	/**
	 * @requires ch != null
	 * @requires message != null
	 * @ensures handles the Login message from the clientHandler
	 * @ensures logging the client in by checking if they're already logged in or not
	 */
	public synchronized String handleLogin(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM, 2);
		if (params.length < 2) {
			return ProtocolMessages.ERROR;
		}
		if (!params[0].equals(ProtocolMessages.LOGIN)) return ProtocolMessages.ERROR;
		
		if (params[1].contains(ProtocolMessages.DELIM)) {
			return ProtocolMessages.ERROR;
		}
		if (activeHandlers.containsKey(params[1])) return ProtocolMessages.ALREADYLOGGEDIN;
		activeHandlers.put(params[1], ch);
		ch.setUsername(params[1]);
		
		if (!wins.containsKey(params[1])) {
			wins.put(params[1], 0);
		}
		
		return ProtocolMessages.LOGIN;
	}
	
	/**
	 * @requires ch != null
	 * @requires message != null
	 * @ensures handles the List message from the clientHandler
	 * @returns List of active players
	 */
	public synchronized String handleList(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length != 1) return ProtocolMessages.ERROR;
		if (!params[0].equals(ProtocolMessages.LIST)) return ProtocolMessages.ERROR;
		
		String response = ProtocolMessages.LIST;
		for (String username : activeHandlers.keySet()) {
			response += ProtocolMessages.DELIM + username;
		}
		return response;
	}
	
	/**
	 * @requires ch != null
	 * @requires message != null
	 * @ensures Puts the player in the queue and start a newgame
	 * @ensures returns null or newgame. If this returns a newgame, also notify the opponent with a new game
	 * @return null or newgame
	 * @throws IOException 
	 */
	public synchronized String handleQueue(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length != 1) return ProtocolMessages.ERROR;
		if (!params[0].equals(ProtocolMessages.QUEUE)) return ProtocolMessages.ERROR;
		
		if (activeGames.containsKey(ch.getUsername())) {
			return ProtocolMessages.ERROR;
		} else if (waitingUser == null) {
			waitingUser = ch.getUsername();
			return null;
		} else if (waitingUser.equals(ch.getUsername())) {
			waitingUser = null;
			return null;
		} else {
			String player1 = waitingUser;
			String player2 = ch.getUsername();
			Game game = new Game(player1, player2);
			activeGames.put(player1, game);
			activeGames.put(player2, game);
			waitingUser = null;
			
			String response = ProtocolMessages.NEWGAME;
			int[] board = game.getBoardArray();
			for (int i = 0; i < board.length; i++) {
				response += ProtocolMessages.DELIM + board[i];
			}
			response += ProtocolMessages.DELIM + player1 + ProtocolMessages.DELIM + player2;
			
			ClientHandler ch1 = activeHandlers.get(player1);
			try {
				ch1.sendMessage(response);
				return response;
			} catch (IOException e) {
				ch1.deactivateThread();
				activeGames.remove(player1);
				activeGames.remove(player2);
				waitingUser = player2;
				return null;
			}
		}
	}
	
	/**
	 *  @requires ch != null 
	 *  @requires message != null
	 *  @ensures returns null and notify the opponent with a move message. 
	 *  @ensures can also return gameover and notify the opponent with a game over.
	 *  @ensures Moves of the players are handled till the game ends.
	 *  @return null or gameover
	 */
	public synchronized String handleMove(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (!(params.length == 2 || params.length == 3)) return ProtocolMessages.ERROR;
		if (!params[0].equals(ProtocolMessages.MOVE)) return ProtocolMessages.ERROR;
		
		String player = ch.getUsername();
		if (!activeGames.containsKey(player)) return ProtocolMessages.ERROR;
		
		try {
			int move1 = Integer.parseInt(params[1]);
			int move2 = -1;
			if (params.length == 3) move2 = Integer.parseInt(params[2]);
			Move move = new Move(move1, move2);
			Game game = activeGames.get(player);
			game.makeMove(player, move);
		
			String opponent = game.getOpponent(player);
			ClientHandler ch2 = activeHandlers.get(opponent);
			try {
				ch2.sendMessage(message);
			} catch (IOException e) {
				ch2.deactivateThread();
			}
			
			if (game.getBoard().gameOver()) {
				int winnerNum = game.getWinner();
				String gameOverResponse = ProtocolMessages.GAMEOVER + ProtocolMessages.DELIM;
				activeGames.remove(player);
				activeGames.remove(opponent);
				if (winnerNum == 1) {
					String winner = game.getPlayer1().getUsername();
					gameOverResponse += ProtocolMessages.VICTORY + ProtocolMessages.DELIM + winner;
					wins.put(winner, wins.get(winner) + 1);
				} else if (winnerNum == 2) {
					String winner = game.getPlayer2().getUsername();
					gameOverResponse += ProtocolMessages.VICTORY + ProtocolMessages.DELIM + winner;
					wins.put(winner, wins.get(winner) + 1);
				} else {
					gameOverResponse += ProtocolMessages.DRAW;
				}
				try {
					ch.sendMessage(gameOverResponse);
				} catch (IOException e) {
					ch.deactivateThread();
				}
				try {
					ch2.sendMessage(gameOverResponse);
				} catch (IOException e) {
					ch2.deactivateThread();
				}
			}
		} catch (NumberFormatException e) {
			return ProtocolMessages.ERROR;
		} catch (IllegalMoveException e) {
			return ProtocolMessages.ERROR;
		} catch (PlayerNotFoundException e) {
			return ProtocolMessages.ERROR;
		} catch (WrongTurnException e) {
			return ProtocolMessages.ERROR;
		} 
		return null;
	}
	
	/**
	 * @requires ch != null 
	 * @requires message != null
	 * @ensures Lists the rankings of the players
	 */
	public synchronized String handleRank(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length != 1) return ProtocolMessages.ERROR;
		if (!params[0].equals(ProtocolMessages.RANK)) return ProtocolMessages.ERROR;
		if (!ch.containsExtension(ProtocolMessages.RANK)) return ProtocolMessages.ERROR;
		
		String response = ProtocolMessages.RANK;
		for (String username : wins.keySet()) {
			response += ProtocolMessages.DELIM + username + ProtocolMessages.DELIM + wins.get(username);
		}
		return response;
	}
	
	/**
	 * @requires ch != null 
	 * @requires message != null
	 * @ensures a global chat in the game
	 * @return null 
	 */
	public synchronized String handleChat(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM, 2);
		if (params.length != 2) return ProtocolMessages.ERROR;
		if (!params[0].equals(ProtocolMessages.CHAT)) return ProtocolMessages.ERROR;
		if (!ch.containsExtension(ProtocolMessages.CHAT)) return ProtocolMessages.ERROR;
		
		String response = ProtocolMessages.CHAT + ProtocolMessages.DELIM + ch.getUsername() + ProtocolMessages.DELIM + params[1];
		for (String username : activeHandlers.keySet()) {
			ClientHandler ch2 = activeHandlers.get(username);
			if (!username.equals(ch.getUsername()) && ch2.containsExtension(ProtocolMessages.CHAT)) {
				try {
					ch2.sendMessage(response);
				} catch (IOException e) {
					ch2.deactivateThread();
				}
			}
		}
		return null;
	}
	
	/**
	 * @requires ch != null 
	 * @requires message != null
	 * @ensures a private chat between players
	 * @return null and notify the recipients with a message
	 */
	public synchronized String handleWhisper(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM, 3);
		if (params.length != 3) return ProtocolMessages.ERROR;
		if (!params[0].equals(ProtocolMessages.WHISPER)) return ProtocolMessages.ERROR;
		if (!ch.containsExtension(ProtocolMessages.CHAT)) return ProtocolMessages.ERROR;
		
		String response = ProtocolMessages.WHISPER + ProtocolMessages.DELIM + ch.getUsername() + ProtocolMessages.DELIM + params[2];
		String recipient = params[1];
		if (!activeHandlers.containsKey(recipient)) {
			return ProtocolMessages.ERROR;
		} else {
			ClientHandler ch2 = activeHandlers.get(recipient);
			if (!ch2.containsExtension(ProtocolMessages.CHAT)) {
				return ProtocolMessages.CANNOTWHISPER + ProtocolMessages.DELIM + recipient;
			} else {
				try {
					ch2.sendMessage(response);
				} catch (IOException e) {
					ch2.deactivateThread();
				}
			}
		}
		return null;
	}
	
	/**
	 * @requires ch != null 
	 * @ensures a client is removed from the server 
	 * @ensures opponent is notified with a game over
	 * @param ch
	 */
	public synchronized void removeClient(ClientHandler ch) {
		String username = ch.getUsername();
		activeHandlers.remove(username);
		if (activeGames.containsKey(username)) {
			String opponent = activeGames.get(username).getOpponent(username);
			activeGames.remove(username);
			
			if (opponent != null && activeGames.containsKey(opponent)) {
				activeGames.remove(opponent);
				
				ClientHandler ch2 = activeHandlers.get(opponent);
				String response = ProtocolMessages.GAMEOVER + ProtocolMessages.DELIM + ProtocolMessages.DISCONNECT + ProtocolMessages.DELIM + opponent;
				try {
					ch2.sendMessage(response);
					wins.put(opponent, wins.get(opponent) + 1);
				} catch (IOException e) {
					ch2.deactivateThread();
				}
			}
		}
		if (username.equals(waitingUser)) {
			waitingUser = null;
		}
	}
	
	/**
	 * @ensures a getter for server description
	 * @return
	 */
	public String getDescription() {
		return serverDescription;
	}
	
	/**
	 * @ensures prints the messages to the client's screen
	 * @param message
	 */
	public void printTUI(String message) {
		tui.print(message);
	}
}
