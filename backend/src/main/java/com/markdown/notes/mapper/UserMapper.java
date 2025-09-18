package com.markdown.notes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.notes.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User Mapper Interface
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}