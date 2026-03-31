package com.kte.blog_app.domain.entities;





public enum PostStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String displayName;
    PostStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
