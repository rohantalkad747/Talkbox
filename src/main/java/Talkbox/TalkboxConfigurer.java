package main.java.Talkbox;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.java.Talkbox.browsing.FileSelector;
import main.java.Talkbox.browsing.SelectionListener;
import main.java.Talkbox.configurer.Configuration;
import main.java.Talkbox.configurer.RecordingPanel;
import main.java.Talkbox.configurer.SetUpPanel;
import main.java.Talkbox.filehandler.FileIO;
import main.java.Talkbox.log.LogController;
import main.java.Talkbox.log.TBCLog;

/**
 * Main class for the Talkbox Configuration App
 * @author jordan
 * @version January 31st 2019
 */
public class TalkboxConfigurer {
	/** The main frame for the app **/
	ConfigurerFrame frame;
	/** Base panel that selects between the other supporting panels **/
	BasePanel panel;
	/** The configuration that the configurer is currently setup with. **/
	Configuration config;
	/** Configuration logger */
	private LogController configLog;

	public TalkboxConfigurer() {
		configLog = new LogController(LogController.LogType.CONFIG_LOG, null);
		frame = new ConfigurerFrame();
		panel = new BasePanel();
		frame.setContentPane(panel);
		frame.setVisible(true);
	}

	public class MenuPanel extends JPanel implements ActionListener {
		JButton setUpButtons;
		JButton recordAudio;
		JButton createNew;
		JButton editOld;
		JButton selectExisting;
		JButton simLog;
		FileSelector selector;
		private static final String TITLE = "Talkbox Configurator";
		private final Font TITLE_FONT = new Font("Rockwell", Font.BOLD, 42);
		private final Font BUTTON_FONT = new Font("Rockwell", Font.PLAIN, 26);
		public MenuPanel() {
			this.setLayout(new BorderLayout());
			//Adding title label
			JLabel titleLabel = new JLabel(TITLE);
			titleLabel.setForeground(Color.BLACK);
			titleLabel.setFont(TITLE_FONT);
			titleLabel.setHorizontalAlignment(JLabel.CENTER);
			this.add(titleLabel,BorderLayout.NORTH);
			this.setBackground(Color.LIGHT_GRAY);
			
			
			//Adding buttons to the button panel
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new GridLayout(2, 2));
			
			// Recording Button
			recordAudio = new JButton("Record Audio");
			recordAudio.addActionListener(this);
			recordAudio.setFont(BUTTON_FONT);
			buttonPanel.add(recordAudio);
			
			// Set up button
			setUpButtons = new JButton("Set Up Buttons");
			setUpButtons.addActionListener(this);
			setUpButtons.setFont(BUTTON_FONT);
			buttonPanel.add(setUpButtons);
			// Set it disabled by default
			setUpButtons.setEnabled(false);
			
			// Create new config directory
			createNew = new JButton("Create New Configuration");
			createNew.setFont(BUTTON_FONT);
			createNew.addActionListener(this);
			buttonPanel.add(createNew);
			
			// Select existing config directory
			selectExisting = new JButton("Open Existing Configuration");
			selectExisting.addActionListener(this);
			selectExisting.setFont(BUTTON_FONT);
			buttonPanel.add(selectExisting);
			this.add(buttonPanel,BorderLayout.CENTER);
			selector = new FileSelector(null, FileSelector.DIRECTORY);
			
