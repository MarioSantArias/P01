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
				if (!baneados.containsKey(inputLine.getId())) {
					if (!inputLine.getType().toString().equals("BAN")) {
						System.out.print(">" + inputLine.getMessage() + "\n>");
					} else {
						baneados.put(inputLine.getId(), inputLine.getMessage());
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
