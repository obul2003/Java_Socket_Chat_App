package chat;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Frame {
	private static final String SERVER_ADDRESS = "localhost";
	private static final int PORT = 12345;
	private TextArea receiveTextArea;
	private TextField sendTextField;
	private PrintWriter out;
	private String username;
	private ArrayList<String> userNamesList; // List to store usernames
	private TextArea userListTextArea; // TextArea to display usernames

	public Client(String username) {
		this.username = username;
		setTitle("Chat Client - " + username);
		setSize(600, 500);
		setLayout(null);
		setResizable(false);

		receiveTextArea = new TextArea();
		receiveTextArea.setEditable(false);
		receiveTextArea.setFont(new Font("Arial", Font.PLAIN, 16));
		add(receiveTextArea);

		sendTextField = new TextField();
		add(sendTextField);

		Button sendButton = new Button("Send");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendMessage();
			}
		});

		userListTextArea = new TextArea();
		userListTextArea.setEditable(false);
		add(userListTextArea);

		Panel panel = new Panel(new BorderLayout());
		panel.add(sendTextField, BorderLayout.CENTER);
		panel.add(sendButton, BorderLayout.EAST);
		add(panel);

		receiveTextArea.setBounds(10, 35, 400, 350);
		panel.setBounds(10, 400, 400, 50);
		userListTextArea.setBounds(420, 35, 150, 415);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		userNamesList = new ArrayList<>(); // Initialize the list

		try {
			Socket socket = new Socket(SERVER_ADDRESS, PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			Thread receiveThread = new Thread(() -> {
				try {
					String message;
					while ((message = in.readLine()) != null) {
						if (message.startsWith("[USERNAME]")) {
							String newUser = message.substring(10); // Extract username
							userNamesList.add(newUser); // Add username to the list
							updateUserListTextArea(); // Update the user list TextArea
						} else {
							receiveMessage(message);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			receiveThread.start();

			// Announce new user to server
			out.println("[USERNAME]" + username); // Send username to the server
		} catch (IOException e) {
			e.printStackTrace();
		}

		setVisible(true);
	}

	private void sendMessage() {
		String message = sendTextField.getText();
		if (!message.isEmpty()) {
			out.println(username + ": " + message);
			sendTextField.setText("");
			receiveTextArea.append(username + ": " + message + "\n");
		}
	}

	private void receiveMessage(String message) {
		receiveTextArea.append(message + "\n");
	}

	// Update the user list TextArea with the current list of usernames
	private void updateUserListTextArea() {
		userListTextArea.setText(""); // Clear existing content
		for (String user : userNamesList) {
			userListTextArea.append(user + "\n");
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java ChatClient <username>");
			System.exit(1);
		}

		new Client(args[0]);
	}
}
