class PixelRecord {
    int color;
    Long userEditTime;
    Long lastEditTime;

    PixelRecord(int color, Long userEditTime, Long lastEditTime) {
        this.color = color;
        this.userEditTime = userEditTime;
        this.lastEditTime = lastEditTime;
    }
}
