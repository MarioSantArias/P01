package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import es.ubu.lsi.common.ChatMessage;

/**
 * Clase encargada de escuchar los mensajes que envia el servidor.
 * 
 * @author Felix Nogal
 * @author Mario Santamaria
 *
 */
public class ChatClientListener implements Runnable {

	/** Socket de conexion con el servidor */
	private Socket socket;
	/** HasMap para guardar los usuarios baneados y su correspondiente id */
	private HashMap<Integer, ArrayList<String>> baneados;
	/**
	 * Flag para indicar si se ha desconectado por un fallo o por orden del
	 * usuario
	 */
	private boolean logout = false;

	/**
	 * Constructor de la clase.
	 * 
	 * @param socket
	 *            El socket con el que se va a realizar la conexion con el
	 *            servidor.
	 */
	public ChatClientListener(Socket socket) {
		this.socket = socket;
		this.baneados = new HashMap<Integer, ArrayList<String>>();
	}

	/**
	 * Metodo run del hilo.
	 */
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ChatMessage inputLine;
			ArrayList<String> usernames;

			while (!((inputLine = (ChatMessage) in.readObject()).equals(null))) {
				switch (inputLine.getType()) {

				case BAN:

					if (inputLine.getId() != 0 || (inputLine.getId() == 0 && !baneados.containsKey(0))) {
						usernames = new ArrayList<String>();
						usernames.add(inputLine.getMessage());
						baneados.put(inputLine.getId(), usernames);
					} else {
						usernames = baneados.get(inputLine.getId());
						usernames.add(inputLine.getMessage());
						baneados.put(inputLine.getId(), usernames);
					}

					System.out.print("El usuario \"" + inputLine.getMessage() + "\" ha sido baneado.\n> ");
					break;

				case UNBAN:

					if (inputLine.getId() != 0) {
						baneados.remove(inputLine.getId());
						System.out.print("El usuario \"" + inputLine.getMessage()
								+ "\" ha sido eliminado de la lista de baneados.\n> ");
					} else {
						usernames = baneados.get(inputLine.getId());
						usernames.remove(inputLine.getMessage());
						baneados.put(inputLine.getId(), usernames);
						System.out.print("El usuario \"" + inputLine.getMessage()
								+ "\" ha sido eliminado de la lista de baneados.\n> ");
					}
					break;

				case UPDATEBAN:
					if (baneados.containsKey(0) && baneados.get(0).contains(inputLine.getMessage())) {
						usernames = baneados.get(0);
						usernames.remove(inputLine.getMessage());
						baneados.put(0, usernames);
						usernames = new ArrayList<String>();
						usernames.add(inputLine.getMessage());
						baneados.put(inputLine.getId(), usernames);
					}
					break;

				case MESSAGE:
					if (!baneados.containsKey(inputLine.getId())) {
						System.out.print(inputLine.getMessage() + "\n> ");
					}
					break;
				case LOGOUT:
					logout = true;
					break;
				default:
					System.err.println("Mensaje corrupto.");
					break;
				}
			}
		} catch (IOException e) {
			if (!logout) {
				System.err.println("Error en la conexión con el servidor.");
			} else {
				System.out.println("Desconectado del servidor.");
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Error al leer el mensaje.");
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}
}
