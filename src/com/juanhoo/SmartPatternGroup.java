package com.juanhoo;

/**
 * Created by Yi He on 5/23/2016.
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 * Created by a20023 on 1/21/2016.
 */
public class SmartPatternGroup {

    HashMap<Integer, Vector<SmartPattern.ParseResult>> outputMap = new HashMap<>();
    HashSet<String> catList = new HashSet<>(); //Store the filter's category

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
    Vector<FilterPattern> filterVector = new Vector<>();

    public SmartPatternGroup() {

    }

    public void AddPattern(int fileID, String filter) {
        FilterPattern fltPattern = new FilterPattern(filter,fileID);
        filterVector.add(fltPattern);
        catList.add(fltPattern.getFilterCat());
    }

    public void ParseLine(String line) {
        for (FilterPattern filterPattern: filterVector) {
            SmartPattern.ParseResult parseResult = filterPattern.smartPattern.ParseLine(line);
            if (parseResult != null) {
                StoreParseOutput(filterPattern.fileID, parseResult);
                break;
            }
        }
    }

    private void StoreParseOutput(int fileID, SmartPattern.ParseResult parseResult) {
        Vector<SmartPattern.ParseResult> parseResultVector = outputMap.get(fileID);
        if (parseResultVector == null) {
            parseResultVector = new Vector<>();
            outputMap.put(fileID, parseResultVector);
        }
        parseResultVector.add(parseResult);
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



        Vector<SmartPattern.ParseResult> parseResultVector = outputMap.get(fileID);
        if (parseResultVector == null) {
            return parseCombineOutput;
        }
        for (SmartPattern.ParseResult parseOutput:parseResultVector) {
            parseCombineOutput += "<p id=\""+ fileID+"\" cat=\""+parseOutput.cat +"\" tag = \"Converted\">" + parseOutput.convertedLog+"</p>\n";
            parseCombineOutput += "<p id=\""+ fileID+"\" cat=\""+parseOutput.cat + "\" tag = \"Orignal\">" +"<font color= \"Gray\">" +parseOutput.orignalLog+"</font></p>\n";
        }
        parseCombineOutput += "</body>\n" +
                "</html>\n";
        return parseCombineOutput;
    }

/*    public String GeneratorHtmlFile(int fileID) {

    }*/


}