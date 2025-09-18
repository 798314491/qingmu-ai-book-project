package com.markdown.notes.dto;

import lombok.Data;

/**
 * Note Update Request DTO
 */
@Data
public class NoteUpdateRequest {
    
    private String title;
    
    private String content;
    
    private Long folderId;
    
    private String tags;
    
    private Boolean isPublic;
    
    private Boolean isStarred;
}
