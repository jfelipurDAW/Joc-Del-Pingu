package config;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class LangConfig {
	
    private static final String YAML_PATH = "src/assets/lang/en_es.yml";
    private static LangConfig instance;
    private Map<String, String> data;

    private LangConfig() {

    }

    private static LangConfig getInstance() {
        if (instance == null) {
            instance = new LangConfig();
        }
        return instance;
    }

    public static void loadLang() {
        getInstance().internalLoad();
    }

    private void internalLoad() {
        Yaml yaml = new Yaml();
        File yamlFile = new File(YAML_PATH);

        try (InputStream inputStream = new FileInputStream(yamlFile)) {
            data = yaml.load(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getLang(Lang lang) { 
        LangConfig config = getInstance();
        
        String value = config.data.get(lang.getKey());
        return value != null ? value : lang.getKey();
    }
}