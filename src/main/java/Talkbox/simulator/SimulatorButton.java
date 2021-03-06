package main.java.Talkbox.simulator;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import main.java.Talkbox.configurer.ButtonConfiguration;
import main.java.Talkbox.log.LogController;
import main.java.Talkbox.musicplayer.MusicPlayer;

/**
 * Simulator button for the talkbox simulator app
 * 
 * @author jordan
 * @version January 31st 2019
 */
public class SimulatorButton extends JButton implements ActionListener {
	private MusicPlayer player;
	private ButtonConfiguration config;
	private SimulatorPanel panel;
	private LogController simLogger;
	private static final Font BUTTON_FONT = new Font("Rockwell",Font.BOLD,20);
	public SimulatorButton(SimulatorPanel panel, LogController simLogger) {
		super("");
		this.setFont(BUTTON_FONT);
		this.panel = panel;
		this.simLogger = simLogger;
	}
	/**
	 * Sets the configuration for the button.
	 * @param config
	 */
	public void setConfiguration(ButtonConfiguration config) {
		this.config = config;
		// Adjusting the text to use html that way the body linewraps
		String adjustedText = String.format("<html><body>%s</body></html>", config.getButtonText());
		this.setText(adjustedText);
		this.setBackground(config.getButtonColor());
		File soundFile = config.getSoundFile();
		if (soundFile != null) {
			player = new MusicPlayer(config.getSoundFile());
		} else {
			player = null;
		}
		File imageFile = config.getImageFile();
		if(imageFile != null) {
			try {
				BufferedImage image = ImageIO.read(imageFile);
				this.setIcon(new ImageIcon(image));
				this.setVerticalTextPosition(SwingConstants.BOTTOM);
				this.setHorizontalTextPosition(SwingConstants.CENTER);
			}
			catch(Exception e) {
				setIcon(null);
			}
		}
		else {
			setIcon(null);
		}
		this.addActionListener(this);
		this.simLogger.logMessage("Set configuration for " + config.getButtonText());
	}
	public boolean isPlaying() {
		if(player == null) {
			return false;
		}
		else {
			return player.isPlaying();
		}
	}
	public void stop() {
		if(player == null) {
			return;
		}
		player.stop();
		player.reset();
	}
	@Override
	public void actionPerformed(ActionEvent event) {
		this.simLogger.logMessage("Sound button pressed.");
		// If there is no soundplayer, do nothing
		if (player == null) {
			return;
		}
		//Stop simulator panel music
		this.simLogger.logMessage("Sound stopped.");
		panel.stopMusic();
		if (player.isPlaying()) {
			return;
		} 
		else {
			this.simLogger.logMessage("Sound button pressed.");
			player.play();
		}
	}

}
