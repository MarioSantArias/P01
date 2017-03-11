package es.ubu.lsi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import es.ubu.lsi.common.ChatMessage;

public class ServerThreadForClient extends Thread {

	private int id;
	private String username;
	private Socket clientSocket;
	private ChatServerImpl chatServer;

	public ServerThreadForClient(ChatServerImpl chatserver, Socket clientSocket) {
		this.clientSocket = clientSocket;
		this.chatServer = chatserver;
	}

	@Override
	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			System.out.println("Se ha conectado el cliente con IP: " + clientSocket.getInetAddress().getHostAddress()
					+ " y puerto: " + clientSocket.getPort() + "\n");
			String inputLine;
			
			while ((inputLine = in.readLine()) != null) {
				System.out.println("He recibido: " + inputLine + "\n");
				ChatMessage msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, inputLine);
				chatServer.broadcast(msg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public int getClientId() {
		return id;
	}
	
	public Socket getClientSocket(){
		return clientSocket;
	}
}
