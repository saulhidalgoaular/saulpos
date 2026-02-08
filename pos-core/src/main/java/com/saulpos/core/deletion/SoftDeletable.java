package com.saulpos.core.deletion;

import java.time.LocalDateTime;

public interface SoftDeletable {
    boolean isDeleted();
    void setDeleted(boolean deleted);
    
    LocalDateTime getDeletedAt();
    void setDeletedAt(LocalDateTime deletedAt);
    
    String getDeletedBy();
    void setDeletedBy(String deletedBy);
}
