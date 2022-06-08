package com.msi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {

    private final String configPath = "testlink-config.json";
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class.getSimpleName());
    public Config config;

    public ConfigManager() {
        config = Config.getInstance();
    }

    public Config getConfig() {
        return config;
    }

    boolean checkConfigFile(){
        return Files.exists(Path.of(configPath));
    }

    void createFile(String pathStr){
        try {
            Files.createFile(Path.of(pathStr));
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    void writeConfig(HashMap<String, HashMap<String, String>> configMap){
        try {
            Files.write(Path.of(configPath), new JSONObject(configMap).toString().getBytes());
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    void writeConfig(JSONObject configJson){
        try {
            Files.write(Path.of(configPath), configJson.toString().getBytes());
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public void saveConfig(){
        writeConfig(config.getConfigMap());
    }

    public HashMap getConfigBack(){
        HashMap<String, String> customFieldsMap = null;
        if (checkConfigFile()) {
            try {
                FileReader reader = new FileReader(configPath);
                JSONParser parser = new JSONParser();
                JSONObject configJson = (JSONObject) parser.parse(reader);

                // TODO: read DEV_KEY

                try {
                    HashMap<String, HashMap<String, String>> config = new Gson().fromJson(configJson.toString()
                            , new TypeToken<HashMap<String, HashMap<String, String>>> (){}.getType());
                    customFieldsMap = config.get("customFields");
                } catch (ClassCastException e){
                    logger.error("can't read config file. please remove it\n" + ExceptionUtils.getStackTrace(e));
                }
                config.setCustomFields(new LinkedHashMap<>(customFieldsMap));
            } catch (IOException | ClassCastException | ParseException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        } else {
            createFile(configPath);
            writeConfig(config.getConfigMap());
            customFieldsMap = config.getCustomFields();
        }

        return customFieldsMap;
    }

    public static class Config {

        static Config config = null;

        private Config() {
        }

        public static Config getInstance(){
            if (config == null){
                config = new Config();
            }
            return config;
        }

        private String DEV_KEY = "";
        private LinkedHashMap<String, String> customFields = new LinkedHashMap<>(Map.ofEntries(
                Map.entry("MP Firmware", "")
                , Map.entry("PP Firmware", "")
                , Map.entry("Main Board FPGA Version", "")
                , Map.entry("Cabin Cam Firmware", "")
                , Map.entry("Display FPGA", "")
                , Map.entry("Front Cam Firmware", "")));
        private final LinkedHashMap<String, HashMap<String, String>> configMap = new LinkedHashMap<>(Map.of("customFields", customFields));

        public String getDEV_KEY() {
            return DEV_KEY;
        }

        public void setCustomFields(LinkedHashMap<String, String> customFields) {
            this.customFields = customFields;
            this.configMap.get("customFields").putAll(customFields);
        }

        public LinkedHashMap<String, HashMap<String, String>> getConfigMap() {
            return configMap;
        }

        public LinkedHashMap<String, String> getCustomFields() {
            return customFields;
        }

        public String getDevKey() {
            return DEV_KEY;
        }

        public void setDevKey(String devKey) {
            this.DEV_KEY = devKey;
        }

        public void loadCustomFields(JSONObject configJson){
            Map customFieldsJson = (Map)configJson.get("customFields");
//            customFields = new LinkedHashMap<String, String>(customFieldsJson);
            customFields.putAll(customFieldsJson);
            logger.info("config loaded from the file");
        }

        public void updateCustomFields(Map<String, String> customFieldsLoc){
            customFields.putAll(customFieldsLoc);
        }
    }

    public static void main(String[] args) {
//        ConfigManager configManager = new ConfigManager();
//        JSONObject configJson = configManager.readConfig();
//        Config.loadCustomFields(configJson);
//        Map<String, String> newValues = new HashMap<>();
//        newValues.put("Front Cam Firmware", "18");
//        newValues.put("Cabin Cam Firmware", "9");
//        Config.updateCustomFields(newValues);
//        assert Config.customFields.size() == 4;


        ConfigManager configManager = new ConfigManager();
        configManager.getConfigBack();

        logger.info("test passed");
    }
}
