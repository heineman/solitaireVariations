package solitaireClient;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComboBox;

import java.awt.AWTPermission;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.ArrayList;

public class ClientGUI {

	private JFrame frame;
	private File[] jarFiles;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI window = new ClientGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientGUI() {
		//play();
		initialize();
	}

	private void play(File file) {
		String fileName = file.getPath();
		
		ProcessBuilder pb = new ProcessBuilder("java", "-Djava.security.manager", "-Djava.security.policy=SolitairePolicy.policy", "-jar", fileName);		
		pb.inheritIO();
		//pb.redirectErrorStream(true);
		try {
			Process process = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void getFilesInFolder() {
		FilenameFilter jarFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				return lowercaseName.endsWith(".jar");
			}
		};

		File folder = new File("Games");
		jarFiles = folder.listFiles(jarFilter);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		getFilesInFolder();

		frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JComboBox comboBox = new JComboBox(jarFiles);
		
		frame.getContentPane().add(comboBox);

		JButton btn = new JButton("Play");
		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				play((File)comboBox.getSelectedItem());
			}
		});
		frame.getContentPane().add(btn);
	}
}
