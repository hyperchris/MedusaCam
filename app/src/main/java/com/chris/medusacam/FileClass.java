package com.chris.medusacam;

import android.os.Environment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Chris on 1/6/15.
 */
public class FileClass {

    public static final int PROCESS_PHOTO = 1;
    public static final int PROCESS_VIDEO = 2;

    /*
     read file: line by lines
      */
    public String readFile(String filePath){
        File tempFile = new File(filePath);
        BufferedReader reader;
        String funcResult = "";
        if (tempFile.exists()){
            try{
                reader = new BufferedReader(new FileReader(tempFile));
                String tempLine;
                if((tempLine = reader.readLine()) != null){
                    // Do something here...
                    funcResult += tempLine;
                }
                reader.close();
            } catch (FileNotFoundException e) {
                MainActivity.transMsg(MainActivity.POST_MSG,"File not found!");
                e.printStackTrace();
            } catch (IOException e) {
                MainActivity.transMsg(MainActivity.POST_MSG,"File reading error!");
                e.printStackTrace();
            }
        }
        else{
            MainActivity.transMsg(MainActivity.POST_MSG,"no such file!");
        }
        return funcResult;
    }

    /*
     append a line to the end of a file
      */
    public void appendFile(String fDir, String fName, String line){
        File fileDir = new File(fDir);

        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        File file = new File(fDir + "/" + fName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            MainActivity.transMsg(MainActivity.POST_MSG,"Cannot create file! \n ---------------------------\n");
            e.printStackTrace();
            return;
        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file,true);
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            bufferWriter.write(line);
            bufferWriter.close();
        } catch (IOException e) {
            MainActivity.transMsg(MainActivity.POST_MSG,"File writing error! \n ---------------------------\n");
            e.printStackTrace();
            return;
        }

        //MainActivity.transMsg(MainActivity.POST_MSG,"metadata stored! \n ---------------------------\n");
    }

    /*
     * move a file from one place to another
     */
    public boolean moveFile(String srcFileName, String dstDirName) {

        File srcFile = new File(srcFileName);
        if(!srcFile.exists() || !srcFile.isFile()) {
            MainActivity.transMsg(MainActivity.POST_MSG, "cannot move file: src file invalid!");
            return false;
        }

        File dstDir = new File(dstDirName);
        if (!dstDir.exists())
            dstDir.mkdirs();

        return srcFile.renameTo(new File(dstDirName + '/' + srcFile.getName()));
    }

    /*
     return the album's dir
      */
    public File getAlbumDir(String albumName) {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStorageDirectory() + MainActivity.CAMERA_DIR + albumName);
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        MainActivity.transMsg(MainActivity.POST_MSG,"failed to create directory");
                        return null;
                    }
                }
            }
        } else{
            MainActivity.transMsg(MainActivity.POST_MSG,"External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    /*
     create the media file
      */
    public File setUpMediaFile (String albumName, int mediaType) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String mediaFileName;
        String prefix, suffix;
        if (mediaType == PROCESS_PHOTO) {
            prefix = MainActivity.JPEG_FILE_PREFIX;
            suffix = MainActivity.JPEG_FILE_SUFFIX;
        }
        else if (mediaType == PROCESS_VIDEO) {
            prefix = MainActivity.MP4_FILE_PREFIX;
            suffix = MainActivity.MP4_FILE_SUFFIX;
        }
        else {
            MainActivity.transMsg(MainActivity.POST_MSG, "cannot create media file - error type!");
            return null;
        }
        mediaFileName = prefix + timeStamp + "_";
        File albumF = getAlbumDir(albumName);
        File mediaFile = File.createTempFile(mediaFileName, suffix, albumF);
        return mediaFile;
    }
}
