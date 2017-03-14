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

	public ServerThreadForClient(int id, ChatServerImpl chatserver, Socket clientSocket) {
		this.id = id;
		this.clientSocket = clientSocket;
		this.chatServer = chatserver;
	}

	@Override
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(this.clientSocket.getInputStream());
			ChatMessage inputLine;

			inputLine = (ChatMessage) in.readObject();
			username = inputLine.getMessage();
			System.out.println("Se ha conectado un cliente:");
			System.out.println("\t- IP: " + clientSocket.getInetAddress().getHostAddress());
			System.out.println("\t- PORT: " + clientSocket.getPort());
			System.out.println("\t- USERNAME: " + username + "\n");

			while ((inputLine = (ChatMessage) in.readObject()) != null) {
				System.out.println(username + " envió: " + inputLine.getMessage());
				ChatMessage msg;
				switch(inputLine.getType()){
				case MESSAGE:
					msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, username + " : " + inputLine.getMessage());
					chatServer.broadcast(msg);
					break;
					
				case BAN:
					int idBan = 0;
					ObjectOutputStream out;
					
					for(ServerThreadForClient elem : chatServer.getConectedClients()){
						if(elem.getUsername().equals(inputLine.getMessage())){
							idBan = elem.getClientId();
						}
					}	
					msg = new ChatMessage(idBan, ChatMessage.MessageType.BAN, inputLine.getMessage());
					out = new ObjectOutputStream(this.clientSocket.getOutputStream());
					out.reset();
					out.writeObject(msg);
					
					break;
				case LOGOUT:
					break;
				
				default:
					System.out.println("Case default");
					break;	
				}
				
				
//				if (!(inputLine.getType().equals(ChatMessage.MessageType.LOGOUT))) {
//					ChatMessage msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, username + " : " + inputLine.getMessage());
//					chatServer.broadcast(msg);
//				} else {
//					break;
//				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			System.out.println("El usuario " + username + " se ha desconectado.");
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

	public String getUsername() {
		return username;
	}
}
