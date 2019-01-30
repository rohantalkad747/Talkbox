package configurer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;

import browsing.FileSelector;
import browsing.SelectionListener;
import emojiPanel.EmojiSearchPane.EmojiSearchFrame;
import filehandler.FileIO;
import musicplayer.MusicPlayer;
import talkbox.TalkboxConfigurer.BasePanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
public class SetUpPanel extends JPanel implements ActionListener{
	public static final int ROWS = 1;
	public static final int COLS = 6;
	public JPanel buttonPanel;
	public JButton backButton;
	private BasePanel panel;
	
	public SetUpFrame setUpFrame;
	public SetUpButton [] buttons;
	public SetUpPanel(BasePanel panel) {
		this.panel = panel;
		this.setLayout(new BorderLayout());
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(ROWS,COLS));
		
		//Initializing button array
		buttons = new SetUpButton[COLS];
		for(int row = 0;row < ROWS;row++) {
			for(int col = 0;col < COLS;col++) {
				SetUpButton button = new SetUpButton();
				buttons[col] = button;
				button.addActionListener(this);
				buttonPanel.add(button);
			}
		}
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1,2));
		JLabel setupLabel = new JLabel("Button Setup");
		topPanel.add(setupLabel);
		backButton = new JButton("Back to Main Menu");
		backButton.addActionListener(this);
		topPanel.add(backButton);
		this.add(topPanel,BorderLayout.NORTH);
		this.add(buttonPanel,BorderLayout.CENTER);
		
		setUpFrame = new SetUpFrame();
	}
	public void setConfiguration(Configuration config) {
		for(int i = 0;i < buttons.length;i++) {
			buttons[i].setConfiguration(config.buttonConfigs[i]);
		}
	}
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == backButton) {
			panel.showMainMenu();
		}
		else {
			setUpFrame.setVisible(true);
			setUpFrame.openSetupFrame((SetUpButton)event.getSource(),((SetUpButton)event.getSource()).getConfiguration());
			setUpFrame.colorFrame.setVisible(false);
		}
	}
	public class SetUpButton extends JButton{
		private ButtonConfiguration config;
		public SetUpButton() {
		}
		public ButtonConfiguration getConfiguration() {
			return config;
		}
		public void setConfiguration(ButtonConfiguration config) {
			this.config = config;
			adaptToConfig();
		}
		public void adaptToConfig() {
			this.setBackground(config.buttonColor);
			this.setText(config.buttonText);
		}
	}
	/**
	 * Inner class for the pop-up frame that handles the configuration of buttons.
	 * @author jordan
	 * @version Friday January 25th 2019
	 */
	public class SetUpFrame extends JFrame implements WindowListener{
		
		private BasicField nameField;
		private JButton emojiButton;
		private JButton selectSound;
		private JButton playSound;
		private JLabel currentPath;
		private JButton setColor;
		private JButton confirmSetup;
		private JPanel buttonsPanel;
		//Panel for color of button
		private JPanel currentColorPanel;
		//Music player for playing back sound
		private MusicPlayer musicPlayer;
		//Selector frames
		private ColorFrame colorFrame;
		private FileSelector fileSelector;
		private EmojiSearchFrame emojiFrame;
		//Current color, audio file
		private Color currentColor;
		private File currentAudioFile;
		private SetUpButton currentButton;
		private final Color DEFAULT_COLOR =  UIManager.getColor("Button.background");
		public SetUpFrame() {
			//Frame initial values
			super("Setup Button");
			this.addWindowListener(this);
			this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			this.setSize(400,300);
			this.setVisible(false);
			
			//Intializing other frames
			emojiFrame = new EmojiSearchFrame(new EmojiListener());
			colorFrame = new ColorFrame();
			fileSelector = new FileSelector(new OpenListener(),FileSelector.SOUND);
			
			//Getting default color for a jbutton
			currentColor = DEFAULT_COLOR;
			
			//Adding the organizing configuration panel.
			ConfigPanel config = new ConfigPanel();
			this.setContentPane(config);
		}
		public void hideSetupFrame() {
			this.setVisible(false);
		}
		/**
		 * Updates the setup frame based on the button that has just been opened.
		 * @param button
		 * @param config
		 */
		public void openSetupFrame(SetUpButton button,ButtonConfiguration config) {
			this.currentButton = button;
			this.currentColor = config.buttonColor;
			this.currentColorPanel.setBackground(this.currentColor);
			this.currentAudioFile = config.soundFile;
			if(this.currentAudioFile != null) {
				currentPath.setText("Sound Path:"+this.currentAudioFile.getPath());
				musicPlayer = new MusicPlayer(this.currentAudioFile);
			}
			else {
				currentPath.setText("Sound Path:(none)");
				musicPlayer = null;
			}
			nameField.setText(config.buttonText);
		}
		public class OpenListener implements SelectionListener {
			public void onFileSelected(File file) {
				if(FileIO.checkFileFormat(file)) {
					currentAudioFile = file;
					currentPath.setText("Sound Path: " + currentAudioFile.getPath());
					musicPlayer = new MusicPlayer(currentAudioFile);
				}
				else {
					JOptionPane.showMessageDialog(null,"Audio File must be of .wav format");
				}
			}
		}
		public class EmojiListener implements ActionListener{
			public void actionPerformed(ActionEvent event) {
				JButton emojiButton = (JButton)event.getSource();
				nameField.insertTextAtCursor(emojiButton.getText());
			}
		}
		public Point getSetupLocation() {
			return this.getLocation();
		}
		public void setButton(SetUpButton currentButton) {
			this.currentButton = currentButton;
		}
		/*
		 * Window state changed methods
		 */
		public void windowActivated(WindowEvent arg0) {}
		public void windowClosed(WindowEvent event) {}
		public void windowClosing(WindowEvent arg0) {
			colorFrame.setVisible(false);
			emojiFrame.setVisible(false);
		}
		public void windowDeactivated(WindowEvent event) {}
		public void windowDeiconified(WindowEvent event) {}
		public void windowIconified(WindowEvent event) {}
		public void windowOpened(WindowEvent event) {}
		
		
		
		public class ConfigPanel extends JPanel implements ActionListener{
			public ConfigPanel() {
				this.setLayout(new BorderLayout());
				
				//Adding textField and emoji button to the name panel
				JPanel namePanel = new JPanel();
				namePanel.setLayout(new BoxLayout(namePanel,BoxLayout.X_AXIS));
				nameField = new BasicField("Button Text:");
				namePanel.add(nameField);
				
				emojiButton = new JButton("Add Emoji");
				emojiButton.addActionListener(this);
				namePanel.add(emojiButton);
				this.add(namePanel,BorderLayout.NORTH);
				
				
				//Buttons panel for selecting the sound and the color
				buttonsPanel = new JPanel();
				buttonsPanel.setLayout(new GridLayout(4,1));
				
				JPanel soundButtons = new JPanel();
				soundButtons.setLayout(new GridLayout(1,2));
				//Adding a sound selection button and a label displaying the current selection
				selectSound = new JButton("Select Sound");
				selectSound.addActionListener(this);
				soundButtons.add(selectSound);
				//Adding a play sound button
				playSound = new JButton("Play Sound");
				playSound.addActionListener(this);
				soundButtons.add(playSound);
				buttonsPanel.add(soundButtons);
				
				
				currentPath = new JLabel("Sound Path:(none)");
				buttonsPanel.add(currentPath);
				
				//Button for setting the color that the button should have.
				setColor = new JButton("Select Color");
				setColor.addActionListener(this);
				buttonsPanel.add(setColor);
				
				currentColorPanel = new JPanel();
				currentColorPanel.add(new JLabel("Current Color"));
				currentColorPanel.setBackground(currentColor);
				buttonsPanel.add(currentColorPanel);
				
				this.add(buttonsPanel,BorderLayout.CENTER);
				
				confirmSetup = new JButton("Confirm Setup");
				confirmSetup.addActionListener(this);
				this.add(confirmSetup,BorderLayout.SOUTH);
				
			}
			public void actionPerformed(ActionEvent event) {
				if(event.getSource() == setColor) {
					//Bringing up color frame
					colorFrame.setLocation(new Point(getSetupLocation().x+60,getSetupLocation().y+200));
					colorFrame.setVisible(true);
					//Hiding other frames
					emojiFrame.setVisible(false);
				}
				else if(event.getSource() == emojiButton) {
					//Bringing up emoji pane
					emojiFrame.setLocation(new Point(getSetupLocation().x+200,getSetupLocation().y+60));
					emojiFrame.setVisible(true);
					//Hiding other frames
					colorFrame.setVisible(false);
				}
				else if(event.getSource() == selectSound) {
					fileSelector.setVisible(true);
				}
				else if(event.getSource() == playSound) {
					if(musicPlayer != null)musicPlayer.play();
				}
				else if(event.getSource() == confirmSetup) {
					ButtonConfiguration config = new ButtonConfiguration(nameField.getText(),currentColor,currentAudioFile, currentButton.getConfiguration().returnDir());
					currentButton.setConfiguration(config);
					hideSetupFrame();
				}
			}
		}
		/**
		 * Color frame for adding color to the button being configured.
		 * @author jordan
		 */
		public class ColorFrame extends JFrame implements ActionListener{
			public ColorFrame() {
				super("Select Color");
				this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				this.setSize(200,220);
				this.setResizable(false);
				this.setVisible(false);
				this.setLayout(new GridLayout(3,3));
				
				this.add(new ColorButton(Color.red,this));
				this.add(new ColorButton(Color.blue,this));
				this.add(new ColorButton(Color.orange,this));
				this.add(new ColorButton(Color.yellow,this));
				this.add(new ColorButton(Color.pink,this));
				this.add(new ColorButton(Color.green,this));
				this.add(new ColorButton(Color.cyan,this));
				this.add(new ColorButton(Color.white,this));
				this.add(new ColorButton(ButtonConfiguration.DEFAULT_COLOR,this));
			}
			/**
			 * Color button for selecting a particular color from the color frame.
			 * @author jordan
			 */
			public class ColorButton extends JButton{
				private Color thisColor;
				public ColorButton(Color color,ActionListener listener) {
					thisColor = color;
					this.setIcon(createColorButtonIcon(color,60));
					this.addActionListener(listener);
				}
				public Color getColor() {
					return thisColor;
				}
			}
			public void actionPerformed(ActionEvent event) {
				ColorButton button = (ColorButton)event.getSource();
				currentColor = button.getColor();
				this.setVisible(false);
				currentColorPanel.setBackground(currentColor);
			}
		}	
	}
	public static ImageIcon createColorButtonIcon(Color color,int size) {
		BufferedImage image = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRoundRect(0,0,size,size,size / 5,size / 5);
		return new ImageIcon(image);
	}
	
}
