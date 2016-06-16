package com.juanhoo;

/**
 * Created by Yi He on 5/23/2016.
 */

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by a20023 on 12/31/2015.
 */
public class SmartPattern {
    String smartPattern;
    Vector<String> memberDef;
    String outputFormat;
    boolean keepDuplicateRecord = false;
    String htmlProp = "";
    String lastRecord = "";
    String cat ="default";
    PlainWordTranslation plainWorldTranslationInstance;
    public SmartPattern(String filter) {
        memberDef = new Vector<>();
        filter = ExtractProp(filter);
        GeneratePattern(filter);
        plainWorldTranslationInstance = PlainWordTranslation.GetInstance();
    }


    private String ExtractProp(String filter) {
        String pattern = "(.*?)\\{keepdup\\}(.*?)";
        Matcher match = Pattern.compile(pattern).matcher(filter);
        if (match.find()) {
            filter = filter.replaceAll("\\{keepdup\\}", "");
            keepDuplicateRecord = true;
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


    private void GeneratePattern(String filter){

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

    // private String[] testNum = {"test0","test1","test2","test3","test4","test5","test6","test7","test8","test9","test10","test11","test12","test13","test14","test15","test16","test17"};

    public class ParseResult {
        String convertedLog;
        String orignalLog;
        String cat;
    }

    public ParseResult ParseLine(String line) {
        if (outputFormat == null) {
            return null;
        }
        Matcher matcher = Pattern.compile(smartPattern).matcher(line);
        if (matcher.find()) {
            ParseResult parseResult = new ParseResult();
            parseResult.cat = cat;
            String output = "";
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
                            assert(false);
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
