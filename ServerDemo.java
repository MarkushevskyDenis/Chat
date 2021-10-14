package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerDemo {
	public static void main(String[] args) {
		ServerSocket server = null;

		try {
			try {
				server = new ServerSocket(8080);
				System.out.println("Server is started");
				while (true) {
					new MyServerConnection(server.accept());
				}
			} finally {
				if (server != null) {
					System.out.println("Server is closed");
					server.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class MyServerConnection implements Runnable {
	private Thread thread;

	private Socket client;
	private BufferedReader reader = null;
	private BufferedWriter writer = null;

	private static ArrayList<MyServerConnection> connections = new ArrayList<>();
	private static String message;

	MyServerConnection(Socket client) throws IOException {
		this.client = client;
		this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

		connections.add(this);

		this.thread = new Thread(this);
		this.thread.start();
		System.out.println("Client " + this.thread.getName() + " is connected");
	}

	public void run() {
		String header;
		String dummy;
		char[] buffer = new char[100];
		byte method = 0;

		try {
			try {
				while (true) {
					dummy = header = "";

					do {
						dummy = this.reader.readLine();
						if (dummy == null) {
							throw new IOException("client is closed");
						}

						if (method == 0) {
							if (dummy.substring(0, 3).equals("GET")) {
								method = 1;
							} else {
								method = -1;
							}
						}

						header += dummy + "\n";
					} while (!dummy.equals(""));

					System.out.println(header);

					if (method == -1) {
						int length = this.reader.read(buffer);
						message = new String(buffer, 0, length);

						System.out.println(message);
						for (MyServerConnection connection : MyServerConnection.connections) {
							if (this.thread.getName().equals(connection.thread.getName())) {
								header = "HTTP/1.1 200 OK\r\n" + "Server: YarServer/2009-09-09\r\n"
										+ "Access-Control-Allow-Origin: *\r\n" + "Content-Type: application/json\r\n"
										+ "Content-Length: " + message.length() + "\r\n"
										+ "Connection: keep-alive\r\n\r\n";
								connection.writer.write(header + message);
								connection.writer.flush();
								continue;
							}
							connection.writer.write("(" + this.thread.getName() + "). " + header + "\n");
							connection.writer.flush();
						}
					}
				}
			} finally {
				if (this.client != null) {
					connections.remove(this);

					this.client.close();
					System.out.println("Client is closed" + this.thread.getName());

					if (this.reader != null) {
						this.reader.close();
					}

					if (this.writer != null) {
						this.writer.close();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof MyServerConnection)) {
			return false;
		}

		MyServerConnection connection = (MyServerConnection) obj;

		return this.client.equals(connection.client);
	}
}