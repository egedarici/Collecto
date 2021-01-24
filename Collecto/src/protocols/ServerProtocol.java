package protocols;

import server.ClientHandler;

public interface ServerProtocol {
	
	public String handleHello(ClientHandler ch, String message);
	
	public String handleLogin(ClientHandler ch, String message);
	
	public String handleList(ClientHandler ch, String message);
	
	public String handleQueue(ClientHandler ch, String message);
	
	public String handleMove(ClientHandler ch, String message);
	
	public String handleRank(ClientHandler ch, String message);
	
	public String handleChat(ClientHandler ch, String message);
	
	public String handleWhisper(ClientHandler ch, String message);
}
