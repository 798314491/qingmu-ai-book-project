package com.markdown.notes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Note Create Request DTO
 */
@Data
public class NoteCreateRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String content;
    
    private Long folderId;
    
    private String tags;
    
    private Boolean isPublic = false;
}
