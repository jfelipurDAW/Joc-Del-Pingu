package model.game;

import java.net.URL;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;


/**
 * Singleton SoundManager to handle background music and sound effects.
 * Requires javafx.media module.
 * Sound files should be placed in src/assets/sounds/
 *
 * Two looping music tracks:
 *   - main_screen_music.wav -> menu / setup / stats screens
 *   - bg_music.wav          -> in-game (game board)
 * The active track switches on context change; the inactive one is fully
 * stopped (and re-seeked to 0) so leaving the game restarts the title
 * music from the beginning.
 *
 * Short SFX (dice / event / bear / seal / snowball) are loaded as
 * {@link AudioClip}s — lighter weight than MediaPlayer and well suited
 * for fire-and-forget playback that can overlap with the music.
 *
 * The singleton pattern is used so controllers across screens share one
 * MediaPlayer per track (otherwise the music would restart and overlap
 * every time a new controller is instantiated).
 */
public class SoundManager {


    /////////////////////////////
    ///      CONSTANTS        ///
    /////////////////////////////

    /** Default music volume (0.0..1.0). 40% keeps it comfortably under SFX. */
    private static final double MUSIC_VOLUME = 0.40;


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    /** Single shared instance — see {@link #getInstance()}. */
    private static SoundManager instance;

    /** Music tracks (looping). */
    private MediaPlayer titleMusicPlayer;
    private MediaPlayer gameMusicPlayer;

    // AudioClips for fast sound effects
    private AudioClip diceSound;
    private AudioClip eventSound;
    private AudioClip bearSound;
    private AudioClip sealSound;
    private AudioClip snowballSound;


    /////////////////////////////
    ///    CONSTRUCTORS       ///
    /////////////////////////////

    /**
     * Private constructor (singleton). Pre-loads every audio resource so the
     * first play() call doesn't pay the I/O / decode cost on the FX thread.
     */
    private SoundManager() {
        // Pre-load sound effects if they exist
        diceSound = loadSound("/assets/sounds/dice.wav");
        eventSound = loadSound("/assets/sounds/event.wav");
        bearSound = loadSound("/assets/sounds/bear.wav");
        sealSound = loadSound("/assets/sounds/seal.wav");
        snowballSound = loadSound("/assets/sounds/snowball.wav");

        // Two looping music tracks — main_screen for menus, bg for the game
        titleMusicPlayer = loadMusic("/assets/sounds/main_screen_music.wav");
        gameMusicPlayer  = loadMusic("/assets/sounds/bg_music.wav");
    }

    /**
     * Lazy singleton accessor. Creating the SoundManager touches JavaFX
     * Media, which requires the JavaFX runtime to be up — that's why we
     * defer instantiation until the first call instead of using a static
     * initializer.
     *
     * @return the shared SoundManager instance
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }


    /////////////////////////////
    ///    LOAD HELPERS       ///
    /////////////////////////////

    /**
     * Loads a short SFX as an {@link AudioClip}. Returns null (and logs a
     * note) if the resource is missing, so missing assets degrade gracefully
     * instead of crashing the game.
     */
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
     * Loads a music track as a looping {@link MediaPlayer} with the default
     * music volume applied. Returns null on failure (caller handles it).
     */
    private MediaPlayer loadMusic(String path) {
        URL url = getClass().getResource(path);
        if (url == null) {
            System.out.println("Music file not found: " + path);
            return null;
        }
        try {
            MediaPlayer mp = new MediaPlayer(new Media(url.toExternalForm()));
            mp.setCycleCount(MediaPlayer.INDEFINITE);
            mp.setVolume(MUSIC_VOLUME);
            return mp;
        } catch (Exception e) {
            System.out.println("Could not initialise music: " + path);
            return null;
        }
    }


    /////////////////////////////
    ///    MUSIC TRACKS       ///
    /////////////////////////////

    /**
     * Switches to the title (main_screen) track. Restarts from the
     * beginning each time so leaving a game makes the menu music
     * "start over" — matches the user-facing UX request.
     */
    public void playTitleMusic() {
        switchMusic(titleMusicPlayer, gameMusicPlayer);
    }

    /** Switches to the in-game (bg_music) track, restarting from zero. */
    public void playGameMusic() {
        switchMusic(gameMusicPlayer, titleMusicPlayer);
    }

    /**
     * Stops one track and (re)starts the other from the beginning. The
     * explicit seek(Duration.ZERO) is what makes the new track always start
     * from frame 0 rather than wherever it was paused, matching the desired
     * "restart" behavior on screen transitions.
     */
    private void switchMusic(MediaPlayer toPlay, MediaPlayer toStop) {
        if (toStop != null && toStop.getStatus() != MediaPlayer.Status.STOPPED) {
            toStop.stop();
        }
        if (toPlay != null) {
            toPlay.seek(Duration.ZERO);
            toPlay.play();
        }
    }

    /**
     * Silences both music tracks (used when leaving the application or
     * temporarily muting).
     */
    public void stopAllMusic() {
        if (titleMusicPlayer != null) titleMusicPlayer.stop();
        if (gameMusicPlayer != null) gameMusicPlayer.stop();
    }


    /////////////////////////////
    ///   SOUND EFFECTS       ///
    /////////////////////////////

    /** Plays the dice-roll SFX (null-safe if the asset is missing). */
    public void playDiceSound() { if (diceSound != null) diceSound.play(); }

    /** Plays the random-event SFX. */
    public void playEventSound() { if (eventSound != null) eventSound.play(); }

    /** Plays the bear SFX. */
    public void playBearSound() { if (bearSound != null) bearSound.play(); }

    /** Plays the seal SFX. */
    public void playSealSound() { if (sealSound != null) sealSound.play(); }

    /** Plays the snowball SFX. */
    public void playSnowballSound() { if (snowballSound != null) snowballSound.play(); }
}
