package com.markdown.notes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.markdown.notes.entity.Note;
import org.apache.ibatis.annotations.Mapper;

/**
 * Note Mapper Interface
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}