package config;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public class Lang {

    public String entitySealName;
    public String entityPlayerType;

    public Lang() {

        // Ruta física dins del projecte (relativa a l'arrel del projecte)
        String yamlPath = "src/assets/lang/es_ca.yml";

        File yamlFile = new File(yamlPath);

        try (InputStream inputStream = new FileInputStream(yamlFile)) {
            Yaml yaml = new Yaml();

            // Carrega com Map<String, Object> o directament com Map
            @SuppressWarnings("unchecked")
            Map<String, Object> data = yaml.load(inputStream);

            if (data != null) {
                this.entitySealName = (String) data.get("entity.seal.name");
                this.entityPlayerType = (String) data.get("entity.player.type");  // adapta les claus del teu YAML
                System.out.println("YAML carregat manualment!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readLangDebugg() {
        System.out.println("entityPlayerType: " + this.entityPlayerType);
        System.out.println("entitySealName:   " + this.entitySealName);
    }

}