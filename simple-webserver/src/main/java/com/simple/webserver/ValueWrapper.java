package com.simple.webserver;

public class ValueWrapper {
    private Object key;

    ValueWrapper(int key) {
        this.key = key;
    }

    ValueWrapper(double key) {
        this.key = key;
    }

    ValueWrapper(String key) {
        try {
            this.key = Integer.parseInt(key);
            return;
        } catch (NumberFormatException e) {
        }
        try {
            this.key = Double.parseDouble(key);
            return;
        } catch (NumberFormatException e) {
        }
        this.key = key;
    }

    @Override
    public String toString() {
        return "" + key;
    }

    @Override
    public boolean equals(Object other) {
        return other.getClass().equals(this.key.getClass()) && other.equals(this.key);

    }

    // @Override
    // public int hashCode() {

    // }
}
