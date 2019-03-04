package jp.co.a_tm.moeyu;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CSV {
    public ArrayList<VoiceTitle> loadVoice(Context context) {
        ArrayList<VoiceTitle> list = new ArrayList();
        InputStreamReader in = new InputStreamReader(context.getResources().openRawResource(R.raw.voice));
        BufferedReader br = new BufferedReader(in);
        while (true) {
            try {
                String strLine = br.readLine();
                if (strLine == null) {
                    break;
                }
                String[] strArray = strLine.split(",");
                list.add(new VoiceTitle(strArray[0], strArray[1]));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ArrayList<String> loadNote(Context context) {
        ArrayList<String> list = new ArrayList();
        InputStreamReader in = new InputStreamReader(context.getResources().openRawResource(R.raw.note));
        BufferedReader br = new BufferedReader(in);
        while (true) {
            try {
                String strLine = br.readLine();
                if (strLine == null) {
                    break;
                }
                list.add(strLine);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
