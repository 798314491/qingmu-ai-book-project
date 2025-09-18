package com.markdown.notes.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String email;
    
    private String password;
    
    private String nickname;
    
    private String avatar;
    
    private Integer status;
    
    private LocalDateTime lastLoginTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}