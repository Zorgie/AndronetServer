package server;

import java.io.PrintStream;

public class User {

	private String mUsername;
	private PrintStream mOutput;

	public User(String username, PrintStream output) {
		setUsername(username);
		setOutput(output);
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String mUsername) {
		this.mUsername = mUsername;
	}

	public PrintStream getOutput() {
		return mOutput;
	}

	public void setOutput(PrintStream mOutput) {
		this.mOutput = mOutput;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof User))
			return false;
		User u = (User) o;
		if (u.mUsername.equals(this.mUsername))
			return true;
		return false;

	}
}
