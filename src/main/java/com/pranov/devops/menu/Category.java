package com.pranov.devops.menu;

public enum Category {
    STARTERS("Starters"),
    MAINS("Main Course"),
    BIRYANI("Biryani"),
    BREADS("Breads"),
    DESSERTS("Desserts"),
    BEVERAGES("Beverages");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
