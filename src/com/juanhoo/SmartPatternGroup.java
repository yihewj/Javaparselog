package com.juanhoo;

/**
 * Created by Yi He on 5/23/2016.
 */

import java.io.FilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * Created by a20023 on 1/21/2016.
 */
public class SmartPatternGroup {

    HashMap<Integer, Vector<SmartPattern.ParseResult>> outputMap = new HashMap<>();
    HashMap<Integer, PriorityQueue<SmartPattern.ParseResult>> parseResultByFilterMap = new HashMap<>();  //Priority list

    HashSet<String> catList = new HashSet<>(); //Store the filter's category
    private String logFileType;

    public void setLogFileType(String logFileType) {
        this.logFileType = logFileType;
    }

    class FilterPattern {
        SmartPattern smartPattern;
        int fileID;

        FilterPattern(String filter, int id) {
            smartPattern = new SmartPattern(filter);
            fileID = id;
        }

        String getFilterCat() {
            return smartPattern.cat;
        }
    }

    HashMap<SmartPattern.FILETYPE, Vector<FilterPattern>> filterMap = new HashMap<>();
    {
        filterMap.put(SmartPattern.FILETYPE.CRASH, new Vector<>());
        filterMap.put(SmartPattern.FILETYPE.EVENT, new Vector<>());
        filterMap.put(SmartPattern.FILETYPE.KERNEL, new Vector<>());
        filterMap.put(SmartPattern.FILETYPE.RADIO, new Vector<>());
        filterMap.put(SmartPattern.FILETYPE.MAIN, new Vector<>());
        filterMap.put(SmartPattern.FILETYPE.ALL, new Vector<>());
        filterMap.put(SmartPattern.FILETYPE.SYSTEM, new Vector<>());
    }

    public SmartPatternGroup() {

    }





    public void AddPattern(int fileID, String filter) {
        FilterPattern fltPattern = new FilterPattern(filter,fileID);
        Vector<FilterPattern> fp = filterMap.get(fltPattern.smartPattern.filetype);
        fp.add(fltPattern);
        //Add one filterpattern includes all filter, it help us to smart parse one merge file
        Vector<FilterPattern> fpAll = filterMap.get(SmartPattern.FILETYPE.ALL);
        fpAll.add(fltPattern);
        catList.add(fltPattern.getFilterCat());
    }

    public void ParseLine(String line) throws Exception {
        Vector<FilterPattern> filterVector = filterMap.get(SmartPattern.getFiletype(logFileType));
        //If logFileTupe is a, all filter need run.
        for (FilterPattern filterPattern: filterVector) {
            SmartPattern.ParseResult parseResult = filterPattern.smartPattern.ParseLine(line);
            if (parseResult != null) {
                StoreParseOutput(filterPattern.fileID, parseResult);
                break;
            }
        }
    }

    private void StoreParseOutput(int fileID, SmartPattern.ParseResult parseResult) {
        PriorityQueue<SmartPattern.ParseResult> parseResultsQueue = parseResultByFilterMap.get(fileID);
        if (parseResultsQueue == null) {
            parseResultsQueue = new PriorityQueue<>();
            parseResultByFilterMap.put(fileID, parseResultsQueue);
        }

        parseResultsQueue.add(parseResult);
    }


    public String GetParseOutputByFileID(int fileID) {
        String parseCombineOutput =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.2/jquery.min.js\"></script>\n" +
                        "<script>\n" +
                        "$(document).ready(function(){\n" +
                        "    $(\"input[type='checkbox'][name='cat']\").prop(\"checked\", true);\n" +
                        "    $(\"p[tag='Orignal']\").hide();\n" +
                        "    var showtitle = 'Show Original Log';\n" +
                        "    var hidetitle = 'Hide Orignal Log';\n" +
                        "    $(\"#btnhideshow\").click(function(){        \n" +
                        "        if ($(\"#btnhideshow\").text() == showtitle) {\n" +
                        "            $.each($(\"input[type='checkbox']:checked\"), function() {\n" +
                        "                var value= $(this).prop('value'); \n" +
                        "                $(\"p[cat='\"+value+\"'][tag='Orignal']\").show();  \n" +
                        "            });\n" +
                        "            $(\"#btnhideshow\").html(hidetitle);\n" +
                        "        } else {\n" +
                        "            $(\"p[tag='Orignal']\").hide();\n" +
                        "            $(\"#btnhideshow\").html(showtitle);\n" +
                        "        }                \n" +
                        "    });\n" +
                        "    $(\"input[type='checkbox'][name='cat']\").change(function(){\n" +
                        "        var value= $(this).prop('value');        \n" +
                        "        if (this.checked) { \n" +
                        "            if ($(\"#btnhideshow\").text() == showtitle) { \n" +
                        "                $(\"p[cat='\"+value+\"'][tag='Converted']\").show();    \n" +
                        "            } else {\n" +
                        "                $(\"p[cat='\"+value+\"']\").show();    \n" +
                        "            }\n" +
                        "        } else {\n" +
                        "            $(\"p[cat='\"+value+\"']\").hide();\n" +
                        "        }\n" +
                        "    });\n" +
                        "    \n" +
                        "});\n" +
                        "</script>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<button id='btnhideshow'>Show Original Log</button>\n";

        for (String cat:catList) {
            parseCombineOutput += "<br><input type=\"checkbox\" name =\"cat\" value=\""+cat+"\">"+cat+"\n";
        }
        parseCombineOutput += "<hr>";



        PriorityQueue<SmartPattern.ParseResult> parseResultsQueue = parseResultByFilterMap.get(fileID);
        if (parseResultsQueue == null) {
            return parseCombineOutput;
        }

         while(!parseResultsQueue.isEmpty()) {
             SmartPattern.ParseResult parseResult = parseResultsQueue.poll();
            if (parseResult.convertedLog != null && parseResult.convertedLog.length() != 0) {
                parseCombineOutput += "<p lid=\"" + fileID + "\" cat=\"" + parseResult.cat + "\" tag = \"Converted\">" + parseResult.convertedLog + "</p>\n";
            }
            if (parseResult.orignalLog != null && parseResult.orignalLog.length() != 0) {
                parseCombineOutput += "<p lid=\"" + fileID + "\" cat=\"" + parseResult.cat + "\" tag = \"Orignal\">" + "<font color= \"Gray\">" + parseResult.orignalLog + "</font></p>\n";
            }
        }
        parseCombineOutput += "</body>\n" +
                "</html>\n";
        return parseCombineOutput;
    }

/*    public String GeneratorHtmlFile(int fileID) {

    }*/


}