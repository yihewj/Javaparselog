package com.juanhoo;

/**
 * Created by Yi He on 5/23/2016.
 */

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a20023 on 12/31/2015.
 */
public class SmartPattern {
    enum FILETYPE {MAIN, KERNEL, EVENT, SYSTEM, RADIO,CRASH,ALL};
    String smartPattern;
    Vector<String> memberDef;
    String outputFormat;
    boolean keepDuplicateRecord = false;
    String htmlProp = "";
    String lastRecord = "";
    boolean onlyKeepOrignal = false;
    String cat ="default";
    FILETYPE filetype = FILETYPE.MAIN;
    PlainWordTranslation plainWorldTranslationInstance;
    public SmartPattern(String filter) {
        memberDef = new Vector<>();
        try {
            filter = ExtractProp(filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        GeneratePattern(filter);
        plainWorldTranslationInstance = PlainWordTranslation.GetInstance();
    }

    private static HashMap<String, FILETYPE> fileTypeMap = new HashMap<>();
    {
        fileTypeMap.put("m", FILETYPE.MAIN);
        fileTypeMap.put("e", FILETYPE.EVENT);
        fileTypeMap.put("s", FILETYPE.SYSTEM);
        fileTypeMap.put("r", FILETYPE.RADIO);
        fileTypeMap.put("k", FILETYPE.KERNEL);
        fileTypeMap.put("c", FILETYPE.KERNEL);
    }



    public static FILETYPE getFiletype(String type) throws Exception {
        FILETYPE filetype = fileTypeMap.get(type);
        if (filetype == null) {
            throw new Exception("Wrong file type filter "+type);
        } else {
            return filetype;
        }
    }


    private String ExtractProp(String filter) throws Exception {
        String pattern = "(.*?)\\{keepdup\\}(.*?)";
        Matcher match = Pattern.compile(pattern).matcher(filter);
        if (match.find()) {
            filter = filter.replaceAll("\\{keepdup\\}", "");
            keepDuplicateRecord = true;
        }
        pattern = "(.*?)\\{type:(.*?)\\}(.*?)";
        match = Pattern.compile(pattern).matcher(filter);
        if (match.find()) {
            filetype = getFiletype(match.group(2));
            filter = filter.replaceAll("\\{type:(.*?)\\}", "");
        }
        pattern = "(.*?)\\{color:(.*?)\\}(.*?)";
        match = Pattern.compile(pattern).matcher(filter);
        if (match.find()) {
            htmlProp += "color= \""+match.group(2) +"\"";
            filter = filter.replaceAll("\\{color:(.*?)\\}", "");
        }
        pattern = "(.*?)\\{cat:(.*?)\\}(.*?)";
        match = Pattern.compile(pattern).matcher(filter);
        if (match.find()) {
            cat = match.group(2);
            filter = filter.replaceAll("\\{cat:(.*?)\\}", "");
        }
        return filter;
    }

    private boolean IsSpecialFilter(String filter) {
        if (filter.contains("{PID=")) {
            return true;
        }
        return false;
    }

    private void GenerateNormalPattern(String filter) {
        String filterPattern = "(.*?)@->(.*?)$";
        Matcher matcher = Pattern.compile(filterPattern).matcher(filter);
        if (matcher.find()) {
            String origPattern = matcher.group(1);
            outputFormat = matcher.group(2);
            String pattern = origPattern;
            pattern = pattern.replace("[", "\\" + "[");
            pattern = pattern.replace("]", "\\" + "]");
            pattern = pattern.replace(")", "\\" + ")");
            pattern = pattern.replace("(", "\\" + "(");
            pattern = pattern.replace("{", "\\" + "{");
            pattern = pattern.replace("}", "\\" + "}");
            pattern = pattern.replaceAll("%%\\w++%%", "(.*)");
            matcher = Pattern.compile(pattern).matcher(origPattern);
            if (matcher.find()) {
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    memberDef.add(i, matcher.group(i).replaceAll("%%", ""));
                }
            }
            pattern = "(^[\\d-]{5})\\s++([\\d:.]{12})\\s++([\\d]{1,5}+)\\s++([\\d]{1,5}+)(.*)" + pattern;
            smartPattern = pattern;
        } else {
            System.out.println("Generate pattern failed: "+ filter);
        }
    }


    private void GeneratePattern(String filter){
        if (IsSpecialFilter(filter)){
            GenerateSpeicalPattern(filter);
        } else {
            GenerateNormalPattern(filter);
        }


    }

    private void GenerateSpeicalPattern(String filter) {
        String filterPattern = "\\{PID=(\\d{1,5}+)\\}";
        Matcher matcher = Pattern.compile(filterPattern).matcher(filter);
        if (matcher.find()) {
            String pid = matcher.group(1);
            smartPattern = "^[\\d-]{5}\\s++[\\d:.]{12}\\s++"+pid+"\\s++[\\d]{1,5}+(.*+)";
            onlyKeepOrignal = true;
        }
    }

    // private String[] testNum = {"test0","test1","test2","test3","test4","test5","test6","test7","test8","test9","test10","test11","test12","test13","test14","test15","test16","test17"};

    public class ParseResult implements Comparable<ParseResult>{ //, Comparator<ParseResult>
        String convertedLog;
        String orignalLog;
        String cat;
        Date time = null;

        private Date getLogTime(ParseResult result) {
            Date logTime = null;
            String pattern =  "^(\\d{1,2})-(\\d{1,2})\\s+(\\d{1,2}):(\\d{1,2}):(\\d{1,2}).(\\d{1,3})\\s+(\\d{1,5})\\s+(\\d{1,5}).*";
            Matcher match = Pattern.compile(pattern).matcher(result.orignalLog);
            if (match.find()) {
                String logTimeString = "2016-" + match.group(1) + "-" + match.group(2) + " " + match.group(3) + ":" + match.group(4) + ":" + match.group(5) + "." + match.group(6);
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
                try {
                    logTime = format.parse(logTimeString);
                } catch (ParseException e) {
                    System.out.println(result.orignalLog);
                    e.printStackTrace();
                }
            }
            return logTime;
        }



        @Override
        public int compareTo(ParseResult compareLog) {
            if (time == null) {
                time = getLogTime(this);
            }
            if (compareLog.time == null) {
                compareLog.time = getLogTime(compareLog);
            }

            if (time.getTime() < compareLog.time.getTime()) {
                return -1;
            }

            if (time.getTime() > compareLog.time.getTime()) {
                return 1;
            }
            return 0;
        }

       /* @Override
        public int compare(ParseResult result1, ParseResult result2) {
            if (result1.time == null) {
                result1.time = getLogTime(result1);
            }
            if (result2.time == null) {
                result2.time = getLogTime(result2);
            }

            if (result1.time.getTime() < result2.time.getTime()) {
                return -1;
            }

            if (result1.time.getTime() > result2.time.getTime()) {
                return 1;
            }
            return 0;
        }*/
    }
    String PIDFilter = "^[\\d-]{5}\\s++[\\d:.]{12}\\s++1838\\s++[\\d]{1,5}+(.*+)";

    public ParseResult ParseLine(String line) {
        if (outputFormat == null && onlyKeepOrignal != true) {  //Keep Orignal output doesn't need output
            return null;
        }
        Matcher matcher = Pattern.compile(smartPattern).matcher(line);
        if (matcher.find() ) {

            ParseResult parseResult = new ParseResult();
            parseResult.cat = cat;
            String output = "";
            if (onlyKeepOrignal) {
                parseResult.convertedLog = line;
                if (htmlProp.length() != 0){
                    parseResult.convertedLog = "<font "+htmlProp + ">" + parseResult.convertedLog +"</font>";
                }
                return parseResult;
            }
            int charIndex = 0;
            int formatLength = outputFormat.length();
            while (charIndex < formatLength) {
                char c = outputFormat.charAt(charIndex);
                if (c != '$') {
                    output += c;
                    charIndex++;
                } else {
                    String numberStr = "";
                    charIndex++;
                    while (true) {
                        c = outputFormat.charAt(charIndex);
                        if (c <= '9' && c >= '0') {
                            numberStr += c;
                            charIndex++;
                            if (charIndex == formatLength) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                    if (numberStr.length() > 0) {
                        int number = Integer.parseInt(numberStr);
                        String convertInfo = plainWorldTranslationInstance.GetEnumNameByValue(memberDef.get(number), matcher.group(number+5));
                        if (convertInfo == null) {
                            return null;
                        }
                        output += " " + convertInfo;
                    }
                }
            }



            if (!keepDuplicateRecord) {
                if (lastRecord.equalsIgnoreCase(output)) {
                    return null;
                }
            }
            lastRecord = output;

            output = matcher.group(1) + " " +matcher.group(2)+ " " + output;

            if (htmlProp.length() != 0){
                output = "<font "+htmlProp + ">" + output +"</font>";
            }
            parseResult.convertedLog = output;
            parseResult.orignalLog = line;
            return parseResult;
        }
        return null;
    }
}
