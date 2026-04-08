package config;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class LangConfig {
	
    // Default language file is now "en.yml" (standardized naming)
    private static final String LANG_DIR = "/assets/lang/";
    private static final String DEFAULT_LANG = "en";

    // List of available language codes for the dropdown
    private static final String[] AVAILABLE_LANGUAGES = {
        "en", "es", "ca", "fr", "pt", "ro", "ar", "ru", "uk", "ff"
    };

    // Display names for the language dropdown
    private static final Map<String, String> LANGUAGE_DISPLAY_NAMES = new LinkedHashMap<>();
    static {
        LANGUAGE_DISPLAY_NAMES.put("en", "\uD83C\uDF0D English");
        LANGUAGE_DISPLAY_NAMES.put("es", "\uD83C\uDF0D Espa\u00f1ol");
        LANGUAGE_DISPLAY_NAMES.put("ca", "\uD83C\uDF0D Catal\u00e0");
        LANGUAGE_DISPLAY_NAMES.put("fr", "\uD83C\uDF0D Fran\u00e7ais");
        LANGUAGE_DISPLAY_NAMES.put("pt", "\uD83C\uDF0D Portugu\u00eas");
        LANGUAGE_DISPLAY_NAMES.put("ro", "\uD83C\uDF0D Rom\u00e2n\u0103");
        LANGUAGE_DISPLAY_NAMES.put("ar", "\uD83C\uDF0D \u0627\u0644\u0639\u0631\u0628\u064A\u0629");
        LANGUAGE_DISPLAY_NAMES.put("ru", "\uD83C\uDF0D \u0420\u0443\u0441\u0441\u043A\u0438\u0439");
        LANGUAGE_DISPLAY_NAMES.put("uk", "\uD83C\uDF0D \u0423\u043A\u0440\u0430\u0457\u043D\u0441\u044C\u043A\u0430");
        LANGUAGE_DISPLAY_NAMES.put("ff", "\uD83C\uDF0D 𞤆𞤵𞤤𞤢𞥄𞤪");
    }

    private static LangConfig instance;
    private Map<String, String> data;

    // Tracks the currently loaded language code
    private String currentLang;

    private LangConfig() {

    }

    private static LangConfig getInstance() {
        if (instance == null) {
            instance = new LangConfig();
        }
        return instance;
    }

    
    public static void loadLang() {
        loadLang(DEFAULT_LANG);
    }

    
    public static void loadLang(String langCode) {
        getInstance().internalLoad(langCode);
    }

    private void internalLoad(String langCode) {
        Yaml yaml = new Yaml();
        String path = LANG_DIR + langCode + ".yml";

        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.err.println("YAML file not found: " + path);
                // If the requested language file is missing, try English
                if (!langCode.equals(DEFAULT_LANG)) {
                    System.err.println("Falling back to default language: " + DEFAULT_LANG);
                    internalLoad(DEFAULT_LANG);
                }
                return;
            }
            data = yaml.load(inputStream);
            currentLang = langCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getLang(Lang lang) { 
        LangConfig config = getInstance();
        
        String value = config.data.get(lang.getKey());
        return value != null ? value : lang.getKey();
    }

    /**
     * Returns the currently loaded language code.
     */
    public static String getCurrentLang() {
        return getInstance().currentLang;
    }

    /**
     * Returns the array of available language codes.
     */
    public static String[] getAvailableLanguages() {
        return AVAILABLE_LANGUAGES;
    }

    /**
     * Returns a human-readable display name for a language code.
     * Used by the language dropdown to show friendly labels.
     */
    public static String getDisplayName(String langCode) {
        return LANGUAGE_DISPLAY_NAMES.getOrDefault(langCode, langCode);
    }

    /**
     * Cycles to the next available language and reloads translations.
     * Returns the new language code after cycling.
     */
    public static String cycleLanguage() {
        String current = getCurrentLang();
        String next = AVAILABLE_LANGUAGES[0]; // default to first

        for (int i = 0; i < AVAILABLE_LANGUAGES.length; i++) {
            if (AVAILABLE_LANGUAGES[i].equals(current)) {
                next = AVAILABLE_LANGUAGES[(i + 1) % AVAILABLE_LANGUAGES.length];
                break;
            }
        }

        loadLang(next);
        return next;
    }
}