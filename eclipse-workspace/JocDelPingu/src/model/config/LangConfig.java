package model.config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Central runtime helper for the game's localisation system.
 *
 * <p>Translations live in YAML files under {@code /assets/lang/<code>.yml}.
 * On startup the application calls {@link #loadLang()} (defaulting to English)
 * which parses the YAML into a flat {@code key -> translated text} map.
 * The UI then resolves individual strings via {@link #getLang(Lang)} using the
 * type-safe identifiers from {@link Lang}.</p>
 *
 * <p>This class also implements a tiny listener mechanism
 * ({@link #addLanguageChangeListener(Runnable)}) so any panel currently on
 * screen can re-render its text immediately when the user switches language
 * — without us having to know about each panel here.</p>
 *
 * <p>A singleton pattern is used so the loaded map is shared across the JVM
 * and lazily created on first access.</p>
 */
public class LangConfig {


    /////////////////////////////
    ///       FIELDS          ///
    /////////////////////////////

    // Classpath directory where translation YAML files live.
    // Default language file is now "en.yml" (standardized naming)
    private static final String LANG_DIR = "/assets/lang/";
    private static final String DEFAULT_LANG = "en";

    // List of available language codes for the dropdown.
    // The order here also defines the cycle order used by cycleLanguage().
    private static final String[] AVAILABLE_LANGUAGES = {
        "en", "es", "ca", "fr", "pt", "ro", "ar", "ru", "uk", "ff", "jp"
    };

    // Display names for the language dropdown. LinkedHashMap preserves
    // insertion order so the UI list matches AVAILABLE_LANGUAGES.
    private static final Map<String, String> LANGUAGE_DISPLAY_NAMES = new LinkedHashMap<>();
    static {
        LANGUAGE_DISPLAY_NAMES.put("en", "🌍 English");
        LANGUAGE_DISPLAY_NAMES.put("es", "🌍 Español");
        LANGUAGE_DISPLAY_NAMES.put("ca", "🌍 Català");
        LANGUAGE_DISPLAY_NAMES.put("fr", "🌍 Français");
        LANGUAGE_DISPLAY_NAMES.put("pt", "🌍 Português");
        LANGUAGE_DISPLAY_NAMES.put("ro", "🌍 Română");
        LANGUAGE_DISPLAY_NAMES.put("ar", "🌍 العربية");
        LANGUAGE_DISPLAY_NAMES.put("ru", "🌍 Русский");
        LANGUAGE_DISPLAY_NAMES.put("uk", "🌍 Українська");
        // Pulaar (ff) and Japanese (jp) use Latin labels because the bundled
        // pixel fonts do not cover Adlam (U+1E900+) or CJK glyphs.
        LANGUAGE_DISPLAY_NAMES.put("ff", "🌍 Pulaar");
        LANGUAGE_DISPLAY_NAMES.put("jp", "🌍 Japanese");
    }

    // Lazy singleton — instantiated on first getInstance() call.
    private static LangConfig instance;

    // Flat map of dotted-key -> translated string, populated by internalLoad().
    private Map<String, String> data;

    // Tracks the currently loaded language code so cycleLanguage() / getCurrentLang()
    // can report what is active without re-reading the YAML.
    private String currentLang;

    // Registered callbacks invoked after every successful language load.
    // Using Runnable keeps the contract trivial: each panel just refreshes itself.
    private static final java.util.List<Runnable> listeners = new java.util.ArrayList<>();


    /////////////////////////////
    ///    CONSTRUCTORS       ///
    /////////////////////////////

    /**
     * Private constructor — clients must go through {@link #getInstance()}.
     */
    private LangConfig() {

    }


    /////////////////////////////
    ///   STATIC METHODS      ///
    /////////////////////////////

    /**
     * Lazily creates and returns the singleton instance.
     *
     * @return the shared {@link LangConfig}
     */
    private static LangConfig getInstance() {
        if (instance == null) {
            instance = new LangConfig();
        }
        return instance;
    }


    /**
     * Convenience overload that loads the default language ({@code en}).
     * Called once during application bootstrap.
     */
    public static void loadLang() {
        loadLang(DEFAULT_LANG);
    }


    /**
     * Loads the YAML file matching {@code langCode} and notifies all listeners.
     *
     * @param langCode ISO-style language code (e.g. {@code "en"}, {@code "es"})
     */
    public static void loadLang(String langCode) {
        getInstance().internalLoad(langCode);
    }


    /**
     * Registers a callback to be invoked every time the language is reloaded.
     * Used by every screen that displays translatable text so it can refresh
     * its labels without polling.
     *
     * @param listener the callback to invoke after a language change
     */
    public static void addLanguageChangeListener(Runnable listener) {
        listeners.add(listener);
    }


    /**
     * Removes a previously registered listener. Screens should call this when
     * they are torn down to avoid leaking references.
     *
     * @param listener the callback to remove
     */
    public static void removeLanguageChangeListener(Runnable listener) {
        listeners.remove(listener);
    }


    /**
     * Internal helper that fires every registered listener in turn.
     */
    private static void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }


    /**
     * Resolves a {@link Lang} key against the currently loaded translation map.
     *
     * @param lang the typed key to resolve
     * @return the translated string, or the raw key itself when no translation
     *         exists (this makes missing entries visible in the UI rather than
     *         silently rendering empty strings)
     */
    public static String getLang(Lang lang) {
        LangConfig config = getInstance();

        String value = config.data.get(lang.getKey());
        return value != null ? value : lang.getKey();
    }


    /**
     * Returns the currently loaded language code.
     *
     * @return the active language code (e.g. {@code "en"})
     */
    public static String getCurrentLang() {
        return getInstance().currentLang;
    }


    /**
     * Returns the array of available language codes.
     *
     * @return the supported language codes, in dropdown order
     */
    public static String[] getAvailableLanguages() {
        return AVAILABLE_LANGUAGES;
    }


    /**
     * Returns a human-readable display name for a language code.
     * Used by the language dropdown to show friendly labels.
     *
     * @param langCode the language code to look up
     * @return the matching display label, or the raw code if not registered
     */
    public static String getDisplayName(String langCode) {
        return LANGUAGE_DISPLAY_NAMES.getOrDefault(langCode, langCode);
    }


    /**
     * Cycles to the next available language and reloads translations.
     * Used by the single "language" button in the main menu, which rotates
     * through {@link #AVAILABLE_LANGUAGES} on each click.
     *
     * @return the new language code after cycling
     */
    public static String cycleLanguage() {
        String current = getCurrentLang();
        String next = AVAILABLE_LANGUAGES[0]; // default to first

        // Find the current language in the cycle and pick the one after it,
        // wrapping back to index 0 once we reach the end.
        boolean found = false;
        for (int i = 0; i < AVAILABLE_LANGUAGES.length && !found; i++) {
            if (AVAILABLE_LANGUAGES[i].equals(current)) {
                next = AVAILABLE_LANGUAGES[(i + 1) % AVAILABLE_LANGUAGES.length];
                found = true;
            }
        }

        loadLang(next);
        return next;
    }


    /////////////////////////////
    ///   INSTANCE METHODS    ///
    /////////////////////////////

    /**
     * Reads the YAML file for {@code langCode} from the classpath and stores
     * its contents in {@link #data}. If the requested file is missing, falls
     * back to English so the UI is never left with an empty map.
     *
     * @param langCode the language code to load
     */
    private void internalLoad(String langCode) {
        Yaml yaml = new Yaml();
        String path = LANG_DIR + langCode + ".yml";

        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.err.println("YAML file not found: " + path);
                // If the requested language file is missing, try English so
                // the UI keeps working instead of throwing on every lookup.
                if (!langCode.equals(DEFAULT_LANG)) {
                    System.err.println("Falling back to default language: " + DEFAULT_LANG);
                    internalLoad(DEFAULT_LANG);
                }
            } else {
                data = yaml.load(inputStream);
                currentLang = langCode;

                // Notify all registered listeners that language has changed
                // so every open screen can refresh its labels in-place.
                notifyListeners();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
