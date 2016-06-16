package com.juanhoo;

import java.io.*;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;

public class Main {

    static String DATAPATH =  null;

    public static void main(String[] args) {
        if (args.length < 3) {
            doUsage();
            return;
        }

        int NOFLAG = 0;
        int FILTERFLAG = 1;
        int INPUTFLAG = 2;
        int flag = NOFLAG;
        String inputFileName = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].compareToIgnoreCase("-f") == 0) {
                flag = FILTERFLAG;
                continue;
            } else if (args[i].compareToIgnoreCase("-i") == 0) {
                flag = INPUTFLAG;
                continue;
            }


            if (flag == FILTERFLAG) {
                filterFileList.add(new FilterFileNameID(args[i]));
                continue;
            }

            if (flag == INPUTFLAG) {
                inputFileName = args[i];
                continue;
            }
        }



        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {

            if (envName.contains("ParsePath")) {
                DATAPATH = env.get(envName);
                break;
            }
        }

        //Create filter pattern group
        SmartPatternGroup smartPatternGroup = new SmartPatternGroup();
        System.out.println("Create filter list");
        for (FilterFileNameID filter:filterFileList) {
            try (BufferedReader br = new BufferedReader(new FileReader((DATAPATH == null)?filter.name:DATAPATH+"\\"+filter.name))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.print(".");
                    smartPatternGroup.AddPattern(filter.fileID, line);
                }
                System.out.print("\n");
            } catch (FileNotFoundException e) {
                System.out.println("Please check the filter file "+filter.name+ " exist or not!");
                return;
            } catch (IOException e) {
                System.out.println("Issue happened when reading file " + filter.name);
                return;
            }
        }
        if (inputFileName == null) {
            doUsage();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            System.out.println("Parsing file");
            int progInd = 0;

            while ((line = br.readLine()) != null) {
                smartPatternGroup.ParseLine(line);
                if (progInd++ == 100) {
                    System.out.print(".");
                    progInd = 0;
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("Please check the filter file " + inputFileName + " exist or not!");
        } catch (IOException e) {
            System.out.println("Issue happened when reading file " + inputFileName);
        }

        for (FilterFileNameID filter:filterFileList) {
            String result = smartPatternGroup.GetParseOutputByFileID(filter.fileID);

            try {
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filter.outputName, false)));
                writer.println(result);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private static Vector<FilterFileNameID>  filterFileList = new Vector<>();


     static class FilterFileNameID {
        public static int globalID = 0;
         public String name;
         public int fileID;
         public String outputName;

         public FilterFileNameID(String fileName) {
             fileID = globalID;
             name = fileName;
             outputName = name+"_output.html";
             globalID++;
         }
    }

    private static void doUsage() {
        System.out.println("Please follow below command format: parselog -f filtername1 filtername2 -i filename");
        return;
    }

    private SmartPatternGroup smartPatternGroup = new SmartPatternGroup();

}
