package es.ubu.lsi.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientImpl implements ChatClient {
	private String server;
	private String username;
	private int port;
	private boolean carryOn = true;
	private int id;
	private Socket socket;
	private Thread chatClientListenter;

	public ChatClientImpl(String server, int port, String username) {
		this.server = server;
		this.username = username;
		this.port = port;
	}

	public boolean start() {

		try {
			socket = new Socket(server, port);
			// Al conectar con el servidor enviamos el nombre de usuario con el
			// que se ha conectado
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(username);
			
			chatClientListenter = new Thread(new ChatClientListener(socket));
			chatClientListenter.start();
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + server);
			return false;
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + server);
			return false;
		}

		return true;
	}

	public void sendMessage(ChatMessage msg) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(msg.getMessage());
		} catch (IOException e) {
			carryOn = false;
			System.err.println("Couldn't get I/O for the connection to " + server);
			System.exit(1);
		}
	}

	public void disconnect() {
		try {
			chatClientListenter.interrupt();
			socket.close();
			System.exit(1);
		} catch (IOException e) {
			return;
		}
	}

	public boolean getCarryOn() {
		return carryOn;
	}

	public void setCarryOn(boolean carryOn) {
		this.carryOn = carryOn;
	}

	public static void main(String[] args) {
		final int PUERTO = 1500;
		String user, ip, read;
		ChatMessage msg;
		Scanner sc = new Scanner(System.in);

		if (args.length != 2) {
			ip = "localhost";
			System.out.println("- Introducir nombre de usuario: ");
			user = sc.nextLine();
		} else {
			ip = args[0];
			user = args[1];
		}
		ChatClientImpl cliente = new ChatClientImpl(ip, PUERTO, user);

		if (cliente.start()) {
			System.out.println("Se ha conectado como: " + user);
			boolean flagContinue = true;

			while (flagContinue) {
				read = sc.nextLine();
				if (read.equals("logout")) {
					msg = new ChatMessage(cliente.id, ChatMessage.MessageType.LOGOUT, read);
					cliente.sendMessage(msg);
				} else {
					msg = new ChatMessage(cliente.id, ChatMessage.MessageType.MESSAGE, read);
					cliente.sendMessage(msg);
				}
			}
		}

		sc.close();
		cliente.disconnect();

	}
}
