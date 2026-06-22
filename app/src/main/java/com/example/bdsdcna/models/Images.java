package com.example.bdsdcna.models;

import java.util.List;

public class Images {

    private List<String> before;
    private List<String> during;
    private List<String> after;

    public Images() {
    }

    public List<String> getBefore() {
        return before;
    }

    public List<String> getDuring() {
        return during;
    }

    public List<String> getAfter() {
        return after;
    }

    public void setBefore(List<String> before) {
        this.before = before;
    }

    public void setDuring(List<String> during) {
        this.during = during;
    }

    public void setAfter(List<String> after) {
        this.after = after;
    }
}