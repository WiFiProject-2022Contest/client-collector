package wifilocation.wifi.barcode;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Barcode {

    @SerializedName("barcode_serial")
    private String serial;

    @SerializedName("pos_x")
    private Float posX;

    @SerializedName("pos_y")
    private Float posY;

    @SerializedName("date")
    private Date date;

    public Barcode(String serial, Float posX, Float posY, Date date) {
        this.serial = serial;
        this.posX = posX;
        this.posY = posY;
        this.date = date;
    }

    public Barcode(String serial, Float posX, Float posY, long date) {
        this.serial = serial;
        this.posX = posX;
        this.posY = posY;
        this.date = new Date(date);
    }

    public String getSerial() {
        return serial;
    }

    public Float getPosX() {
        return posX;
    }

    public Float getPosY() {
        return posY;
    }

    public Date getDate() {
        return date;
    }
}
