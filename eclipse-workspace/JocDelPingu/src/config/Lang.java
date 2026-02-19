package config;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class Lang {
	
	private static Lang instance;

    public String entitySealName;
    public String entityPlayerType;

    public Lang() {

        String yamlPath = "src/assets/lang/es_ca.yml";

        File yamlFile = new File(yamlPath);

        try (InputStream inputStream = new FileInputStream(yamlFile)) {
            Yaml yaml = new Yaml();

            @SuppressWarnings("unchecked")
            Map<String, String> data = yaml.load(inputStream);

            if (data != null) {
                this.entitySealName = (String) data.get("entity.seal.name");
                this.entityPlayerType = (String) data.get("entity.player.type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Lang getInstance() {
        if (instance == null) {
            instance = new Lang();
        }
        return instance;
    }
    
    public String getSealName() {
        return entitySealName != null ? entitySealName : "Seal";
    }

    public String getPlayerType() {
        return entityPlayerType != null ? entityPlayerType : "Penguin";
    }

}