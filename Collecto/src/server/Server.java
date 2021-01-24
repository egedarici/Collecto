package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import protocols.ServerProtocol;

public class Server implements ServerProtocol, Runnable {
	
	private int port;
	private String description;
	
	public Server(int port, String description) {
		this.port = port;
		this.description = description;
	}
	
	public void run() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println(description + ": Server socket couldn't be created.");
		}
		
		while (ss != null) {
			try {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket);
				new Thread(handler).start();
			} catch (IOException e) {
				System.out.println(description + ": Connection failed.");
			}
		}
	}
}
