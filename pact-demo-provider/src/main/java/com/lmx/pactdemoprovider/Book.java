package com.lmx.pactdemoprovider;


import java.util.Date;
import java.util.UUID;

public class Book {
    private UUID id;
    private String author;
    private boolean bestSeller;
    private Date createdOn;

    public Book() {}

    public Book(UUID id, String author, boolean bestSeller, Date createdOn) {
        this.id = id;
        this.author = author;
        this.bestSeller = bestSeller;
        this.createdOn = createdOn;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isBestSeller() {
        return bestSeller;
    }

    public void setBestSeller(boolean bestSeller) {
        this.bestSeller = bestSeller;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

}
