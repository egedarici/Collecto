package test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import exceptions.IllegalMoveException;
import exceptions.PlayerNotFoundException;
import exceptions.WrongTurnException;
import game.Game;
import game.Move;
import protocols.ProtocolMessages;
import server.Server;
import server.ServerTUI;

class ServerTest {
	private Server server;
	
	private static final String IP = "localhost";
	private static final int PORT = 8888;
	private static final String DESCRIPTION = "Ege's server";

	@BeforeEach
	void setUp() throws Exception {
		server = new Server(PORT, DESCRIPTION, new ServerTUI());
		new Thread(server).start();
	}

	@Test
	void test() throws IOException {
		Socket clientSocket1 = new Socket(IP, PORT);
		Socket clientSocket2 = new Socket(IP, PORT);
	
		BufferedReader reader1 = new BufferedReader(new InputStreamReader(clientSocket1.getInputStream()));
		BufferedWriter writer1 = new BufferedWriter(new OutputStreamWriter(clientSocket1.getOutputStream()));
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(clientSocket2.getInputStream()));
		BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(clientSocket2.getOutputStream()));
		
		writer1.write(ProtocolMessages.HELLO + ProtocolMessages.DELIM + "Client: Alice" + ProtocolMessages.DELIM + ProtocolMessages.RANK + ProtocolMessages.DELIM + ProtocolMessages.CHAT + "\n");
		writer1.flush();
		String response = reader1.readLine();
		assertThat(response, containsString(ProtocolMessages.HELLO + ProtocolMessages.DELIM + DESCRIPTION));
		assertThat(response, containsString(ProtocolMessages.DELIM + ProtocolMessages.RANK));
		assertThat(response, containsString(ProtocolMessages.DELIM + ProtocolMessages.CHAT));
		
		writer2.write(ProtocolMessages.HELLO + ProtocolMessages.DELIM + "Client: Bob" + ProtocolMessages.DELIM + ProtocolMessages.RANK + ProtocolMessages.DELIM + ProtocolMessages.CHAT + "\n");
		writer2.flush();
		response = reader2.readLine();
		assertThat(response, containsString(ProtocolMessages.HELLO + ProtocolMessages.DELIM + DESCRIPTION));
		assertThat(response, containsString(ProtocolMessages.DELIM + ProtocolMessages.RANK));
		assertThat(response, containsString(ProtocolMessages.DELIM + ProtocolMessages.CHAT));
		
		writer1.write(ProtocolMessages.LOGIN + ProtocolMessages.DELIM + "Alice" + "\n");
		writer1.flush();
		response = reader1.readLine();
		assertThat(response, containsString(ProtocolMessages.LOGIN));
		
		writer2.write(ProtocolMessages.LOGIN + ProtocolMessages.DELIM + "Alice" + "\n");
		writer2.flush();
		response = reader2.readLine();
		assertThat(response, containsString(ProtocolMessages.ALREADYLOGGEDIN));
		
		writer1.write(ProtocolMessages.LIST + "\n");
		writer1.flush();
		response = reader1.readLine();
		assertThat(response, containsString(ProtocolMessages.LIST));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Alice"));
		assertThat(response, not(containsString(ProtocolMessages.DELIM + "Bob")));
		
		writer2.write(ProtocolMessages.LOGIN + ProtocolMessages.DELIM + "Bob" + "\n");
		writer2.flush();
		response = reader2.readLine();
		assertThat(response, containsString(ProtocolMessages.LOGIN));
		
		writer1.write(ProtocolMessages.LIST + "\n");
		writer1.flush();
		response = reader1.readLine();
		assertThat(response, containsString(ProtocolMessages.LIST));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Alice"));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Bob"));
		
		writer1.write(ProtocolMessages.CHAT + ProtocolMessages.DELIM + "Alice" + ProtocolMessages.DELIM + "Hey" + "\n");
		writer1.flush();
		response = reader2.readLine();
		assertThat(response, containsString(ProtocolMessages.CHAT));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Alice"));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Hey"));
		
		writer1.write(ProtocolMessages.WHISPER + ProtocolMessages.DELIM + "Bob" + ProtocolMessages.DELIM + "Whisper" + "\n");
		writer1.flush();
		response = reader2.readLine();
		assertThat(response, containsString(ProtocolMessages.WHISPER));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Alice"));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Whisper"));
		
		writer1.write(ProtocolMessages.RANK + "\n");
		writer1.flush();
		response = reader1.readLine();
		assertThat(response, containsString(ProtocolMessages.RANK));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Alice" + ProtocolMessages.DELIM + 0));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Bob" + ProtocolMessages.DELIM + 0));
		
		writer1.write(ProtocolMessages.QUEUE + "\n");
		writer1.flush();
		writer2.write(ProtocolMessages.QUEUE + "\n");
		writer2.flush();
		response = reader1.readLine();
		String response2 = reader2.readLine();
		assertEquals(response, response2);
		assertThat(response, containsString(ProtocolMessages.NEWGAME));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Alice"));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Bob"));
		String[] params = response.split(ProtocolMessages.DELIM);
		assertEquals(52, params.length);
		String name1 = params[50];
		String name2 = params[51];
		int[] board = new int[49];
		try {
			for (int i = 0; i < board.length; i++) {
				board[i] = Integer.parseInt(params[i+1]); 
			}
		} catch (NumberFormatException e) {
			fail("NumberFormatException occured.");
		}
		Game game = new Game(name1, name2, board);
		while (!game.getBoard().gameOver()) {
			Move move = game.getCurrentPlayer().getHint(game.getBoard());
			if (game.getCurrentPlayer().getUsername().equals(name1)) {
				writer1.write(ProtocolMessages.MOVE + move.toString() + "\n");
				writer1.flush();
				response = reader2.readLine();
				assertThat(response, containsString(ProtocolMessages.MOVE + move.toString()));
			} else {
				writer2.write(ProtocolMessages.MOVE + move.toString() + "\n");
				writer2.flush();
				response = reader1.readLine();
				assertThat(response, containsString(ProtocolMessages.MOVE + move.toString()));
			}
			try {
				game.makeMove(move);
			} catch (PlayerNotFoundException | WrongTurnException | IllegalMoveException e) {
				fail("Exception occured.");
			}
		}
		
		boolean exit = false;
		if (game.getBoard().gameOver()) {
			while (!exit) {
				if(game.getWinner() == 1) {
					response = reader1.readLine();
					response2 = reader2.readLine();
					assertEquals(response, response2);
					assertThat(response, containsString(ProtocolMessages.GAMEOVER));
					assertThat(response, containsString(ProtocolMessages.DELIM + ProtocolMessages.VICTORY));
					assertThat(response, containsString(ProtocolMessages.DELIM + "Alice"));
					exit = true;
				} else if(game.getWinner() == 2) {
					response = reader1.readLine();
					response2 = reader2.readLine();
					assertEquals(response, response2);
					assertThat(response, containsString(ProtocolMessages.GAMEOVER));
					assertThat(response, containsString(ProtocolMessages.DELIM + ProtocolMessages.VICTORY));
					assertThat(response, containsString(ProtocolMessages.DELIM + "Bob"));
					exit = true;
				} else {
					response = reader1.readLine();
					response2 = reader2.readLine();
					assertEquals(response, response2);
					assertThat(response, containsString(ProtocolMessages.GAMEOVER));
					assertThat(response, containsString(ProtocolMessages.DELIM + ProtocolMessages.DRAW));
					exit = true;
				}
			}
		}
		
		writer1.write(ProtocolMessages.RANK + "\n");
		writer1.flush();
		response = reader1.readLine();
		assertThat(response, containsString(ProtocolMessages.RANK));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Alice" + ProtocolMessages.DELIM + 1));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Bob" + ProtocolMessages.DELIM + 0));
		
		writer1.write(ProtocolMessages.QUEUE + "\n");
		writer1.flush();
		writer2.write(ProtocolMessages.QUEUE + "\n");
		writer2.flush();
		response = reader1.readLine();
		response2 = reader2.readLine();
		assertEquals(response, response2);
		assertThat(response, containsString(ProtocolMessages.NEWGAME));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Alice"));
		assertThat(response, containsString(ProtocolMessages.DELIM + "Bob"));
		
		reader1.close();
		writer1.close();
		reader2.close();
		writer2.close();
		clientSocket1.close();
		clientSocket2.close();
	}
}
