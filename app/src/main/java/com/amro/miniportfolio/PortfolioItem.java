package com.amro.miniportfolio;

public class PortfolioItem {
    private String title;
    private String description;
    private String docId; // ADD THIS

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public PortfolioItem() {} // Needed by Firestore

    public PortfolioItem(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }

    public String getDescription() { return description; }
}
