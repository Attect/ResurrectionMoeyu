package jp.co.a_tm.moeyu;

public class ItemListItem {
    private ItemTableController mController;
    private int[] mItemNumber;

    public ItemListItem(int[] itemNumber, ItemTableController controller) {
        this.mItemNumber = itemNumber;
        this.mController = controller;
    }

    public int getButtonCount() {
        return this.mItemNumber.length;
    }

    public int getItemNumber(int buttonNumber) {
        return this.mItemNumber[buttonNumber - 1];
    }

    public boolean getOpened(int buttonNumber) {
        return this.mController.isOpened(this.mItemNumber[buttonNumber - 1]);
    }
}
