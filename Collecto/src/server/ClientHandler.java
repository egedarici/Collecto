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
	
	private String clientDescription;
	private String[] extensions;
	private String username;
	
	public ClientHandler(Socket socket, Server server) throws IOException {
		this.server = server;
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}
	
	public void run() {
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
			
			while (true) {
				String message = receiveMessage();
				String response = handleMessage(message);
				sendMessage(response);
			}
		} catch (IOException e) {
			server.removeClient(this);
			if (clientDescription != null) {
				server.printTUI(server.getDescription() + ": ClientHandler for " + clientDescription + " shutting down... (Connection lost)");
			} else {
				server.printTUI(server.getDescription() + ": ClientHandler for thread shutting down... (Connection lost)");
			}
		}
		try {
			socket.close();
		} catch (IOException e) {
			if (clientDescription != null) {
				server.printTUI(server.getDescription() + ": Socket for " + clientDescription + " couldn't be closed.");
			} else {
				server.printTUI(server.getDescription() + ": Socket couldn't be closed.");
			}
		}
	}
	
	/**
	 * Calls the correct server method that will handle the command inside the message.
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
	
	public void setDescription(String description) {
		this.clientDescription = description;
	}
	
	public void setExtentions(String[] extensions) {
		this.extensions = extensions;
	}
	
	public boolean containsExtension(String extension) {
		for (int i = 0; i < extensions.length; i++) {
			if (extensions[i].equals(extension)) return true;
		}
		return false;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
}
