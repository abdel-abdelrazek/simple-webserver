package edu.mum.waa;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

public class BareBonesHTTPD extends Thread {

	private static final int PortNumber = 8080;

	Socket connectedClient = null;

	public BareBonesHTTPD(Socket client) {
		connectedClient = client;
	}

	public void run() {

		try {
			System.out.println(connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");

			BBHttpRequest httpRequest = getRequest(connectedClient.getInputStream());

			if (httpRequest != null) {
				BBHttpResponse httpResponse = new BBHttpResponse();

				processRequest(httpRequest, httpResponse);

				sendResponse(httpResponse);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processRequest(BBHttpRequest httpRequest, BBHttpResponse httpResponse) {

		String type = httpRequest.getMethod();

		String uri = httpRequest.getUri().substring(1, httpRequest.getUri().length());

		// --- Problem# 1 ------
		// String response = readFile("C:\\root\\" + uri);
		// if (response != null) {
		// httpResponse.setStatusCode(200);
		// httpResponse.setMessage(response.toString());
		// } else {
		//
		// httpResponse.setStatusCode(404);
		// httpResponse.setMessage("File Doesn't Exist");
		// }

		String myClass = "";
		try {

			File configFile = new File("config.properties");

			try {
				FileReader reader = new FileReader(configFile);
				Properties props = new Properties();
				props.load(reader);

				myClass = props.getProperty(uri);
				reader.close();

				if (myClass != null) {
					Class<?> c = Class.forName("edu.mum.waa." + myClass);
					Object t = c.newInstance();

					Method[] allMethods = c.getDeclaredMethods();
					for (Method m : allMethods) {
						String mname = m.getName();

						if (type.equals("GET")) {

							if (mname.equals("doGet")) {

								try {
									m.setAccessible(true);
									Object o = m.invoke(t, httpRequest, httpResponse);

									// Handle any exceptions thrown by method to be invoked.
								} catch (InvocationTargetException x) {
									Throwable cause = x.getCause();
									System.out.println("invocation of %s failed: %s%n" + mname + cause.getMessage());
								}
							}
						}
					}
				}
				else {
					
					httpResponse.setStatusCode(404);
					httpResponse.setMessage("File Doesn't Exist");
				}

			} catch (FileNotFoundException ex) {
				// file does not exist
			} catch (IOException ex) {
				// I/O error
			}

		} catch (

		ClassNotFoundException x) {
			x.printStackTrace();
		} catch (InstantiationException x) {
			x.printStackTrace();
		} catch (IllegalAccessException x) {
			x.printStackTrace();
		}

	}

	private String readFile(String fileName) {

		// This will reference one line at a time
		String line = "";
		String currentLine = null;

		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((currentLine = bufferedReader.readLine()) != null) {
				line += currentLine;
			}

			// Always close files.
			bufferedReader.close();

		} catch (FileNotFoundException ex) {
			System.out.println("File '" + fileName + "' is not exist");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}

		return line;
	}

	private BBHttpRequest getRequest(InputStream inputStream) throws IOException {

		BBHttpRequest httpRequest = new BBHttpRequest();

		BufferedReader fromClient = new BufferedReader(new InputStreamReader(inputStream));

		String headerLine = fromClient.readLine();

		if ((headerLine == null) || (headerLine.isEmpty())) {
			return null;
		}

		System.out.println("The HTTP request is ....");
		System.out.println(headerLine);

		// Header Line
		StringTokenizer tokenizer = new StringTokenizer(headerLine);
		httpRequest.setMethod(tokenizer.nextToken());
		httpRequest.setUri(tokenizer.nextToken());
		httpRequest.setHttpVersion(tokenizer.nextToken());

		// Header Fields and Body
		boolean readingBody = false;
		ArrayList<String> fields = new ArrayList<>();
		ArrayList<String> body = new ArrayList<>();

		while (fromClient.ready()) {

			headerLine = fromClient.readLine();
			System.out.println(headerLine);

			if (!headerLine.isEmpty()) {
				if (readingBody) {
					body.add(headerLine);
				} else {
					fields.add(headerLine);
				}
			} else {
				readingBody = true;
			}
		}
		httpRequest.setFields(fields);
		httpRequest.setMessage(body);
		return httpRequest;
	}

	private void sendResponse(BBHttpResponse response) throws IOException {

		String statusLine = null;
		if (response.getStatusCode() == 200) {
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		} else {
			statusLine = "HTTP/1.1 501 Not Implemented" + "\r\n";
		}

		String serverdetails = "Server: BareBones HTTPServer";
		String contentLengthLine = "Content-Length: " + response.getMessage().length() + "\r\n";
		String contentTypeLine = "Content-Type: " + response.getContentType() + " \r\n";

		try (DataOutputStream toClient = new DataOutputStream(connectedClient.getOutputStream())) {

			toClient.writeBytes(statusLine);
			toClient.writeBytes(serverdetails);
			toClient.writeBytes(contentTypeLine);
			toClient.writeBytes(contentLengthLine);
			toClient.writeBytes("Connection: close\r\n");
			toClient.writeBytes("\r\n");
			toClient.writeBytes(response.getMessage());

		}
	}

	public static void main(String args[]) throws Exception {

		try (ServerSocket server = new ServerSocket(PortNumber, 10, InetAddress.getByName("127.0.0.1"))) {
			System.out.println("Server Started on port " + PortNumber);

			while (true) {
				Socket connected = server.accept();
				(new BareBonesHTTPD(connected)).start();
			}
		}
	}
}
