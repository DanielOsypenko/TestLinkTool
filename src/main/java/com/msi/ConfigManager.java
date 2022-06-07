package com.msi;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
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
    public Config testProductConfig;

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

    public Map processConfig(){
        Map customFieldsMap = null;
        if (checkConfigFile()) {
            try {
                FileReader reader = new FileReader(configPath);
                JSONParser parser = new JSONParser();
                JSONObject configJson = (JSONObject) parser.parse(reader);

                // TODO: read DEV_KEY

                try {
                    HashMap config = new Gson().fromJson(configJson.toString(), HashMap.class);
                    customFieldsMap = (Map)config.get("customFields");
                } catch (ClassCastException e){
                    logger.error("can't read config file. please remove it\n" + ExceptionUtils.getStackTrace(e));
                }
                Config.customFields = new LinkedHashMap<>(customFieldsMap);



            } catch (IOException | ClassCastException | ParseException e) {
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        } else {
            createFile(configPath);
            writeConfig(Config.configMap);
            customFieldsMap = Config.customFields;
        }

        return customFieldsMap;
    }

    public static class Config {
        public static String DEV_KEY = "";
        public static HashMap<String, String> customFields = new LinkedHashMap<>(Map.ofEntries(
                Map.entry("MP Firmware", "")
                , Map.entry("PP Firmware", "")
                , Map.entry("Main Board FPGA Version", "")
                , Map.entry("Cabin Cam Firmware", "")
                , Map.entry("Display FPGA", "")
                , Map.entry("Front Cam Firmware", "")));
        public static HashMap<String, HashMap<String, String>> configMap = new HashMap<>(Map.of("customFields", customFields));

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
//        ConfigManager configManager = new ConfigManager();
//        JSONObject configJson = configManager.readConfig();
//        Config.loadCustomFields(configJson);
//        Map<String, String> newValues = new HashMap<>();
//        newValues.put("Front Cam Firmware", "18");
//        newValues.put("Cabin Cam Firmware", "9");
//        Config.updateCustomFields(newValues);
//        assert Config.customFields.size() == 4;


        ConfigManager configManager = new ConfigManager();
        configManager.processConfig();

        logger.info("test passed");
    }
}
