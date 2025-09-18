package com.markdown.notes.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.notes.dto.NoteCreateRequest;
import com.markdown.notes.dto.NoteUpdateRequest;
import com.markdown.notes.entity.Note;
import com.markdown.notes.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Note Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {
    
    private final NoteMapper noteMapper;
    
    /**
     * Get user notes with pagination
     */
    public IPage<Note> getUserNotes(Page<Note> page, Long userId, String keyword, Long folderId) {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_deleted", false)
                   .orderByDesc("updated_at");
        
        if (folderId != null) {
            queryWrapper.eq("folder_id", folderId);
        }
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like("title", keyword)
                .or()
                .like("content", keyword)
                .or()
                .like("tags", keyword));
        }
        
        return noteMapper.selectPage(page, queryWrapper);
    }
    
    /**
     * Get note by ID
     */
    public Note getNoteById(Long id, Long userId) {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                   .eq("user_id", userId)
                   .eq("is_deleted", false);
        
        Note note = noteMapper.selectOne(queryWrapper);
        if (note == null) {
            throw new RuntimeException("Note not found or access denied");
        }
        
        return note;
    }
    
    /**
     * Create new note
     */
    @Transactional
    public Note createNote(NoteCreateRequest request, Long userId) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setFolderId(request.getFolderId());
        note.setTags(request.getTags());
        note.setIsPublic(request.getIsPublic());
        note.setIsStarred(false);
        note.setIsDeleted(false);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        
        // Generate summary from content
        if (StringUtils.hasText(request.getContent())) {
            String summary = request.getContent().length() > 200 
                ? request.getContent().substring(0, 200) + "..." 
                : request.getContent();
            note.setSummary(summary);
        }
        
        noteMapper.insert(note);
        log.info("Created note with ID: {} for user: {}", note.getId(), userId);
        
        return note;
    }
    
    /**
     * Update note
     */
    @Transactional
    public Note updateNote(Long id, NoteUpdateRequest request, Long userId) {
        Note note = getNoteById(id, userId);
        
        if (StringUtils.hasText(request.getTitle())) {
            note.setTitle(request.getTitle());
        }
        
        if (request.getContent() != null) {
            note.setContent(request.getContent());
            // Update summary
            if (StringUtils.hasText(request.getContent())) {
                String summary = request.getContent().length() > 200 
                    ? request.getContent().substring(0, 200) + "..." 
                    : request.getContent();
                note.setSummary(summary);
            }
        }
        
        if (request.getFolderId() != null) {
            note.setFolderId(request.getFolderId());
        }
        
        if (request.getTags() != null) {
            note.setTags(request.getTags());
        }
        
        if (request.getIsPublic() != null) {
            note.setIsPublic(request.getIsPublic());
        }
        
        if (request.getIsStarred() != null) {
            note.setIsStarred(request.getIsStarred());
        }
        
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        
        log.info("Updated note with ID: {} for user: {}", id, userId);
        return note;
    }
    
    /**
     * Delete note (soft delete)
     */
    @Transactional
    public void deleteNote(Long id, Long userId) {
        Note note = getNoteById(id, userId);
        note.setIsDeleted(true);
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        
        log.info("Deleted note with ID: {} for user: {}", id, userId);
    }
    
    /**
     * Search notes
     */
    public List<Note> searchNotes(String keyword, Long userId) {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_deleted", false)
                   .and(wrapper -> wrapper
                       .like("title", keyword)
                       .or()
                       .like("content", keyword)
                       .or()
                       .like("tags", keyword))
                   .orderByDesc("updated_at")
                   .last("LIMIT 50");
        
        return noteMapper.selectList(queryWrapper);
    }
    
    /**
     * Toggle star status
     */
    @Transactional
    public void toggleStar(Long id, Long userId) {
        Note note = getNoteById(id, userId);
        note.setIsStarred(!note.getIsStarred());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        
        log.info("Toggled star for note ID: {} to: {} for user: {}", id, note.getIsStarred(), userId);
    }
    
    /**
     * Get starred notes
     */
    public List<Note> getStarredNotes(Long userId) {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_deleted", false)
                   .eq("is_starred", true)
                   .orderByDesc("updated_at");
        
        return noteMapper.selectList(queryWrapper);
    }
    
    /**
     * Get recent notes
     */
    public List<Note> getRecentNotes(Long userId, int limit) {
        QueryWrapper<Note> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("is_deleted", false)
                   .orderByDesc("updated_at")
                   .last("LIMIT " + limit);
        
        return noteMapper.selectList(queryWrapper);
    }
}
