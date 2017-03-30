package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import es.ubu.lsi.common.ChatMessage;

/**
 * 
 * @author Felix Nogal
 * @author Mario Santamaria
 *
 */
public class ServerThreadForClient extends Thread {

	/** Id del cliente */
	private int id;
	/** Nombre de usuario */
	private String username;
	/** Socket para la conexion con el cliente */
	private Socket clientSocket;
	/** Instancia del servidor */
	private ChatServerImpl chatServer;
	/** OutputStream para el envio de mensajes */
	private ObjectOutputStream out;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            Identificador del cliente.
	 * @param chatserver
	 *            Instancia del servidor.
	 * @param clientSocket
	 *            Socket de conexion con el cliente.
	 */
	public ServerThreadForClient(int id, ChatServerImpl chatserver, Socket clientSocket) {
		try {
			this.id = id;
			this.clientSocket = clientSocket;
			this.chatServer = chatserver;
			out = new ObjectOutputStream(this.clientSocket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Error al obtener el OutputStream para el cliente " + username);
		}
	}

	/**
	 * Metodo run del hilo.
	 */
	@Override
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(this.clientSocket.getInputStream());

			ChatMessage inputLine;
			ChatMessage msg;

			inputLine = (ChatMessage) in.readObject();
			username = inputLine.getMessage();
			System.out.println("\nSe ha conectado un cliente:");
			System.out.println("\t- IP: " + clientSocket.getInetAddress().getHostAddress());
			System.out.println("\t- PORT: " + clientSocket.getPort());
			System.out.println("\t- USERNAME: " + username + "\n");
			chatServer.broadcast(new ChatMessage(id, ChatMessage.MessageType.UPDATEBAN, username));
			msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, "\"" + username + "\" se ha conectado.");
			chatServer.broadcast(msg);

			while ((inputLine = (ChatMessage) in.readObject()) != null) {
				System.out.println("--- " + username + " envió: " + inputLine.getMessage());
				// ChatMessage msg;
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
					sendLogoutMsg();
					System.out.println("### " + "El usuario " + username + " quiere desconectarse.");
					msg = new ChatMessage(id, ChatMessage.MessageType.MESSAGE, "\"" + username + "\" se ha desconectado.");
					chatServer.broadcast(msg);
					break;

				default:
					System.out.println("Envio de mensaje inadecuado.");
					break;
				}
			}
		} catch (IOException e) {
			System.out.println("### " + "El usuario " + username + " se ha desconectado.");
		} catch (ClassNotFoundException e) {
			System.err.println("Error al leer el mensaje.");
		} finally {
			try {
				clientSocket.close();
			} catch (IOException e) {
			}
			chatServer.remove(id);
			this.interrupt();
		}
	}

	/**
	 * Metodo auxiliar para enviar informacion de ban y unban.
	 * 
	 * @param msgType
	 *            Tipo de mensaje a enviar.
	 * @param msg
	 *            El mensaje.
	 */
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

	/**
	 * Metodo auxiliar para enviar un mensaje cuando el cliente desea
	 * desconectarse.
	 */
	private void sendLogoutMsg() {
		try {
			ChatMessage msgToSend = new ChatMessage(id, ChatMessage.MessageType.LOGOUT, "logout");
			out.reset();
			out.writeObject(msgToSend);
		} catch (IOException e) {
			System.err.println("Error al enviar mensaje de logout.");
		}
	}

	/**
	 * Devuelve el identificador del cliente.
	 * 
	 * @return El identificador del cliente.
	 */
	public int getClientId() {
		return id;
	}

	/**
	 * Devuelve el socket del cliente.
	 * 
	 * @return el socket del cliente.
	 */
	public Socket getClientSocket() {
		return clientSocket;
	}

	/**
	 * Devuelve el nombre de usuario.
	 * 
	 * @return El nombre de usario.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Devuelve el OutputStream para el envio de mensajes.
	 * 
	 * @return El OutputStream.
	 */
	public ObjectOutputStream getOutputStream() {
		return out;
	}
}
