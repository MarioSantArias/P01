package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;

import es.ubu.lsi.common.ChatMessage;

/**
 * Clase encargada de escuchar los mensajes que envia el servidor.
 * @author Felix Nogal
 * @author Mario Santamaria
 *
 */
public class ChatClientListener implements Runnable {

	/** Socket de conexion con el servidor */
	private Socket socket;
	/** HasMap para guardar los usuarios baneados y su correspondiente id */
	private HashMap<Integer, String> baneados;
	/** Flag para indicar si se ha desconectado por un fallo o por orden del usuario */
	private boolean logout = false;

	/**
	 * Constructor de la clase.
	 * @param socket El socket con el que se va a realizar la conexion con el servidor.
	 */
	public ChatClientListener(Socket socket) {
		this.socket = socket;
		this.baneados = new HashMap<Integer, String>();
	}

	@Override
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ChatMessage inputLine;
			while (!((inputLine = (ChatMessage) in.readObject()).equals(null))) {
				switch (inputLine.getType()) {

				case BAN:
					baneados.put(inputLine.getId(), inputLine.getMessage());
					System.out.print("El usuario \"" + baneados.get(inputLine.getId()) + "\" ha sido baneado.\n> ");
					break;

				case UNBAN:
					baneados.remove(inputLine.getId());
					System.out.print("El usuario \"" + inputLine.getMessage()
							+ "\" ha sido eliminado de la lista de baneados.\n> ");
					break;

				case UPDATEBAN:
					if (baneados.containsKey(0)) {
						baneados.put(inputLine.getId(), inputLine.getMessage());
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
			if(!logout){
				System.err.println("Error en la conexión con el servidor.");
			}else{
				System.out.println("Desconectado del servidor.");
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Error al leer el mensaje.");
		} finally {
			try {
				socket.close();
			} catch (IOException e) {}
		}
	}
}
