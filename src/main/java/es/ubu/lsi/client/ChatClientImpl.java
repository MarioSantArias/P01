package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;

/**
 * 
 * @author Felix Nogal
 * @author Mario Santamaria
 *
 */
public class ChatClientImpl implements ChatClient {

	/** Nombre del servidor */
	private String server;
	/** Nombre del usuario */
	private String username;
	/** Puerto para la conexion */
	private int port;
	/** Identifcador del cliente */
	private int id;
	/** Socket de conexion con el servidor */
	private static Socket socket;
	/** Hilo encargado de escuchar los mensajes del servidor */
	private Thread chatClientListenter;
	/** OutputStream para enviar los mensajes al servidor */
	private ObjectOutputStream msgToServer;

	/**
	 * Constructor de la clase.
	 * 
	 * @param server
	 *            nombre del servidor para realizar la conexion.
	 * @param port
	 *            puerto por el que se establecera la conexion.
	 * @param username
	 *            nombre de usuario del cliente.
	 */
	public ChatClientImpl(String server, int port, String username) {
		this.server = server;
		this.username = username;
		this.port = port;
	}

	/**
	 * @see #start()
	 */
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
			System.err.println("No se conoce el servidor: " + server);
			return false;
		} catch (IOException e) {
			System.err.println("No se pudo establecer conexion con el servidor: " + server);
			return false;
		}

		return true;
	}

	/**
	 * @see #sendMessage(ChatMessage)
	 */
	public void sendMessage(ChatMessage msg) {
		try {
			msgToServer.reset();
			msgToServer.writeObject(msg);
		} catch (IOException e) {
			System.err.println("El mensaje no se envio correctamente.");
		}
	}

	/**
	 * @see #disconnect()
	 */
	public void disconnect() {
		try {
			chatClientListenter.interrupt();
			socket.close();
			System.exit(1);
		} catch (IOException e) {
			return;
		}
	}

	/**
	 * Main de la clase.
	 * 
	 * @param args
	 *            Argumentos de entrada(puerto)
	 */
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
			System.out.println("--------------------------------------------");
			System.out.println("               BIENVENIDO");
			System.out.println("--------------------------------------------");
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
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
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
