package org.redwid.tools;

import java.io.*;
import java.text.DecimalFormat;

/**
 * Created by Redwid
 */
public class ApkSizeAnalyzer {

    private final static String DEX_2_JAR_SCRIPT = "sh tools/dex2jar/dex2jar.sh";
    private final static String AXML_PRINTER_2_SCRIPT = "java -jar tools/axml_printer2/axmlprinter-0.1.7.jar";

    private boolean rootFolder = true;
    private long rootSize = 0;

    private final UnzipUtility unzipUtility = new UnzipUtility();

    public static void main(String[] args) {
        if(args.length == 0) {
            System.err.println("Error no apk file specified!");
            System.err.println("Usage: ApkSizeAnalyzer /path/to/your/apk/file.apk(*.aar)");
            return;
        }
        final ApkSizeAnalyzer apkSizeAnalyzer = new ApkSizeAnalyzer();
        if(apkSizeAnalyzer.unzipApkFile(args[0])) {
            apkSizeAnalyzer.convertBinaryXmlFile("tmp/AndroidManifest.xml");
            //You could convert any xml layout file by using that pattern
            //apkSizeAnalyzer.convertBinaryXmlFile("tmp/res/layout/your_xml_layout.xml");
            apkSizeAnalyzer.dexDecode();
            apkSizeAnalyzer.printThree("tmp/");
        }
        else {

        }
    }

    /**
     * Unzip apk file
     * @param filePath
     * @return - boolean
     */
    protected boolean unzipApkFile(String filePath) {
        System.out.println("unzipApkFile(" + filePath + ")");

        final File apkFile = new File(filePath);
        if(apkFile.exists()) {
            final File tmpDir = deleteAndReCreateDir("tmp/");

            System.out.println("Apk size: " + readableFileSize(apkFile.length()));
            try {
                unzipUtility.unzip(filePath, tmpDir.getAbsolutePath());
                return true;
            } catch (IOException e) {
                System.err.println("Unable to unzip file: " + filePath);
                e.printStackTrace();
            }
        }
        else {
            System.err.println("File is not exist: " + apkFile.getAbsolutePath());
        }
        return false;
    }

    /**
     * Force delete and re create /tmp folder
     * @param filePath - the filePath
     * @return - the File
     */
    private File deleteAndReCreateDir(String filePath) {
        System.out.println("deleteAndReCreateDir(" + filePath + ")");
        final File tmpDir = new File(filePath);
        if(tmpDir.exists()) {
            deleteFile(tmpDir);
            if(tmpDir.exists()) {
                try {
                    final Process process = Runtime.getRuntime().exec("rm -rf " + tmpDir.getAbsolutePath());
                    final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String s;
                    while ((s = stdOut.readLine()) != null) {
                        //System.out.println(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (tmpDir.exists()) {
                    deleteFile(tmpDir);
                }
            }
        }
        tmpDir.mkdir();
        return tmpDir;
    }

    /**
     * Decode dex files
     */
    protected void dexDecode() {
        System.out.println("dexDecode()");
        File tmpDir = new File("tmp/");
        File[] list = tmpDir.listFiles();
        for(File file: list) {
            if(file.getName().endsWith(".dex")) {
                System.out.println("    decoding: " + file.getAbsolutePath());
                try {
                    final Process process = Runtime.getRuntime().exec(DEX_2_JAR_SCRIPT + " " + file.getAbsolutePath());
                    final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String s;
                    while((s = stdOut.readLine())!=null){
                        //System.out.println(s);
                    }
                    deleteFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        list = tmpDir.listFiles();
        for(File file: list) {
            if(file.getName().endsWith("_dex2jar.jar") ||
               file.getName().endsWith("classes.jar")) { //For aar packages
                unzipJar(file);
            }
        }
    }

    private void unzipJar(File file) {
        try {
            unzipUtility.unzip(file.getAbsolutePath(), "tmp/src/");
            deleteFile(file);
        } catch (IOException e) {
            System.err.println("Unable to unzip file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
    }

    /**
     * Convert binary xml file
     * @param fileName - the fileName
     */
    public void convertBinaryXmlFile(final String fileName) {
        System.out.println("convertBinaryXmlFile(" + fileName + ")");
        File file = new File(fileName);
        if(!file.exists()) {
            System.err.println("Can't find file: " + file.getAbsolutePath());
            return;
        }

        BufferedWriter writer = null;
        try {
            final String value = AXML_PRINTER_2_SCRIPT + " " + file.getAbsolutePath();
            //System.out.println("value: " + value);
            final File newFile = new File(file.getParent() + File.separator + addTermToFileName(file, "_out"));
            writer = new BufferedWriter(new FileWriter(newFile));

            final Process process = Runtime.getRuntime().exec(value);
            final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            while ((s = stdOut.readLine()) != null) {
                //System.out.println(s);
                writer.write(s);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Unable to convert binary file: " + fileName);
            e.printStackTrace();
        } finally {
            try {
                if(writer != null) {
                    writer.close();
                }
            } catch (Exception e) {}
        }
    }

    /**
     * Add term to file
     * @param file - the file
     * @param term - the term
     * @return - String
     */
    private String addTermToFileName(File file, String term) {
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');
        if(index != -1) {
            fileName = fileName.substring(0, index) + term + fileName.substring(index);
        }
        return fileName;
    }

    /**
     * Print files tree
     * @param startDir - the startDir
     */
    public void printThree(String startDir) {
        System.out.println("printThree(" + startDir + ")");
        printThree(startDir, 0);
    }

    /**
     * Print files tree
     * @param startDir - the startDir
     * @param tabCounter - the tabCounter
     */
    public void printThree(String startDir, int tabCounter) {

        final File dir = new File(startDir);
        final Values values = new Values();
        if(tabCounter > 0) {
            String tab = "";
            for(int i = 0; i < tabCounter - 1; i++) {
                tab += "    ";
            }
            getCounts(startDir, values);
            System.out.println(tab + dir.getName() + " (" + values.filesCount + ", " + readableFileSize(values.size) + ", " + getPercentage(values.size, rootSize) + ")");
        }
        else {
            getCounts(startDir, values);
            System.out.println("(files, size, %)");
            System.out.println("(" + values.filesCount + ", " + readableFileSize(values.size) + ", 100.0%)");

            if(rootFolder) {
                rootFolder = false;
                rootSize = values.size;
            }
        }

        tabCounter++;

//		if(tabCounter > 3)
//			return;

        final File list[] = dir.listFiles();
        for (File file : list) {
            if(file.isDirectory()) {
                printThree(file.getAbsolutePath(), tabCounter);
            }
        }
    }

    /**
     * Get file counts
     * @param startDir - the startDir
     * @param values - the values
     */
    private void getCounts(String startDir, Values values) {
        final File dir = new File(startDir);
        final File list[] = dir.listFiles();
        for (File file : list) {
            if(file.isDirectory()) {
                getCounts(file.getAbsolutePath(), values);
            }
            else {
                values.filesCount++;
                values.size += file.length();
            }
        }
    }

    /**
     * Get human readable file size
     * @param size - the size
     * @return String
     */
    private String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * Get percentage
     * @param currentSize - the currentSize
     * @param allSize - the allSize
     * @return String
     */
    private String getPercentage(long currentSize, long allSize) {
        double value = (double)currentSize*100/(double)allSize;
        return new DecimalFormat("#0.000").format(value) + "%";
    }

    /**
     * Delete file
     * @param f - file
     */
    private void deleteFile(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteFile(c);
        }
        else {
            f.delete();
        }
    }
}
