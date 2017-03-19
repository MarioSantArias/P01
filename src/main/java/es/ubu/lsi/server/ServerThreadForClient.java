package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import es.ubu.lsi.common.ChatMessage;

public class ServerThreadForClient extends Thread {

	private int id;
	private String username;
	private Socket clientSocket;
	private ChatServerImpl chatServer;
	private ObjectOutputStream out;

	public ServerThreadForClient(int id, ChatServerImpl chatserver, Socket clientSocket) throws IOException {
		this.id = id;
		this.clientSocket = clientSocket;
		this.chatServer = chatserver;
		out = new ObjectOutputStream(this.clientSocket.getOutputStream());
	}

	@Override
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(this.clientSocket.getInputStream());

			ChatMessage inputLine;

			inputLine = (ChatMessage) in.readObject();
			username = inputLine.getMessage();
			System.out.println("\nSe ha conectado un cliente:");
			System.out.println("\t- IP: " + clientSocket.getInetAddress().getHostAddress());
			System.out.println("\t- PORT: " + clientSocket.getPort());
			System.out.println("\t- USERNAME: " + username + "\n");
			chatServer.broadcast(new ChatMessage(id, ChatMessage.MessageType.UPDATEBAN, username));

			while ((inputLine = (ChatMessage) in.readObject()) != null) {
				System.out.println("--- " + username + " envió: " + inputLine.getMessage());
				ChatMessage msg;
				switch (inputLine.getType()) {
				case MESSAGE:
					msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE,
							username + ": " + inputLine.getMessage());
					chatServer.broadcast(msg);
					break;

				case BAN:
					sendBanInfo(ChatMessage.MessageType.BAN, inputLine.getMessage());
					System.out.println("### " + username + " ha baneado a " + inputLine.getMessage());
					break;

				case UNBAN:
					sendBanInfo(ChatMessage.MessageType.UNBAN, inputLine.getMessage());
					System.out.println("### " + username + " ha eliminado el baneado sobre " + inputLine.getMessage());
					break;

				case LOGOUT:
					System.out.println("### " + "El usuario " + username + " se ha desconectado.");
					break;

				default:
					System.out.println("Envio de mensaje inadecuado.");
					break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			// e.printStackTrace();
			System.err.println("El usuario " + username + " se ha desconectado forzosamente.");
		} finally {
			chatServer.remove(id);
			this.interrupt();
		}
	}

	private void sendBanInfo(ChatMessage.MessageType msgType, String msg) {
		try {
			int id = 0;
			for (ServerThreadForClient elem : chatServer.getConectedClients()) {
				if (elem.getUsername().equals(msg)) {
					id = elem.getClientId();
					break;
				}
			}
			ChatMessage msgToSend = new ChatMessage(id, msgType, msg);
			out.reset();
			out.writeObject(msgToSend);
		} catch (IOException e) {
			System.err.println("Error al enviar mensaje con la informacion de ban/unban.");
		}
	}

	public int getClientId() {
		return id;
	}

	public Socket getClientSocket() {
		return clientSocket;
	}

	public String getUsername() {
		return username;
	}

	public ObjectOutputStream getOutputStream() {
		return out;
	}
}
