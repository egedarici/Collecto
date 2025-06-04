# Collecto Multiplayer Game

This is a Java-based multiplayer game built for a project in University of Twente. It allows two players to connect to a server and play a turn-based board game in the terminal.

## How to Run

### 1. Clone the Repository

```bash
git clone https://github.com/egedarici/Collecto.git
cd Collecto/src
```

### 2. Make Sure Java is Installed

```bash
java -version
javac -version
```

### 3. Compile the Project

Run this inside the `src` directory:

```bash
javac client/*.java server/*.java game/*.java protocols/*.java
```

### 4. Start the Server

In one terminal:

```bash
java server.ServerTUI
```

Give any port and name to your server:

```
Enter port, followed by a server description (E.g. 1234 Bob's server)
```

### 5. Start the Clients

Open two separate terminals. In each one:

```bash
cd path/to/Collecto/src
java client.ClientTUI
```

Then follow the prompts:

- Enter a client description: `Player1` or `Player2`
- Enter server IP: `localhost`
- Enter port: `8800` (the port you've given to the server)
- Enter your username: `Player1` or `Player2`

### 6. Start the Game

In both client terminals, type:

```bash
queue
```

Once both players are queued, the game will start.
