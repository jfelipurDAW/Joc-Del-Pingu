package model.game;

import java.net.URL;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * Singleton SoundManager to handle background music and sound effects.
 * Requires javafx.media module.
 * Sound files should be placed in src/assets/sounds/
 */
public class SoundManager {

    /** Volume used while the user is in menus / setup screens. */
    private static final double MENU_VOLUME = 0.40;
    /** Volume used during a game so dice, bear, seal, etc. can be heard clearly. */
    private static final double GAME_VOLUME = 0.12;
    /** Duration of the smooth volume fade between menu and game. */
    private static final double FADE_MS = 600.0;

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
                backgroundMusicPlayer.setVolume(MENU_VOLUME);
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

    /**
     * Starts the background music if it isn't already playing. Idempotent —
     * safe to call from every screen's initializer (main menu, setup, board).
     */
    public void startBackgroundMusic() {
        if (backgroundMusicPlayer != null
                && backgroundMusicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
            backgroundMusicPlayer.play();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }

    /** Smoothly drops the BGM to game level so SFX can be heard. */
    public void duckToGameVolume() {
        fadeVolumeTo(GAME_VOLUME);
    }

    /** Smoothly restores the BGM to menu level when leaving a game. */
    public void restoreMenuVolume() {
        fadeVolumeTo(MENU_VOLUME);
    }

    private void fadeVolumeTo(double target) {
        if (backgroundMusicPlayer != null) {
            Timeline tl = new Timeline(new KeyFrame(
                Duration.millis(FADE_MS),
                new KeyValue(backgroundMusicPlayer.volumeProperty(), target)
            ));
            tl.play();
        }
    }

    public void playDiceSound() { if (diceSound != null) diceSound.play(); }
    public void playEventSound() { if (eventSound != null) eventSound.play(); }
    public void playBearSound() { if (bearSound != null) bearSound.play(); }
    public void playSealSound() { if (sealSound != null) sealSound.play(); }
    public void playSnowballSound() { if (snowballSound != null) snowballSound.play(); }
}
