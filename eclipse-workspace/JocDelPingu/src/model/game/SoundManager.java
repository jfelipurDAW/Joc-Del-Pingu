package model.game;

import java.net.URL;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Singleton SoundManager to handle background music and sound effects.
 * Requires javafx.media module.
 * Sound files should be placed in src/assets/sounds/
 */
public class SoundManager {
    
    private static SoundManager instance;
    private MediaPlayer backgroundMusicPlayer;
    
    // AudioClips for fast sound effects
    private AudioClip diceSound;
    private AudioClip eventSound;
    private AudioClip bearSound;
    private AudioClip sealSound;
    private AudioClip snowballSound;
    
    private SoundManager() {
        // Pre-load sound effects if they exist
        diceSound = loadSound("/assets/sounds/dice.wav");
        eventSound = loadSound("/assets/sounds/event.wav");
        bearSound = loadSound("/assets/sounds/bear.wav");
        sealSound = loadSound("/assets/sounds/seal.wav");
        snowballSound = loadSound("/assets/sounds/snowball.wav");
        
        // Setup background music
        URL bgmUrl = getClass().getResource("/assets/sounds/bg_music.wav");
        if (bgmUrl != null) {
            try {
                Media media = new Media(bgmUrl.toExternalForm());
                backgroundMusicPlayer = new MediaPlayer(media);
                backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
                backgroundMusicPlayer.setVolume(0.3); // Lower volume for BGM
            } catch (Exception e) {
                System.out.println("Could not load background music.");
            }
        }
    }
    
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    private AudioClip loadSound(String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                return new AudioClip(url.toExternalForm());
            }
        } catch (Exception e) {
            System.out.println("Could not load sound: " + path);
        }
        return null;
    }
    
    public void startBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.play();
        }
    }
    
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }
    
    public void playDiceSound() { if (diceSound != null) diceSound.play(); }
    public void playEventSound() { if (eventSound != null) eventSound.play(); }
    public void playBearSound() { if (bearSound != null) bearSound.play(); }
    public void playSealSound() { if (sealSound != null) sealSound.play(); }
    public void playSnowballSound() { if (snowballSound != null) snowballSound.play(); }
}
