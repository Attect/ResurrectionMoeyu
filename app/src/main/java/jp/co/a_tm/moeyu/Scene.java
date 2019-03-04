package jp.co.a_tm.moeyu;

public enum Scene {
    bath_a(0),
    head(1),
    body(2),
    bath_b(3);
    
    public final int number;

    private Scene(int n) {
        this.number = n;
    }

    public Scene next() {
        switch (this) {
            case bath_a:
                return head;
            case head:
                return body;
            case body:
                return bath_b;
            default:
                return null;
        }
    }
}
