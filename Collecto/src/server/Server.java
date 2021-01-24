package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import game.Game;
import protocols.ProtocolMessages;
import protocols.ServerProtocol;

public class Server implements ServerProtocol, Runnable {
	
	private int port;
	private String serverDescription;
	private ServerTUI tui;
	
	private Map<String, ClientHandler> activeHandlers = new HashMap<String, ClientHandler>();
	
	private Map<String, Game> activeGames = new HashMap<String, Game>();
	
	private String waitingUser;
	
	private Map<String, Integer> wins = new HashMap<String, Integer>();
	
	public Server(int port, String description, ServerTUI tui) {
		this.port = port;
		this.serverDescription = description;
		this.tui = tui;
	}
	
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
	
	public synchronized String handleLogin(ClientHandler ch, String message) {
		String[] params = message.split(ProtocolMessages.DELIM, 2);
		if (params.length < 2) return ProtocolMessages.ERROR;
		if (!params[0].equals(ProtocolMessages.LOGIN)) return ProtocolMessages.ERROR;
		
		if (activeHandlers.containsKey(params[1])) return ProtocolMessages.ALREADYLOGGEDIN;
		activeHandlers.put(params[1], ch);
		ch.setUsername(params[1]);
		
		if (!wins.containsKey(params[1])) {
			wins.put(params[1], 0);
		}
		
		return ProtocolMessages.LOGIN;
	}
	
	public synchronized String handleList(ClientHandler ch, String message) {
		//to-do: Actually return correct response
		return ProtocolMessages.LIST;
	}
	
	public synchronized String handleQueue(ClientHandler ch, String message) {
		//to-do: Actually return correct response (this should return null or newgame)
		// (if this returns a newgame, also notify the opponent with a new game)
		return ProtocolMessages.NEWGAME;
	}
	
	public synchronized String handleMove(ClientHandler ch, String message) {
		//to-do: Actually return correct response (this should return null and notify the opponent with a move message)
		// (this can also return gameover and notify the opponent with a game over)
		return ProtocolMessages.MOVE;
	}
	
	public synchronized String handleRank(ClientHandler ch, String message) {
		//to-do: Actually return correct response 
		return ProtocolMessages.RANK;
	}
	
	public synchronized String handleChat(ClientHandler ch, String message) {
		//to-do: Actually return correct response (this should return null and notify everyone else with a chat message)
		return ProtocolMessages.CHAT;
	}
	
	public synchronized String handleWhisper(ClientHandler ch, String message) {
		//to-do: Actually return correct response (this should return null and notify the recipients with a whisper message)
		return ProtocolMessages.WHISPER;
	}
	
	public synchronized void removeClient(ClientHandler ch) {
		String username = ch.getUsername();
		activeHandlers.remove(username);
		//to-do: notify the opponent with a game over
	}
	
	public String getDescription() {
		return serverDescription;
	}
	
	public void printTUI(String message) {
		tui.print(message);
	}
}
