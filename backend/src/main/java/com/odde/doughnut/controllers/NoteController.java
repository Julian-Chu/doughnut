package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.currentUser.CurrentUser;
import com.odde.doughnut.models.Note;
import com.odde.doughnut.repositories.NoteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class NoteController {
    private final CurrentUser currentUser;
    private final NoteRepository noteRepository;

    public NoteController(CurrentUser currentUser, NoteRepository noteRepository) {
        this.currentUser = currentUser;
        this.noteRepository = noteRepository;
    }

    @GetMapping("/note")
    public String notes(Model model) {
        model.addAttribute("note", new Note());
        return "note";
    }

    @GetMapping({"/all_my_notes", "/all_my_notes/{id}"})
    public String all_my_notes(@PathVariable(name = "id") Integer noteId, Model model) {
        if (noteId != null) {
            model.addAttribute("note", noteRepository.findById(noteId).get());
        }
        model.addAttribute("all_my_notes", currentUser.getUser().getNotes());
        return "all_my_notes";
    }

    @GetMapping("/link/{id}")
    public String link(
            @PathVariable("id") String id,
            @RequestParam(required = false) String searchTerm,
            Model model
    ) {
        Note sourceNote = noteRepository.findById(Integer.valueOf(id)).get();
        List<Note> linkableNotes = currentUser.getUser().filterLinkableNotes(sourceNote, searchTerm);
        model.addAttribute("linkableNotes", linkableNotes);
        model.addAttribute("sourceNote", sourceNote);
        return "link";
    }
}
