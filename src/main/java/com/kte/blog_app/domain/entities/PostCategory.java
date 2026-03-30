package com.kte.blog_app.domain.entities;




public enum PostCategory {
    DRAFT("Draft"),
    PUBLISHED("Publisher");

    private final String displayName;
    PostCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}