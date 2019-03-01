package solitaireClient;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Process;
import java.lang.ProcessBuilder;

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
		System.out.println("Playing: " + file.getName());
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
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnGames = new JMenu("Games");
		menuBar.add(mnGames);
		
		for (File f : jarFiles) {
			JMenuItem m = new JMenuItem(f.getName());
			mnGames.add(m);
			m.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent actionEvent) {
			      play(f);
			    }
			});
		}
	}
}
