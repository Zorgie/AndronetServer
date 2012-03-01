package server;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerMain {
	private ServerSocket incomingConnections;
	private ArrayList<User> users;
	private String welcomeMessage;

	public static void main(String args[]) {
		new ServerMain().Listen();
	}

	public void Listen() {
		welcomeMessage = "Welcome to this fancy chat!";
		users = new ArrayList<User>();
		try {
			this.incomingConnections = new ServerSocket(8081);
		} catch (Exception e) {
			// TODO error handling.
			e.printStackTrace();
		}
		
		while (true) {
			try {
				Socket tempSocket = this.incomingConnections.accept();
				new ServerThread(tempSocket, this).start();
				System.out.println("New connection accepted,attempting to login!");
			} catch (IOException e) {
				// TODO error handling.
				e.printStackTrace();
			}
		}
	}

	public synchronized void broadcastUserlist() {
		String message = "USERLIST\n" + this.getUserList() + "\nSTOPUSERLIST";
		Iterator<User> it = users.iterator();
		while (it.hasNext()) {
			PrintStream out = it.next().getOutput();
			if (out.checkError())
				it.remove();
			else
				out.println(message);
		}
	}

	public synchronized void Broadcast(String message) {
		Iterator<User> it = users.iterator();
		while (it.hasNext()) {
			PrintStream out = it.next().getOutput();
			if (out.checkError())
				it.remove();
			else
				out.println(message);
		}

		System.out.println("Connected streams:" + users.size());
	}

	public void setWelcomeMessage(String newMsg) {
		welcomeMessage = newMsg;
		Broadcast("Welcome message changed to: " + welcomeMessage);
	}

	public void addUser(String user, PrintStream stream) {
		users.add(new User(user, stream));
	}

	public void removeUser(String user, PrintStream stream) {
		users.remove(new User(user, stream));
	}

	public String getUserList() {
		StringBuilder sb = new StringBuilder();
		sb.append("Connected users: " + users.size());
		for (User user : users) {
			sb.append("\n" + user.getUsername());
		}
		return sb.toString();
	}
}
