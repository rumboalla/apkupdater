package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import eu.chainfire.libsuperuser.Shell;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class FileUtil
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public boolean inputStreamToFile(
        InputStream input,
        File file
    ) {
        try {
            OutputStream output = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int read;

            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }

            output.flush();
            output.close();
            input.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public boolean installApk(
        String path
    )
        throws Exception
    {
        if (Shell.SU.available()) {
            if(Shell.SU.run("pm install -r " + path) == null) {
                throw new Exception("Error executing pm install.");
            } else {
                return true;
            }
        } else {
            throw new Exception("Root not available.");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public File getRandomFile(
        Context context
    )
        throws  IOException
    {
        File dir = context.getExternalCacheDir();
        if (dir == null) {
//            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                throw new IOException("No external storage.");
//            }

            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            if (dir == null) {
                throw new IOException("Unable to get file.");
            }
        }
        return new File(dir, UUID.randomUUID().toString());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////