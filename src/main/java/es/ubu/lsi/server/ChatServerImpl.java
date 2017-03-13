package es.ubu.lsi.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer {

	private static ChatServerImpl instance = null;
	private final int DEFAULT_PORT = 1500;
	private int clientId;
	private SimpleDateFormat sdf;
	private int port;
	private boolean alive;
	private ServerSocket serverSocket;
	private List<ServerThreadForClient> conectedClients;

	public ChatServerImpl() {
		this.port = DEFAULT_PORT;
		clientId = 0;
		conectedClients = new ArrayList<ServerThreadForClient>();
	}

	public ChatServerImpl(int port) {
		this.port = port;
		clientId = 0;
		conectedClients = new ArrayList<ServerThreadForClient>();
	}

	@Override
	public void startup() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Servidor iniciado, esperando conexiones.");
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Se ha conectado el cliente.");
				ServerThreadForClient thread = new ServerThreadForClient(clientId++, this, clientSocket);
				thread.start();
				conectedClients.add(thread);
			}
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + port + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}

	@Override
	public void shutdown() {
		try {
			for (ServerThreadForClient client : conectedClients) {
				client.getClientSocket().close();
				client.interrupt();
			}
			serverSocket.close();
			System.exit(1);
		} catch (IOException e) {
			System.exit(0);
		}
	}

	@Override
	public synchronized void broadcast(ChatMessage message) {
		PrintWriter out;
		for (ServerThreadForClient client : conectedClients) {
			try {
				if (message.getId() != client.getClientId()) {
					out = new PrintWriter(client.getClientSocket().getOutputStream(), true);
					out.println(message.getMessage());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void remove(int id) {
		for (int i = 0; i < conectedClients.size(); i++) {
			if (conectedClients.get(i).getClientId() == id) {
				conectedClients.get(i).interrupt();
				conectedClients.remove(i);
				break;
			}
		}
	}

	public static ChatServerImpl getInstance() {
		if (instance == null) {
			instance = new ChatServerImpl();
		}
		return instance;
	}

	public static ChatServerImpl getInstance(int port) {
		if (instance == null) {
			instance = new ChatServerImpl(port);
		}
		return instance;
	}

	public static void main(String[] args) {
		ChatServerImpl chatServer;

		if (args.length != 1) {
			System.out.println("Se usara el puerto por defecto: 1500");
			chatServer = getInstance();
		} else {
			System.out.println("Se usara el puerto: " + args[0]);
			chatServer = getInstance(Integer.parseInt(args[0]));
		}

		System.out.println("Iniciando el servidor...");
		chatServer.startup();
	}

}
