package com.example.meinkochbuch.io;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.meinkochbuch.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import lombok.Getter;
public final class ImageDatabase {

    @Getter
    private static ImageDatabase instance;

    public static void init(MainActivity context){
        if(instance != null)return;
        instance = new ImageDatabase(context);
    }

    private final MainActivity main;
    private final File folder;
    private ImageDatabase(MainActivity context){
        this.main = context;
        this.folder = new File(context.getFilesDir(), "recipe_images/");
        if(!folder.exists())folder.mkdir();
    }

    private final String FALL_BACK_FORMAT = "jpg";


    public boolean saveFile(@NotNull Uri imageUri, @NotNull String nameWithoutExtension){
        try {
            InputStream inputStream = main.getContentResolver().openInputStream(imageUri);
            String extension = getFileExtensionFromUri(imageUri);
            File outputFile = new File(folder, nameWithoutExtension+"."+extension);
            if(outputFile.exists())outputFile.delete();
            OutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
            return outputFile.exists();

            //Toast.makeText(this, "Bild gespeichert", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("ImageDatabase", "Couldn't read and copy file "+imageUri+"!",e);
        }
        return false;
    }

    private String getFileExtensionFromUri(Uri uri) {
        String extension = null;

        if (Objects.equals(uri.getScheme(), ContentResolver.SCHEME_CONTENT)) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(main.getContentResolver().getType(uri));
        } else {
            String path = uri.getPath();
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex != -1) {
                extension = path.substring(dotIndex + 1);
            }
        }

        return extension != null ? extension : FALL_BACK_FORMAT;
    }

    public void deleteFile(@NotNull String nameWithoutExtension){
        for(File file : folder.listFiles()){

        }
    }

}
