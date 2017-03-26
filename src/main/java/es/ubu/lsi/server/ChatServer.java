package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

/**
 * @author Felix Nogal
 * @author Mario Santamaria
 */
public interface ChatServer {

	/**
	 * Se encarga de inicializar el servidor y aceptar las conexiones de los clientes
	 * iniciando un nuevo hilo para cada uno de ellos(ServerThreadForClient).
	 */
	public void startup();

	/**
	 * Apaga el servidor.
	 */
	public void shutdown();

	/**
	 * Realiza un boradcast del mensaje a todos los clientes conectados. 
	 * @param message Mensaje.
	 */
	public void broadcast(ChatMessage message);

	/**
	 * Elimina un cliente de la lista de clientes conectados.
	 * @param id El id del cliente a eleminar.
	 */
	public void remove(int id);
}
