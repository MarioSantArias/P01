package es.ubu.lsi.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.MessageType;

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
		String user, ip, read;
		ChatMessage msg;
		Scanner sc = new Scanner(System.in);
		
		if(args.length!=2){
			ip = "localhost";
			System.out.println("- Introducir nombre de usuario: ");
			user = sc.nextLine();
		}else{
			ip = args[0];
			user = args[1];
		}
		ChatClientImpl cliente = new ChatClientImpl(ip, PUERTO, user);
		
		System.out.println("Se ha conectado como: " + user);
		boolean flagContinue = true;
		{
			read = sc.nextLine();
			if(!(read.equals("logout"))){
				msg = new ChatMessage(cliente.id, ChatMessage.MessageType.MESSAGE, read);		
			}else{
				flagContinue = false;	
			}

		}while(flagContinue);
		
		try {
			cliente.socket.close();
			sc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
