package com.juanhoo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by Yi He on 8/5/2016.
 * This is a sample library project
 */
public class LogFile {
    String fileName;
    String fileType;

    public static void unzipGZFile(String fileName, String outputFileName) {
        byte[] buffer = new byte[1024];

        try {

            GZIPInputStream gzis =
                    new GZIPInputStream(new FileInputStream(fileName));

            FileOutputStream out =
                    new FileOutputStream(outputFileName);

            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzis.close();
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static LogFile createLogFile(String fileName) {
        String pattern = ".*Boot-\\d{9,}_Set-\\d{9,}_Stream-(\\w{1}).txt";
        Matcher match = Pattern.compile(pattern).matcher(fileName);
        if (match.find()) {
            String logType = match.group(1);
            LogFile inputFile = new LogFile();
            inputFile.fileName = fileName;
            inputFile.fileType = logType;
            return inputFile;
        }
        return null;
    }

    private static void scanUnzipFile(String dir) {
        File root = new File(dir);

        for (File file : root.listFiles()) {
            if (file.canRead() != true) {
                return;
            }
            if (file.isDirectory()) {
                scanUnzipFile(file.getPath());
            } else {
                String name = file.getName();
                String fileExtenstion = name.substring(name.lastIndexOf('.') + 1);
                if (fileExtenstion.compareTo("gz") == 0) {
                    String unzipFileName = file.getPath().substring(0, file.getPath().lastIndexOf('.'));
                    LogFile.unzipGZFile(file.getPath(), unzipFileName);
                }
            }
        }
    }

    public static Vector<LogFile> getLogFileList(String dir) {
        File root = new File(dir);
        Vector<LogFile> logFiles = new Vector<>();

        scanUnzipFile(dir);
        //Permission issue
        for (File file : root.listFiles()) {
            if (file.canRead() != true) {
                return logFiles;
            }
            if (file.isDirectory()) {
                getLogFileList(file.getPath());
            } else {
                String name = file.getName();
                String fileExtenstion = name.substring(name.lastIndexOf('.') + 1);
                LogFile logFile ;
                if (fileExtenstion.compareTo("txt") == 0) {
                    logFile = LogFile.createLogFile(file.getPath());
                    if (logFile != null) {
                        logFiles.add(logFile);
                    }
                }

            }
        }
        return logFiles;
    }


}
