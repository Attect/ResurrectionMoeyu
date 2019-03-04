package jp.co.a_tm.moeyu.live2d;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {
    Context context;

    public FileManager(Context context) {
        this.context = context;
    }

    public boolean exists_resource(String path) {
        try {
            this.context.getAssets().open(path).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public InputStream open_resource(String path) throws IOException {
        return this.context.getAssets().open(path);
    }

    public boolean exists_cache(String path) {
        return new File(this.context.getCacheDir(), path).exists();
    }

    public InputStream open_cache(String path) throws FileNotFoundException {
        return new FileInputStream(new File(this.context.getCacheDir(), path));
    }

    public InputStream open(String path, boolean isCache) throws IOException {
        if (isCache) {
            return open_cache(path);
        }
        return open_resource(path);
    }
}
