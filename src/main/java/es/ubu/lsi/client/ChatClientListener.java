package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientListener implements Runnable {
	
	private Socket socket;

	public ChatClientListener(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ChatMessage inputLine;
			while (!((inputLine = (ChatMessage) in.readObject()).equals(null))) {
				System.out.print(">" + inputLine.getMessage() + "\n>");
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
