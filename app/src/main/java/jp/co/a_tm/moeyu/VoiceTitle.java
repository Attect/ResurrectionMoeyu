package jp.co.a_tm.moeyu;

public class VoiceTitle {
    private String mFileName;
    private String mTitle;

    public VoiceTitle(String name, String title) {
        this.mFileName = name;
        this.mTitle = title;
    }

    public String getFileName() {
        return this.mFileName;
    }

    public String getTitle() {
        return this.mTitle;
    }
}
