package server;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ServerTUI {
	
	
	public void startTUI(){
		Scanner scanner = new Scanner(System.in);
		Set<Integer> usedPorts = new HashSet<Integer>();
		
		boolean exit = false;
		while (!exit) {
			print("Enter port, followed by a server description (E.g. 1234 Bob's server)");
			print("Enter exit to stop creating servers");
			String command = scanner.nextLine();
			if (command.equals("exit")) {
				exit = true;
			} else {
				String[] params = command.split(" ", 2);
				if (params.length < 2) {
					print("Command not recognized.");
				} else {
					try {
						int port = Integer.parseInt(params[0]);
						if (usedPorts.contains(port)) {
							print("Server with this port already exists.");
						} else {
							Server server = new Server(port, params[1], this);
							usedPorts.add(port);
							new Thread(server).start();
						}
					} catch (NumberFormatException e) {
						print("Port must be an integer.");
					}
				}
			}
		}
		scanner.close();
	}
	
	public synchronized void print(String message) {
		if (message == null) return;
		System.out.println(message);
	}
	
	public static void main(String[] args) {
		new ServerTUI().startTUI();;
	}
}
