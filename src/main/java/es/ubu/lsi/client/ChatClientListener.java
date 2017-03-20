package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.HashMap;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientListener implements Runnable {

	private Socket socket;
	private HashMap<Integer, String> baneados;

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
				switch(inputLine.getType()){
				
				case BAN:
					baneados.put(inputLine.getId(), inputLine.getMessage());
					System.out.print("El usuario \"" + baneados.get(inputLine.getId()) + "\" ha sido baneado.\n> ");
					break;
				
				case UNBAN:
					baneados.remove(inputLine.getId());
					System.out.print("El usuario \"" + inputLine.getMessage() + "\" ha sido eliminado de la lista de baneados.\n> ");
					break;
					
				case UPDATEBAN:
					if(baneados.containsKey(0)){
						baneados.put(inputLine.getId(), inputLine.getMessage());
					}
					break; 
					
				case MESSAGE:
					if (!baneados.containsKey(inputLine.getId())) {
						System.out.print(inputLine.getMessage() + "\n> ");
					}
					break;
					
				default:
					System.err.println("Mensaje corrupto.");
					break;
				
				}
				
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.err.println("Error en la conexión con el servidor.");
		}
	}
}
