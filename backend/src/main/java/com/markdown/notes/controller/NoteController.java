package com.markdown.notes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markdown.notes.common.Result;
import com.markdown.notes.dto.NoteCreateRequest;
import com.markdown.notes.dto.NoteUpdateRequest;
import com.markdown.notes.entity.Note;
import com.markdown.notes.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import com.markdown.notes.security.UserPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Note Management Controller
 */
@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Note management APIs")
public class NoteController {
    
    private final NoteService noteService;
    
    @GetMapping
    @Operation(summary = "Get user notes with pagination")
    public Result<IPage<Note>> getNotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long folderId,
            Authentication authentication) {
        
        Page<Note> pageParam = new Page<>(page, size);
        IPage<Note> notes = noteService.getUserNotes(pageParam, getUserId(authentication), keyword, folderId);
        return Result.success(notes);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get note by ID")
    public Result<Note> getNoteById(@PathVariable Long id, Authentication authentication) {
        Note note = noteService.getNoteById(id, getUserId(authentication));
        return Result.success(note);
    }
    
    @PostMapping
    @Operation(summary = "Create new note")
    public Result<Note> createNote(@Valid @RequestBody NoteCreateRequest request, Authentication authentication) {
        Note note = noteService.createNote(request, getUserId(authentication));
        return Result.success(note);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update note")
    public Result<Note> updateNote(@PathVariable Long id, 
                                   @Valid @RequestBody NoteUpdateRequest request, 
                                   Authentication authentication) {
        Note note = noteService.updateNote(id, request, getUserId(authentication));
        return Result.success(note);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete note")
    public Result<Void> deleteNote(@PathVariable Long id, Authentication authentication) {
        noteService.deleteNote(id, getUserId(authentication));
        return Result.success();
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search notes")
    public Result<List<Note>> searchNotes(@RequestParam String keyword, Authentication authentication) {
        List<Note> notes = noteService.searchNotes(keyword, getUserId(authentication));
        return Result.success(notes);
    }
    
    @PostMapping("/{id}/star")
    @Operation(summary = "Star/Unstar note")
    public Result<Void> toggleStar(@PathVariable Long id, Authentication authentication) {
        noteService.toggleStar(id, getUserId(authentication));
        return Result.success();
    }
    
    @GetMapping("/starred")
    @Operation(summary = "Get starred notes")
    public Result<List<Note>> getStarredNotes(Authentication authentication) {
        List<Note> notes = noteService.getStarredNotes(getUserId(authentication));
        return Result.success(notes);
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recently edited notes")
    public Result<List<Note>> getRecentNotes(@RequestParam(defaultValue = "10") int limit, 
                                             Authentication authentication) {
        List<Note> notes = noteService.getRecentNotes(getUserId(authentication), limit);
        return Result.success(notes);
    }
    
    private Long getUserId(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getId();
    }
}
