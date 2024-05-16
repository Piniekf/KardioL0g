package com.party.kardiol0g.files;

public class FileData {
    private String url;
    private String note;
    private String fileType;

    public FileData() {
        // Konieczny pusty konstruktor dla Firebase
    }

    public FileData(String url, String note, String fileType) {
        this.url = url;
        this.note = note;
        this.fileType = fileType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
