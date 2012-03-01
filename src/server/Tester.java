package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Tester {

	public static void main(String args[]) {
		new Tester(args[0], args[1]);
	}

	private Socket socket;
	private PrintStream output;
	private BufferedReader input;

	public Tester(String username, String pass) {
		try {
			socket = new Socket("localhost", 8081);
			output = new PrintStream(socket.getOutputStream());
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output.println("LOGIN");
			output.println(username);
			output.println(pass);

			System.out.println(input.readLine());
			output.println("LOGOUT");

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Tester(int h) {
		ServerThread st = new ServerThread();
		String hash = st.getHash("hej");
		System.out.println(hash);
	}

}
