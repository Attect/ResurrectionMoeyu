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

import jp.co.a_tm.moeyu.util.Logger;

/**
 * 解密类
 * 负责解密应用中的音频文件
 */
public class Decryption {
    /**
     * 音频文件夹名
     */
    private static final String VOICE = "voice";
    /** XOR解密密钥 */
    private static final int XOR_KEY = 58;
    /** 中文音频文件夹名 */
    private static final String VOICE_CN = "voice_cn";
    /** 上下文 */
    private Context mContext;

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    public Decryption(Context context) {
        this.mContext = context;
    }

    /**
     * 执行解密操作
     * 解密所有音频文件并保存到应用目录
     *
     * @throws IOException IO异常
     */
    public void execute() throws IOException {
        AssetManager manager = this.mContext.getResources().getAssets();
        String[] fileNames = manager.list(VOICE);
        List<String> alreadies = Arrays.asList(this.mContext.fileList());
        if (fileNames != null) {
            for (String fileName : fileNames) {
                String oggName = fileName.replace(".okk", ".ogg");
                if (!alreadies.contains(oggName)) {
                    write(oggName, decrypt(fileName, manager.open(VOICE + File.separator + fileName)));
                }
            }
        }

        String[] cnFileNames = manager.list(VOICE_CN);
        if (cnFileNames != null) {
            for (String fileName : cnFileNames) {
                String oggName = fileName.replace(".okk", "_cn.ogg");
                if (!alreadies.contains(oggName)) {
                    write(oggName, decrypt(fileName, manager.open(VOICE_CN + File.separator + fileName)));
                }
            }
        }
    }

    /**
     * 解密文件内容
     *
     * @param name 文件名
     * @param inputStream 输入流
     * @return 解密后的字节数组
     * @throws IOException IO异常
     */
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
            array[i] = (byte) (array[i] ^ XOR_KEY);
        }
        return array;
    }

    /**
     * 按需解密单个语音文件
     * 如果文件已存在则跳过，否则从 assets 解密并写入内部存储
     *
     * @param voiceName 语音文件名（不含扩展名）
     * @param useCN 是否使用中文语音
     * @return 解密后的 .ogg 文件名
     * @throws IOException IO异常
     */
    public String decryptOnDemand(String voiceName, boolean useCN) throws IOException {
        String oggName = voiceName + (useCN ? "_cn.ogg" : ".ogg");
        if (Arrays.asList(this.mContext.fileList()).contains(oggName)) {
            return oggName;
        }
        String assetPath = (useCN ? VOICE_CN : VOICE) + File.separator + voiceName + ".okk";
        try {
            byte[] decrypted = decrypt(voiceName, this.mContext.getResources().getAssets().open(assetPath));
            write(oggName, decrypted);
            Logger.d("Decryption", "按需解密完成: " + oggName);
        } catch (IOException e) {
            if (useCN) {
                Logger.w("Decryption", "中文语音不存在，回退到日文: " + voiceName);
                return decryptOnDemand(voiceName, false);
            }
            throw e;
        }
        return oggName;
    }

    /**
     * 写入文件
     *
     * @param name 文件名
     * @param buffer 字节缓冲区
     * @throws IOException IO异常
     */
    private void write(String name, byte[] buffer) throws IOException {
        FileOutputStream fileOutputStream = this.mContext.openFileOutput(name, 0);
        fileOutputStream.write(buffer);
        fileOutputStream.flush();
        fileOutputStream.close();
    }
}
