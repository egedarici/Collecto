package server;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class ServerTUI {
	
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		Set<Integer> usedPorts = new HashSet<Integer>();
		
		boolean exit = false;
		while (!exit) {
			System.out.println("Enter port, followed by a server description (E.g. 1234 Bob's server)");
			System.out.println("Enter exit to shut down servers");
			String command = scanner.nextLine();
			if (command.equals("exit")) {
				exit = true;
			} else {
				String[] params = command.split(" ", 2);
				if (params.length < 2) {
					System.out.println("Command not recognized.");
					try {
						int port = Integer.parseInt(params[0]);
						if (usedPorts.contains(port)) {
							System.out.println("Server with this port already exists.");
						} else {
							Server server = new Server(port, params[1]);
							usedPorts.add(port);
							new Thread(server).start();
						}
					} catch (NumberFormatException e) {
						System.out.println("Port must be an integer.");
					}
				}
			}
		}
		scanner.close();
	}
}
