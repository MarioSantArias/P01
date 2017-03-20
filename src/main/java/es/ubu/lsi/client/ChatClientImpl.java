package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;

public class ChatClientImpl implements ChatClient {

	private String server;
	private String username;
	private int port;
	private int id;
	private static Socket socket;
	private Thread chatClientListenter;
	private ObjectOutputStream msgToServer;

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
			msgToServer = new ObjectOutputStream(socket.getOutputStream());
			sendMessage(new ChatMessage(id, ChatMessage.MessageType.MESSAGE, username));
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
			msgToServer.reset();
			msgToServer.writeObject(msg);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + server);
			System.exit(1);
		}
	}

	public void disconnect() {
		try {
			chatClientListenter.interrupt();
			msgToServer.close();
			socket.close();
			System.exit(1);
		} catch (IOException e) {
			return;
		}
	}

	public static void main(String[] args) {
		final int PUERTO = 1500;
		String user, ip, read;
		Scanner sc = new Scanner(System.in);

		if (args.length != 2) {
			ip = "localhost";
			System.out.println("--------------------------------------------");
			System.out.println("               BIENVENIDO");
			System.out.println("--------------------------------------------");
			System.out.print("Introducir tu nombre de usuario: ");
			user = sc.nextLine();
		} else {
			ip = args[0];
			user = args[1];
		}
		ChatClientImpl cliente = new ChatClientImpl(ip, PUERTO, user);

		if (cliente.start()) {
			System.out.println("Logeado como \"" + user + "\"");
			System.out.println("Comandos para banear, unbanear y salir:");

			System.out.println("\t- BAN + \"username\"");
			System.out.println("\t- UNBAN + \"username\"");
			System.out.println("\t- LOGOUT");
			System.out.println("--------------------------------------------");

			boolean flagContinue = true;

			while (flagContinue) {
				System.out.print("> ");
				read = sc.nextLine();
				// -----------------OPCION LOGAUT---------------------
				if (read.equals("logout")) {
					cliente.sendMessage(new ChatMessage(cliente.id, ChatMessage.MessageType.LOGOUT, read));
					sc.close();
					cliente.disconnect();

					// -----------------OPCION BAN---------------------
				} else if (read.split(" ")[0].equals("ban")) {
					cliente.sendMessage(new ChatMessage(cliente.id, ChatMessage.MessageType.BAN, read.split(" ")[1]));

					// -----------------OPCION UNBAN---------------------
				} else if (read.split(" ")[0].equals("unban")) {
					cliente.sendMessage(new ChatMessage(cliente.id, ChatMessage.MessageType.UNBAN, read.split(" ")[1]));

					// -----------------OPCION MESSAGE---------------------
				} else {
					cliente.sendMessage(new ChatMessage(cliente.id, ChatMessage.MessageType.MESSAGE, read));
				}
			}
		}
	}
}
