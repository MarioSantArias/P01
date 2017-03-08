package es.ubu.lsi.client;

import java.io.BufferedReader;
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
	
	

	public ChatClientImpl(String server, int port, String username){
		this.server = server;
		this.username = username;
		this.port = port;
	}

	public boolean start() {
		
		try {
			socket = new Socket(server, port);
			new ChatClientListener();
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + server);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + server);
			System.exit(1);
		}
	
		return true;
	}

	public void sendMessage(ChatMessage msg) {
		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(msg);

		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + server);
			System.exit(1);
		}
		
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		final int PUERTO = 1500;
		String user;
		String ip;
				
		if(args.length!=2){
			ip = "localhost";
			System.out.println("- Introducir nombre de usuario: ");
			Scanner sc = new Scanner(System.in);
			user = sc.nextLine();
			sc.close();	
		}else{
			ip = args[0];
			user = args[1];
		}
		ChatClientImpl cliente = new ChatClientImpl(ip, PUERTO, user);
		
		
	}
	
}
