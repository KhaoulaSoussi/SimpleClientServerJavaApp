import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CloudServer {
	private static int maxClients = 3;
	private static ArrayList<ClientThread> clients = new ArrayList<>();
	private static ExecutorService executor = Executors.newFixedThreadPool(maxClients);

	public static void main(String[] args) throws Exception {
		try (ServerSocket ss = new ServerSocket(80)) {
			int clientIndex = 0;
			while(true){
				System.out.println("Server waiting...");
				Socket connectionFromClient = ss.accept();
				ClientThread clientThread = new ClientThread(connectionFromClient, clientIndex);
				clients.add(clientThread);
				executor.execute(clientThread);
				clientIndex++;
			}	
		}
	}
}