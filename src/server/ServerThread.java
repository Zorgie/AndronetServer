package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerThread extends Thread {
	private Socket mSocket;
	private ServerMain mParent;
	private BufferedReader mInput;
	private PrintStream mOutput;
	private String mLoggedinUsername;
	private String mUsername;

	public ServerThread(Socket s, ServerMain parent) {
		mParent = parent;
		mSocket = s;
		try {
			mInput = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			mOutput = new PrintStream(mSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Thread spawned!");
	}

	public ServerThread() {

	}

	@Override
	public void run() {
		System.out.println("Thread running!");
		String command = "";
		boolean loggedin = false;
		boolean loggedout = false;
		while (!loggedout) {
			try {
				command = mInput.readLine();
				
				System.out.println("Command received: " + command);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (command.equals("REGISTER")) {
				loggedin = register();
			} else if (command.equals("LOGIN")) {
				loggedin = login();
			} else if (command.equals("USERLIST")) {
				sendUserList();
			} else if (command.equals("SEND") && loggedin) {
				sendMessage();
			} else if (command.equals("LOGOUT")) {
				loggedout = logout();
			}
		}
	}

	private boolean login() {
		boolean loggedin = initiateConnection("LOGIN");
		if (loggedin)
			mParent.addUser(mUsername, mOutput);
		mOutput.println("LOGIN");
		mOutput.println(loggedin);
		mParent.broadcastUserlist();
		return loggedin;
	}

	private boolean register() {
		boolean loggedin = initiateConnection("REGISTER");
		if (loggedin)
			mParent.addUser(mUsername, mOutput);
		mOutput.println("REGISTER");
		mOutput.println(loggedin);
		mParent.broadcastUserlist();
		return loggedin;
	}

	private void sendUserList() {
		mOutput.println("USERLIST");
		mOutput.println(mParent.getUserList());
		mOutput.println("STOPUSERLIST");
	}

	private void sendMessage() {
		String inputMessage;
		try {
			mParent.Broadcast("SEND");
			mParent.Broadcast(this.mLoggedinUsername);
			while (!(inputMessage = mInput.readLine()).equals("STOPSEND")) {
				mParent.Broadcast(inputMessage);
			}
			mParent.Broadcast("STOPSEND");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean logout() {
		mParent.removeUser(mUsername, mOutput);
		try {
			mInput.close();
			mSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Thread shutdown.");
		return true;
	}

	private synchronized boolean initiateConnection(String action) {
		try {
			String username = mInput.readLine();
			String pass = mInput.readLine();
			String hashpass = getHash(pass);
			Driver d = (Driver) Class.forName("org.sqlite.JDBC").newInstance();
			DriverManager.registerDriver(d);
			Connection conn = DriverManager.getConnection("jdbc:sqlite:res/res/database.db");
			Statement stat = conn.createStatement();
			boolean correct = username.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}");
			if (correct) {
				ResultSet rs = stat.executeQuery("SELECT pass FROM users WHERE name='" + username + "';");

				if (action.equals(("LOGIN"))) {
					String storedpass = rs.getString("pass");
					rs.close();
					stat.close();
					if (storedpass == null)
						return false;
					mUsername = username;
					this.mLoggedinUsername = username.split("@")[0];
					return validatePass(hashpass, storedpass);
				} else if (action.equals("REGISTER")) {
					if (!rs.isClosed()) {
						if (rs.next()) {
							if (rs.getString("name").isEmpty() == false) {
								System.out.println(rs.getString("name"));
								rs.close();
								stat.close();
								return false;
							}
						}
					}
					rs.close();
					stat.close();
					stat = conn.createStatement();
					stat.executeUpdate("INSERT INTO users VALUES ('" + username + "', '" + hashpass + "');");
					stat.close();
					this.mLoggedinUsername = username.split("@")[0];
					mUsername = username;
					return true;
				}
			}
			stat.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	private boolean validatePass(String first, String second) {
		return first.equals(second);
	}

	public String getHash(String password) {
		try {
			byte[] bytesOfMessage = password.getBytes("UTF-8");

			MessageDigest md = MessageDigest.getInstance("SHA-512");
			byte[] thedigest = md.digest(bytesOfMessage);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < thedigest.length; i++) {
				sb.append(thedigest[i]);
			}
			String result = sb.toString();
			// result = result.replaceAll("-", "");
			return result;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
