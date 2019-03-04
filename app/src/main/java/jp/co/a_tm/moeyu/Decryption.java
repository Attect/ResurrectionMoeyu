package jp.co.a_tm.moeyu;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class Decryption {
    private static final String VOICE = "voice";
    private Context mContext;

    public Decryption(Context context) {
        this.mContext = context;
    }

    public void execute() throws IOException {
        AssetManager manager = this.mContext.getResources().getAssets();
        String[] fileNames = manager.list(VOICE);
        List<String> alreadies = Arrays.asList(this.mContext.fileList());
        for (String fileName : fileNames) {
            String oggName = fileName.replace(".okk", ".ogg");
            if (!alreadies.contains(oggName)) {
                write(oggName, decrypt(fileName, manager.open(VOICE + File.separator + fileName)));
            }
        }
    }

    private byte[] decrypt(String name, InputStream inputStream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        byte[] buffer = new byte[512];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        while (-1 != bufferedInputStream.read(buffer)) {
            byteArrayOutputStream.write(buffer);
        }
        byte[] array = byteArrayOutputStream.toByteArray();
        int limit = array.length;
        for (int i = 0; i < limit; i++) {
            array[i] = (byte) (array[i] ^ 58);
        }
        return array;
    }

    private void write(String name, byte[] buffer) throws IOException {
        FileOutputStream fileOutputStream = this.mContext.openFileOutput(name, 0);
        fileOutputStream.write(buffer);
        fileOutputStream.flush();
        fileOutputStream.close();
    }
}
