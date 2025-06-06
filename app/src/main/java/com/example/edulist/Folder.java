package com.example.edulist;

public class Folder {
    private long id;
    private String name;
    private int color;

    public Folder() {
    }

    public Folder(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public Folder(long id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return name;
    }
}