package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import client.Client;
import server.Server;

class ClientTest {
	private Client client;
	
	private Server server;
	
	private static final String IP = "localhost";
	private static final int PORT = 8888;
	private static final String DESCRIPTION = "Ege's server";
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void test() {
		fail("Not yet implemented");
	}

}
