package pivot.charts.content;

import java.util.Date;

public class Candlestick {
    private Date date = null;
    private float open = 0;
    private float high = 0;
    private float low = 0;
    private float close = 0;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @SuppressWarnings("deprecation")
    public final void setDate(String date) {
        setDate(new Date(Date.parse((String)date)));
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }
}
