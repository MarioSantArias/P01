package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * 
 * @author Felix Nogal
 * @author Mario Santamaria
 *
 */
public interface ChatClient {

	/**
	 * Metodo que realiza la conexion con el servidor y arranca el hilo encargado
	 * de escuchar los mensajes que llegan desde el servidor(ChatClientListener).
	 * 
	 * @return True si se ha conectado al servidor y arrancado el hilo correctamente,
	 * False en caso contrario.
	 */
	public boolean start();

	/**
	 * Metodo encargado de enviar un mensaje al servidor.
	 * 
	 * @param msg el mensaje que se quiere enviar.
	 */
	public void sendMessage(ChatMessage msg);

	/**
	 * Metodo encargado de parar la ejecucion del hilo ChatClientListener y
	 * de cerrar la conexion con el servidor.
	 */
	public void disconnect();

}