			//Button for opening the simulator log. 
			simLog = new JButton("View Simulator Logs");
			simLog.addActionListener(this);
			simLog.setFont(BUTTON_FONT);
			simLog.setEnabled(false);
			this.add(simLog, BorderLayout.SOUTH);
		}
		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getSource() == setUpButtons) {
				panel.showSetup();
				configLog.logMessage("Buttons being added");
			} else if (event.getSource() == recordAudio) {
				panel.showRecording();
				configLog.logMessage("Recording panel opened");
			}
			// set pre-existing configration
			else if (event.getSource() == selectExisting) {
				configLog.logMessage("Selecting existing configuration.");
				JOptionPane.showMessageDialog(null,"Please select a TalkboxData Configuration Directory");
				selector.setVisible(true);
				selector.setSelectionListener(new ExistingConfigListener());
			}
			//Create new configuration directory, and use it. 
			else if (event.getSource() == createNew) {
				configLog.logMessage("Creating new configuration.");
				JOptionPane.showMessageDialog(null, 
						"Please select a directory for the TalkboxData Directory to be saved in.");
				selector.setVisible(true);
				selector.setSelectionListener(new NewConfigListener());
			}
			//TODO Rohan fix sim log 
			else if(event.getSource() == simLog) {
				TBCLog loggerApp = new TBCLog(LogController.LogType.SIM_LOG);
				loggerApp.getLogPanel().addLogsFolder(new File(config.getConfigDir() + FileIO.SEP + "logs"));
				loggerApp.setVisible(true);
			}
		}
		/**
		 * Task for telling the FileSelector what to do once an existing Configuration is Selected.
		 * @author jordan
		 */
		public class ExistingConfigListener implements SelectionListener{

			@Override
			public void onFileSelected(File file) {
				config = Configuration.readConfiguration(file);
				// If the configuration was successfully opened
				if (config != null) {
					configLog.logMessage("Previous configuration opened");
					configLog.addLogFolder(new File(config.getConfigDir() + FileIO.SEP + "logs"));
					panel.configureSetup();
					setUpButtons.setEnabled(true);
					simLog.setEnabled(true);
				}
				// Otherwise, show an error message.
				else {
					JOptionPane.showMessageDialog(null,
							"Failed to read a Talkbox Configuration from the selected directory.\n");
					configLog.logMessage("Configuration failed to open.");
				}
				//Set selector to invisible. 
				selector.setVisible(false);
				
			}
			
		}
		/**
		 * Task for telling the FileSelector what to do when a directory for the new configuration has been selected.
		 * @author jordan
		 *
		 */
		public class NewConfigListener implements SelectionListener{
			public void onFileSelected(File file) {
				
				//Create the directory within the dir selected by user.
				config = new Configuration(file.getPath());
				panel.configureSetup();
				configLog.addLogFolder(new File(config.getConfigDir() + FileIO.SEP + "logs"));
				configLog.logMessage("New configuration created.");
				//Adjust enabling of buttons.
				setUpButtons.setEnabled(true);
				//Set selector to invisible. 
				selector.setVisible(false);
				simLog.setEnabled(true);
			}
		}
	}
	

	public class BasePanel extends JPanel {
		CardLayout layout;
		public static final String MENU = "MENU";
		public static final String SETUP = "SETUP";
		public static final String RECORD = "RECORD";
		public MenuPanel menu;
		public SetUpPanel setup;
		public RecordingPanel record;

		public BasePanel() {
			layout = new CardLayout();
			this.setLayout(layout);
			// Adding menu to the base
			menu = new MenuPanel();
			this.add(MENU, menu);

			// Adding recording panel to the base
			record = new RecordingPanel(this, configLog);
			this.add(RECORD, record);

			// Adding setup panel to the base; send it
			// reference of current configurance
			setup = new SetUpPanel(this, configLog);
			this.add(SETUP, setup);
			layout.show(this, MENU);

			this.revalidate();
			this.repaint();
		}

		public void configureSetup() {
			setup.setConfiguration(config);
		}

		public void showSetup() {
			layout.show(this, SETUP);
		}

		public void showMainMenu() {
			layout.show(this, MENU);
		}

		public void showRecording() {
			layout.show(this, RECORD);
		}
	}

	public static void main(String[] args) {
		new TalkboxConfigurer();
	}

	public class ConfigurerFrame extends JFrame {
		public static final int FRAME_X = 1000;
		public static final int FRAME_Y = 700;

		public ConfigurerFrame() {
			super("TalkBox Configurer");
			this.setSize(FRAME_X, FRAME_Y);
			this.setLocation(20, 20);
			this.setResizable(false);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setVisible(false);
		}
	}
}
