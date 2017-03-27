package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import es.ubu.lsi.common.ChatMessage;

/**
 * 
 * @author Felix Nogal
 * @author Mario Santamaria
 *
 */
public class ChatServerImpl implements ChatServer {
	
	/** Instancia del servidor */
	private static ChatServerImpl instance = null;
	/** Puerto por defecto para la conexion */
	private final int DEFAULT_PORT = 1500;
	/** Contador para los identificadores del cliente */
	private int clientId;
	/** Puerto para la conexion */
	private int port;
	/** Socket del servidor */
	private ServerSocket serverSocket;
	/** Lista de clientes conectados */
	private List<ServerThreadForClient> conectedClients;

	/**
	 * Constructor.
	 */
	public ChatServerImpl() {
		this.port = DEFAULT_PORT;
		clientId = 0;
		conectedClients = new ArrayList<ServerThreadForClient>();
	}

	/**
	 * Constructor.
	 * @param port El puerto para la conexion.
	 */
	public ChatServerImpl(int port) {
		this.port = port;
		clientId = 0;
		conectedClients = new ArrayList<ServerThreadForClient>();
	}

	/**
	 * @see #startup()
	 */
	public void startup() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("- Servidor iniciado, esperando conexiones");
			System.out.println("--------------------------------------------");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				ServerThreadForClient thread = new ServerThreadForClient(clientId++, this, clientSocket);
				thread.start();
				conectedClients.add(thread);
			}
		} catch (IOException e) {
			System.err.println("Excepcion producida al escuchar por el puerto " + port + " o escuchando nuevas conexiones.");
		}
	}

	/**
	 * @see #shutdown()
	 */
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

	/**
	 * @see #broadcast(ChatMessage)
	 */
	public synchronized void broadcast(ChatMessage message) {
		ObjectOutputStream out;
		for (ServerThreadForClient client : conectedClients) {
			try {
				if (message.getId() != client.getClientId()) {
					out = client.getOutputStream();
					out.reset();
					out.writeObject(message);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @see #remove(int)
	 */
	public void remove(int id) {
		for (int i = 0; i < conectedClients.size(); i++) {
			if (conectedClients.get(i).getClientId() == id) {
				conectedClients.get(i).interrupt();
				conectedClients.remove(i);
				break;
			}
		}
	}

	/**
	 * Devuelve una instancia del servidor.
	 * @return La instancia del servidor.
	 */
	public static ChatServerImpl getInstance() {
		if (instance == null) {
			instance = new ChatServerImpl();
		}
		return instance;
	}

	/**
	 * @see #getInstance()
	 * @param port el puerto para la conexion.
	 * @return La instancia del servidor.
	 */
	public static ChatServerImpl getInstance(int port) {
		if (instance == null) {
			instance = new ChatServerImpl(port);
		}
		return instance;
	}
	
	/**
	 * Devuelve una lista con los clientes conectados.
	 * @return Lista de clientes conectados.
	 */
	public List<ServerThreadForClient> getConectedClients(){
		return conectedClients;	
	}

	public static void main(String[] args) {
		ChatServerImpl chatServer;

		System.out.println("--------------------------------------------");
		System.out.println("               BIENVENIDO");
		System.out.println("--------------------------------------------");
		System.out.println("- Inicializando Chat");

		if (args.length != 1) {
			System.out.println("- Se usará el puerto por defecto(1500)");
			chatServer = getInstance();
		} else {
			System.out.println("- Se usara el puerto " + args[0]);
			chatServer = getInstance(Integer.parseInt(args[0]));
		}

		System.out.println("- Iniciando el servidor");
		System.out.println("                 ....");

		chatServer.startup();
	}
	
}
