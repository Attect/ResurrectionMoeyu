package jp.co.a_tm.moeyu;

public class LovePoint {
    private final int[] IN_COIN = new int[]{1, 3, 30};
    private final int[] TERM = new int[]{0, 2, 14, 44, 100, 188};

    public int currentLevel(int currentPoint) {
        for (int i = 1; i < this.TERM.length; i++) {
            if (currentPoint < this.TERM[i]) {
                return i;
            }
        }
        return this.TERM.length;
    }

    public int rest(int currentPoint) {
        for (int i = 1; i < this.TERM.length; i++) {
            if (currentPoint < this.TERM[i]) {
                return currentPoint - this.TERM[i - 1];
            }
        }
        return 0;
    }

    public int need(int currentPoint) {
        for (int i = 1; i < this.TERM.length; i++) {
            if (currentPoint < this.TERM[i]) {
                return this.TERM[i] - currentPoint;
            }
        }
        return -1;
    }

    public int nextTerm(int currentLevel) {
        if (currentLevel < this.TERM.length) {
            return this.TERM[currentLevel] - this.TERM[currentLevel - 1];
        }
        return -1;
    }

    public int next(int currentLevel) {
        if (currentLevel < this.TERM.length) {
            return this.TERM[currentLevel];
        }
        return -1;
    }

    public int maxLevel() {
        return this.TERM.length;
    }

    public int getPoint(String type) {
        if (type.equals(CoinController.PLATINUM)) {
            return this.IN_COIN[2];
        }
        if (type.equals(CoinController.GOLD)) {
            return this.IN_COIN[1];
        }
        return this.IN_COIN[0];
    }
}
