package com.msi;

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

    JSONObject config = new JSONObject();
    private final String configPath = "testlink-config.json";
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class.getSimpleName());

    public ConfigManager() {
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

    void writeConfig(){
        try {
            Files.write(Path.of(configPath), config.toString().getBytes());
        } catch (IOException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    JSONObject readConfig(){
        JSONObject config = null;
        try {
            FileReader reader = new FileReader(configPath);
            JSONParser parser = new JSONParser();
            config = (JSONObject) parser.parse(reader);
        } catch (IOException | ClassCastException | ParseException e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return config;
    }

    static class Config {
        static String DEV_KEY = "";
        static HashMap<String, String> customFields = new LinkedHashMap<>(Map.ofEntries(
                Map.entry("MP Firmware", "")
                , Map.entry("PP Firmware", "")
                , Map.entry("Main Board FPGA Version", "")
                , Map.entry("Cabin Cam Firmware", "")
                , Map.entry("Display FPGA", "")
                , Map.entry("Front Cam Firmware", "")));

        public static String getDevKey() {
            return DEV_KEY;
        }

        public static void setDevKey(String devKey) {
            Config.DEV_KEY = devKey;
        }


        static void loadCustomFields(JSONObject configJson){
            Map customFieldsJson = (Map)configJson.get("customFields");
            customFields = new LinkedHashMap<String, String>(customFieldsJson);
            logger.info("config loaded from the file");
        }

        static void updateCustomFields(Map<String, String> customFieldsLoc){
            customFields.putAll(customFieldsLoc);
        }
    }

    public static void main(String[] args) {
        ConfigManager configManager = new ConfigManager();
        JSONObject configJson = configManager.readConfig();
        Config.loadCustomFields(configJson);
        Map<String, String> newValues = new HashMap<>();
        newValues.put("Front Cam Firmware", "18");
        newValues.put("Cabin Cam Firmware", "9");
        Config.updateCustomFields(newValues);
        assert Config.customFields.size() == 4;

        logger.info("test passed");
    }

}
