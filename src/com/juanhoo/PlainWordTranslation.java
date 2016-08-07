package com.juanhoo;

import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlainWordTranslation {

    static private PlainWordTranslation instance;
    private static HashMap<String, EnumValueMap> enumVarMap = new HashMap<>();
    private static EnumValueMap enumValueMap;



    public class EnumValueMap {
        private HashMap<String, String> map;

        EnumValueMap() {
            map = new HashMap<>();
        }

        String get(String key) {
            String value = map.get(key);
            if (value == null) {
                value = "No Defined";
            }
            return value;
        }

        void put(String key, String value) {
            map.put(key, value);
        }
    }

    static public PlainWordTranslation GetInstance() {
        if (instance == null) {
            synchronized (PlainWordTranslation.class) {
                if (instance == null) {
                    instance = new PlainWordTranslation();
                    instance.GenerateDataFromEnumFile();
                }
            }
        }
        return instance;
    }

    private void GenerateDataFromEnumFile() {

        String fileName = (Main.DATAPATH==null)?"parameters.txt": Main.DATAPATH+"\\"+ "parameters.txt";
        File file = new File (fileName);

        if (!file.exists()) {
            System.out.println("Cannot find file " + fileName);
        }
        String newSection = "typedef enum {";
        String enumValueNamePattern = "\\s++(.*?)\\s++=\\s++(\\d{1,8}+).*?";
        String enumVariableNamePattern = "\\s*?}\\s*?(\\S*?);\\s*?";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(newSection)) {
                    enumValueMap = new EnumValueMap();
                    continue;
                }
                Matcher match = Pattern.compile(enumVariableNamePattern).matcher(line);
                if (match.find()) {
                    enumVarMap.put(match.group(1), enumValueMap);
                    continue;
                }
                match = Pattern.compile(enumValueNamePattern).matcher(line);
                if (match.find()) {
                    enumValueMap.put(match.group(2), match.group(1));
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String GetEnumNameByValue(String varName, String value) {
        if (varName.equalsIgnoreCase("keepOrignal")) {
            return value;
        }


        String pattern = "^LargeThan(\\d++)";
        Matcher match = Pattern.compile(pattern).matcher(varName);
        if (match.find()) {
            String limit = match.group(1);
            if (Float.parseFloat(value) > Float.parseFloat(limit)) {
                return value;
            } else {
                return null;
            }
        }
        pattern = "^LessThan(\\d++)";
        match = Pattern.compile(pattern).matcher(varName);
        if (match.find()) {
            String limit = match.group(1);
            if (Float.parseFloat(value) < Float.parseFloat(limit)) {
                return value;
            } else {
                return null;
            }
        }

        EnumValueMap enumNameValueMap = enumVarMap.get(varName);
        if (enumNameValueMap != null) {
            return enumNameValueMap.get(value);
        }

        return null;
    }

}