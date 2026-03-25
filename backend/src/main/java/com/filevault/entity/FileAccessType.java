package com.filevault.entity;

public enum FileAccessType {
    PUBLIC,      // Anyone can view
    PRIVATE,     // Only admin can view
    RESTRICTED   // Admin can share with specific users or payment required
}
