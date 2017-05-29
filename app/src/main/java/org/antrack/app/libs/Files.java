package org.antrack.app.libs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Files {
    public static void mkdir(String path) {
        new File(path).mkdir();
    }

    public static void mkdirs(String path) {
        new File(path).mkdirs();
    }

    public static void touch(String path) {
        try {
            new File(path).createNewFile();
        } catch (IOException e) {}
    }

    // Create all needed directories for given file
    public static void mkdirsForFile(String FilePath) {
        new File(FilePath.substring(0,FilePath.lastIndexOf("/"))).mkdirs();
    }

    // Delete all data in folder
    public static void deleteDir(String path, boolean delDir) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        if (files != null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteDir(f.getAbsolutePath(), true);
                } else {
                    f.delete();
                }
            }
        }
        if (delDir)
            folder.delete();
    }


    // Read file into String
    public static String readTextFile(String filename) throws IOException {
        File file = new File(filename);
        StringBuilder text = new StringBuilder();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append('\n');
        }
        br.close();

        return text.toString();
    }

    public static ArrayList<String> textFileToArray(String filename) throws IOException {
        ArrayList<String> strings = new ArrayList<>();

        File file = new File(filename);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null) {
            strings.add(line);
        }
        br.close();

        return strings;
    }

    // Empty file contents
    public static void emptyFile(String path) throws IOException {
        RandomAccessFile file = new RandomAccessFile(path, "w");
        file.setLength(0);
        file.close();
    }

    // Write String to file at begin
    public static void writeTextFile(String filename, String text) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
        out.println(text);
        out.close();
    }

    // Add String to file at end
    public static void addLine(String filename, String text) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
        out.println(text);
        out.close();
    }

    // Add line removing first line if exceed limit for lines
    public static void addLineToStack(String path, String string, int limit) throws IOException {
        if (!new File(path).exists() || countLines(path) < limit) {
            addLine(path, string);
        }
        else {
            String s = readTextFile(path);
            s = s.substring(s.indexOf('\n')+1);
            writeTextFile(path, s + string);
        }
    }

    public static PrintWriter writeLines(String filename) throws IOException {
        return new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
    }


    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean endsWithoutNewLine = false;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n')
                        ++count;
                }
                endsWithoutNewLine = (c[readChars - 1] != '\n');
            }
            if(endsWithoutNewLine) {
                ++count;
            }
            return count;
        } finally {
            is.close();
        }
    }

    public static void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
}
