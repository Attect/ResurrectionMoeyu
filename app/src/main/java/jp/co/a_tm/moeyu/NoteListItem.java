package jp.co.a_tm.moeyu;

public class NoteListItem {
    private boolean mCleared;
    public int mNo;
    private boolean mPreCleared;
    public int mTerm;

    public NoteListItem(int columnNum, int openedNum, boolean cleared, boolean preCleared) {
        this.mNo = columnNum;
        this.mTerm = openedNum;
        this.mCleared = cleared;
        this.mPreCleared = preCleared;
    }

    public boolean getCleared() {
        return this.mCleared;
    }

    public void setPreCleared(boolean preCleared) {
        this.mPreCleared = preCleared;
    }

    public boolean getPreCleared() {
        return this.mPreCleared;
    }
}
