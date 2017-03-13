package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import es.ubu.lsi.common.ChatMessage;

public class ServerThreadForClient extends Thread {

	private int id;
	private String username;
	private Socket clientSocket;
	private ChatServerImpl chatServer;

	public ServerThreadForClient(int id, ChatServerImpl chatserver, Socket clientSocket) {
		this.id = id;
		this.clientSocket = clientSocket;
		this.chatServer = chatserver;
	}

	@Override
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(this.clientSocket.getInputStream());

//			System.out.println("Se ha conectado el cliente con IP: " + clientSocket.getInetAddress().getHostAddress()
//					+ " y puerto: " + clientSocket.getPort() + "\n");
			ChatMessage inputLine = (ChatMessage) in.readObject();

			System.out.println("Se ha conectado un cliente:");
			System.out.println("\t- IP: " + clientSocket.getInetAddress().getHostAddress());
			System.out.println("\t- PORT: " + clientSocket.getPort());
			System.out.println("\t- USERNAME: " + inputLine.getMessage() + "\n");
			System.out.println("asdcgggh" + in.readObject());

			inputLine = null;

			System.out.println("asdcgggh" + in.readObject());
			while (!((inputLine = (ChatMessage) in.readObject()).equals(null))) {
				System.out.println(username + " envió: " + inputLine + "\n");
				ChatMessage msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, username + " : " + inputLine);
				chatServer.broadcast(msg);
			}
		} catch (IOException | ClassNotFoundException e) {
			// e.printStackTrace();
			chatServer.remove(id);
			this.interrupt();
		}

	}

	public int getClientId() {
		return id;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}
}
