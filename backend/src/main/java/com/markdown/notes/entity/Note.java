package com.markdown.notes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Note Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("notes")
public class Note implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long folderId;
    
    private String title;
    
    private String content;
    
    private String summary;
    
    private String tags;
    
    private Boolean isStarred;
    
    @TableLogic
    private Boolean isDeleted;
    
    private Integer wordCount;
    
    private Integer viewCount;
    
    private Integer sortOrder;
    
    private LocalDateTime deletedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}