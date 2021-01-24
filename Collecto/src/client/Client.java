package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import game.Game;
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
	
	private Game game;
	
	public Client(String description, Socket socket, ClientTUI tui) throws IOException {
		this.description = description;
		this.tui = tui;
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	public void run() {
		try {
			sendMessage(ProtocolMessages.HELLO + ProtocolMessages.DELIM + description + 
					ProtocolMessages.DELIM + ProtocolMessages.RANK +  ProtocolMessages.DELIM + ProtocolMessages.CHAT);
			String helloMessage = receiveMessage();
			handleHello(helloMessage);
			
			while (!loggedIn) {
				printTUI("Please enter a username to login.");
				String username = receiveTUICommand();
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
	
	public void handleHello(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length < 2) sendMessage(ProtocolMessages.ERROR);
		if (!params[0].equals(ProtocolMessages.HELLO)) sendMessage(ProtocolMessages.ERROR);
		
		String[] extensions = new String[params.length - 2];
		for (int i = 0; i < extensions.length; i++) {
			extensions[i] = params[i+2];
		}
		setServerExtensions(extensions);
	}
	
	public boolean handleLogin(String message) throws IOException {
		String[] params = message.split(ProtocolMessages.DELIM);
		if (params.length != 1) {
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
	
	public void handleList(String message) throws IOException {
		//to-do : Actually handle server message
		printTUI("List of players: ");
	}
	
	public void handleNewGame(String message) throws IOException {
		//to-do : Actually handle server message
		printTUI("New game started.");
	}
	
	public void handleMove(String message) throws IOException {
		//to-do : Actually handle server message
		printTUI("Opponent made a move.");
	}
	
	public void handleGameOver(String message) throws IOException {
		//to-do : Actually handle server message
		printTUI("Game over. ");
	}
	
	public void handleRank(String message) throws IOException {
		//to-do : Actually handle server message
		printTUI("Rankings:  ");
	}
	
	public void handleChat(String message) throws IOException {
		//to-do : Actually handle server message
		printTUI("Global chat message: ");
	}
	
	public void handleWhisper(String message) throws IOException {
		//to-do : Actually handle server message
		printTUI("Private chat message:  ");
	}
	
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
		case ProtocolMessages.ERROR:
			printTUI("Server error occured.");
			break;
		}
	}
	
	public void setServerExtensions(String[] serverExtensions) {
		this.serverExtensions = serverExtensions;
	}
	
	public boolean serverContainsExtension(String extension) {
		for (int i = 0; i < serverExtensions.length; i++) {
			if (serverExtensions[i].equals(extension)) return true;
		}
		return false;
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public synchronized void sendMessage(String message) throws IOException {
		if (message == null) return;
		writer.write(message + "\n");
		writer.flush();
	}
	
	public String receiveMessage() throws IOException {
		String message = reader.readLine();
		if (message == null) throw new IOException();
		return message;
	}
	
	public void printTUI(String message) {
		tui.print(message);
	}
	
	public String receiveTUICommand() {
		return tui.scan();
	}
	
	public void handleListCommand(String line) throws IOException  {
		sendMessage(ProtocolMessages.LIST);
	}
	
	public void handleQueueCommand(String line) throws IOException  {
		sendMessage(ProtocolMessages.QUEUE);
	}
	
	public void handleMoveCommand(String line) throws IOException  {
		//to-do : send an actual move
		sendMessage(ProtocolMessages.MOVE);
	}
	
	public void handleHintCommand(String line) throws IOException  {
		//to-do : calculate and display a hint
	}
	
	public void handleRankCommand(String line) throws IOException  {
		sendMessage(ProtocolMessages.RANK);
	}
	
	public void handleChatCommand(String line) throws IOException  {
		//to-do : send an actual message
		sendMessage(ProtocolMessages.CHAT);
	}
	
	public void handleWhisperCommand(String line) throws IOException  {
		//to-do : send an actual whisper
		sendMessage(ProtocolMessages.WHISPER);
	}

}
