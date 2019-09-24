package it.unipi.gio.vaseDriver.model;


public class GioPlantsResponse {
    private String id;
    private String value;
    private String mac;
    private String creation_timestamp;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getCreation_timestamp() {
        return creation_timestamp;
    }

    public void setCreation_timestamp(String creation_timestamp) {
        this.creation_timestamp = creation_timestamp;
    }
}
