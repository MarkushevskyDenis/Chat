package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientDemo {
	public static void main(String[] args) {
		Client client;
		String ip;

		if (args.length != 0) {
			ip = args[0];
		} else {
			ip = "192.168.173.184";
		}

		System.out.println("You are connected to host: " + ip);

		try {
			client = new Client(new Socket(ip, 8080));

			client.consoleThread.join();
			client.socketThread.join();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class Client implements Runnable {
	public Thread consoleThread;
	public Thread socketThread;

	private Socket client = null;
	private BufferedWriter writer = null;
	private BufferedReader reader = null;
	private BufferedReader userInput = null;

	public Client(Socket client) throws IOException {
		this.client = client;
		this.writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.userInput = new BufferedReader(new InputStreamReader(System.in));

		this.consoleThread = new Thread(this, "console");
		this.socketThread = new Thread(this, "socket");

		this.consoleThread.start();
		this.socketThread.start();
	}

	public void run() {
		try {
			try {
				while (true) {
					if (Thread.currentThread().getName().equals("console")) {
						readConsole();
					} else if (Thread.currentThread().getName().equals("socket")) {
						readSocket();
					}
				}
			} finally {
				System.out.println("Fin client");
				if (this.client != null) {
					System.out.println("Fin client close");
					if (this.userInput != null) {
						this.userInput.close();
					}
					if (this.reader != null) {
						this.reader.close();
					}
					if (this.writer != null) {
						this.writer.close();
					}
					this.client.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readConsole() throws IOException {
		String str;

		str = userInput.readLine();
		if (str != null) {
			str += '\n';
			writer.write(str);
			writer.flush();
		}
	}

	private void readSocket() throws IOException {
		String str;

		str = reader.readLine();
		System.out.println(str);
	}
}
