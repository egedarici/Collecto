package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import protocols.ProtocolMessages;

public class ClientHandler implements Runnable {
	
	private Socket socket;
	private BufferedReader reader;
	private BufferedWriter writer;
	
	/**
	 * This should hold a reference to the server running this handler.
	 */
	private Server server;
	
	private volatile boolean isActive;
	
	private String clientDescription;
	private String[] extensions;
	private String username;
	
	/**
	 * @requires socket != null
	 * @requires server != null
	 * @ensures server, socket, reader and writer are initialized
	 * @param socket
	 * @param server
	 * @throws IOException
	 */
	public ClientHandler(Socket socket, Server server) throws IOException {
		this.server = server;
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	/**
	 * This method establishes the connection between the client and the server with sockets
	 */
	public void run() {
		isActive = false;
		try {
			String helloMessage = receiveMessage();
			String helloResponse = server.handleHello(this, helloMessage);
			sendMessage(helloResponse);
			
			boolean loggedIn = false;
			while (!loggedIn) {
				String loginMessage = receiveMessage();
				String loginResponse = server.handleLogin(this, loginMessage);
				sendMessage(loginResponse);
				if (loginResponse.equals(ProtocolMessages.LOGIN)) {
					loggedIn = true;
				}
			}
			
			isActive = true;
		} catch (IOException e) {
			printConnectionError();
		}
		
		while (isActive) {
			try {
				String message = receiveMessage();
				String response = handleMessage(message);
				sendMessage(response);
			} catch (IOException e) {
				isActive = false;
				printConnectionError();
			}
		}
		server.removeClient(this);
		
		try {
			socket.close();
		} catch (IOException e) {
			printConnectionError();
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures Calls the correct server method that will handle the command inside the message.
	 * @param message
	 * @return
	 */
	public String handleMessage(String message) {
		String[] params = message.split(ProtocolMessages.DELIM, 2);
		if (params.length < 1) return ProtocolMessages.ERROR;
		
		switch (params[0]) {
		case ProtocolMessages.LIST:
			return server.handleList(this, message);
		case ProtocolMessages.QUEUE:
			return server.handleQueue(this, message);
		case ProtocolMessages.MOVE:
			return server.handleMove(this, message);
		case ProtocolMessages.RANK:
			return server.handleRank(this, message);
		case ProtocolMessages.CHAT:
			return server.handleChat(this, message);
		case ProtocolMessages.WHISPER:
			return server.handleWhisper(this, message);
		default:
			return ProtocolMessages.ERROR;
		}
	}
	
	/**
	 * @requires message != null
	 * @ensures client's message is sent to the server
	 * @param message
	 * @throws IOException
	 */
	public synchronized void sendMessage(String message) throws IOException {
		if (message == null) return;
		writer.write(message + "\n");
		writer.flush();
		
	}
	
	/**
	 * @ensures receives the message of client 
	 * @return
	 * @throws IOException
	 */
	public String receiveMessage() throws IOException {
		String message = reader.readLine();
		if (message == null) throw new IOException();
		return message;
	}
	
	/**
	 * @requires description != null
	 * @ensures client's description is set
	 * @param description
	 */
	public void setDescription(String description) {
		this.clientDescription = description;
	}
	
	/**
	 * @requires extensions extensions != null
	 * @ensures extentions are set
	 * @param extensions
	 */
	public void setExtentions(String[] extensions) {
		this.extensions = extensions;
	}
	
	/**
	 * @requires extension != null
	 * @ensures if specific extension appears to be there
	 * @param extension
	 * @return true if contains, false otherwise
	 */
	public boolean containsExtension(String extension) {
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i].equals(extension)) return true;
		}
		return false;
	}
	
	/**
	 * @requires username != null
	 * @ensures username is set to specific username
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * @requires username != null
	 * @ensures a getter for username
	 * @return
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * @requires isActive == true;
	 * @ensures isActive is false and thread is deactive
	 */
	public void deactivateThread() {
		isActive = false;
	}
	
	/**
	 * Prints the errors for client hanndler
	 */
	public void printConnectionError() {
		if (clientDescription != null) {
			server.printTUI(server.getDescription() + ": ClientHandler for " + clientDescription + " shutting down... (Connection lost)");
		} else {
			server.printTUI(server.getDescription() + ": ClientHandler for thread shutting down... (Connection lost)");
		}
	}
}
