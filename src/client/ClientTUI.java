package client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientTUI {
	
	private Scanner scanner;
	private Client client;
	
	/**
	 * @ensures Scanner is initialized
	 */
	public ClientTUI() {
		scanner = new Scanner(System.in);
	}
	
	/**
	 * Establishes the connection between the screen and the client.
	 */
	public void startTUI() {
		print("Please enter client description");
		String description = scan();
		print("Please enter server IP");
		String host = scan();
		print("Please enter port number");
		try {
			String portstr = scan();
			int port = Integer.parseInt(portstr);
			InetAddress addr = InetAddress.getByName(host);
			Socket socket = new Socket(addr, port);
			client = new Client(description, socket, this);
			new Thread(client).start();
			
			boolean loggedIn = false;
			while (!loggedIn) {
				loggedIn = client.isLoggedIn();
			}
			
			printHelp();
			while (true) {
				print("Please enter a command.");
				String line = scan();
				handleUserCommand(line);
			}
		} catch (NumberFormatException e) {
			print("Invalid port number.");
		} catch (UnknownHostException e) {
			print("Unknown host.");
		} catch (IOException e) {
			print("Connection failed.");
		}
		scanner.close();
	}
	
	/**
	 * @requires line != null
	 * @ensures Handles the commands from the screen.
	 * @param line
	 * @throws IOException
	 */
	public void handleUserCommand(String line) throws IOException {
		String[] params = line.split(" ", 2);
		String command = params[0];
		switch (command) {
		case "help":
			printHelp();
			break;
		case "list":
			client.handleListCommand();
			break;
		case "queue":
			client.handleQueueCommand();
			break;
		case "move":
			client.handleMoveCommand(line);
			break;
		case "ai":
			client.handleAICommand(line);
			break;
		case "hint":
			client.handleHintCommand();
			break;
		case "rank":
			client.handleRankCommand();
			break;
		case "chat":
			client.handleChatCommand(line);
			break;
		case "whisper":
			client.handleWhisperCommand(line);
			break;
		default:
			print("Please enter a valid command.");
			break;
		}
	}
	
	/**
	 * @ensures thread-safety. ClientTUI and Client threads may try to output to System.out at the same time. 
	 * We fix this potential problem by making a synchronized print method.
	 * @param message
	 */
	public synchronized void print(String message) {
		if (message == null) return;
		System.out.println(message);
	}
	
	/**
	 * @ensures scanner gets the next line
	 * @return next String line
	 */
	public String scan() {
		return scanner.nextLine();
	}
	
	/**
	 * @ensures the possible commands are printed to help the user
	 */
	public void printHelp() {
		print("Possible commands: ");
		print("help.................................print this help menu");
		print("list.................................list all clients logged into server");
		print("queue................................queue for a game / unqueue");
		print("move <moveNumber1> <moveNumber2>.....make a move indicated by the move number(s)");
		print("ai <strategy>........................choose ai strategy (random, dumb, smart) or disable ai (none)");
		print("hint.................................request hint for current move");
		print("rank.................................print player rankings on the server");
		print("chat <message>.......................send message to the global chat");
		print("whisper <recipient> <message>........send message to a specific player (recipient)");
	}
	
	
	public static void main(String[] args) {
		new ClientTUI().startTUI();
	}
	
}
