package protocols;

import java.io.IOException;

public interface ClientProtocol {
	
	public void handleHello(String message) throws IOException;
	
	public boolean handleLogin(String message) throws IOException;
	
	public void handleList(String message) throws IOException;
	
	public void handleNewGame(String message) throws IOException;
	
	public void handleMove(String message) throws IOException;
	
	public void handleGameOver(String message) throws IOException;
	
	public void handleRank(String message) throws IOException;
	
	public void handleChat(String message) throws IOException;
	
	public void handleWhisper(String message) throws IOException;
}
