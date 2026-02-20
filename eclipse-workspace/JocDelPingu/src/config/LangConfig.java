package config;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class LangConfig {
	
	private String yamlPath = "src/assets/lang/es_ca.yml";
	private static LangConfig instance;

    private Yaml yaml;
    private File yamlFile;
    private Map<String, String> data;

    public void LoadLang() {

    	this.yaml = new Yaml();
        this.yamlFile = new File(yamlPath);

        try (InputStream inputStream = new FileInputStream(yamlFile)) {

            this.data = yaml.load(inputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LangConfig getInstance() {
        if (instance == null) {
            instance = new LangConfig();
        }
        return instance;
    }
    
    public String getLang(Lang lang) {
    	return (String) data.get(lang.getKey()) != null ? data.get(lang.getKey()) : lang.getKey();
    }
}