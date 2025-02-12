
package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.currentUser.CurrentUserFetcher;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.entities.json.*;
import com.odde.doughnut.exceptions.NoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.NoteViewer;
import com.odde.doughnut.models.SearchTermModel;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.TestabilitySettings;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
class RestNoteController {
    private final ModelFactoryService modelFactoryService;
    private final CurrentUserFetcher currentUserFetcher;
    @Resource(name = "testabilitySettings")
    private final TestabilitySettings testabilitySettings;

    public RestNoteController(ModelFactoryService modelFactoryService, CurrentUserFetcher currentUserFetcher, TestabilitySettings testabilitySettings) {
        this.modelFactoryService = modelFactoryService;
        this.currentUserFetcher = currentUserFetcher;
        this.testabilitySettings = testabilitySettings;
    }

    static class NoteStatistics {
        @Getter
        @Setter
        private ReviewPoint reviewPoint;
        @Getter
        @Setter
        private NoteRealm note;

    }

    @PostMapping(value = "/{parentNote}/create")
    @Transactional
    public NotesBulk createNote(@PathVariable(name = "parentNote") Note parentNote, @Valid @ModelAttribute NoteCreation noteCreation) throws NoAccessRightException {
        final UserModel userModel = currentUserFetcher.getUser();
        userModel.getAuthorization().assertAuthorization(parentNote);
        User user = userModel.getEntity();
        Note note = Note.createNote(user, testabilitySettings.getCurrentUTCTimestamp(), noteCreation.textContent);
        note.setParentNote(parentNote);
        modelFactoryService.noteRepository.save(note);
        if (noteCreation.getLinkTypeToParent() != null) {
            Link link = new Link();
            link.setUser(user);
            link.setSourceNote(note);
            link.setTargetNote(parentNote);
            link.setTypeId(noteCreation.getLinkTypeToParent());
            modelFactoryService.linkRepository.save(link);
        }
        return NotesBulk.jsonNoteWithParent(note, userModel);
    }

    @GetMapping("/{note}")
    public NotesBulk show(@PathVariable("note") Note note) throws NoAccessRightException {
        final UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertReadAuthorization(note);

        return NotesBulk.jsonNoteWithChildren(note, user);
    }

    @GetMapping("/{note}/overview")
    public NotesBulk showOverview(@PathVariable("note") Note note) throws NoAccessRightException {
        final UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertReadAuthorization(note);

        return NotesBulk.jsonNoteWitheDescendants(note, user);
    }

    @PatchMapping(path = "/{note}")
    @Transactional
    public NoteRealm updateNote(@PathVariable(name = "note") Note note, @Valid @ModelAttribute NoteAccessories noteAccessories) throws NoAccessRightException, IOException {
        final UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertAuthorization(note);

        noteAccessories.setUpdatedAt(testabilitySettings.getCurrentUTCTimestamp());

        note.updateNoteContent(noteAccessories, user.getEntity());
        modelFactoryService.noteRepository.save(note);
        return new NoteViewer(user.getEntity(), note).toJsonObject();

    }

    @GetMapping("/{note}/statistics")
    public NoteStatistics statistics(@PathVariable("note") Note note) throws NoAccessRightException {
        final UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertReadAuthorization(note);
        NoteStatistics statistics = new NoteStatistics();
        statistics.setReviewPoint(user.getReviewPointFor(note));
        statistics.note = new NoteViewer(user.getEntity(), note).toJsonObject();
        return statistics;
    }

    @PostMapping("/search")
    @Transactional
    public List<Note> searchForLinkTarget(@Valid @RequestBody SearchTerm searchTerm) {
        SearchTermModel searchTermModel = modelFactoryService.toSearchTermModel(currentUserFetcher.getUser().getEntity(), searchTerm);
        return searchTermModel.searchForNotes();
    }

    @PostMapping(value = "/{note}/delete")
    @Transactional
    public Integer deleteNote(@PathVariable("note") Note note) throws NoAccessRightException {
        currentUserFetcher.getUser().getAuthorization().assertAuthorization(note);
        modelFactoryService.toNoteModel(note).destroy(testabilitySettings.getCurrentUTCTimestamp());
        modelFactoryService.entityManager.flush();
        return note.getId();
    }

    @PatchMapping(value = "/{note}/undo-delete")
    @Transactional
    public NotesBulk undoDeleteNote(@PathVariable("note") Note note) throws NoAccessRightException {
        currentUserFetcher.getUser().getAuthorization().assertAuthorization(note);
        modelFactoryService.toNoteModel(note).restore();
        modelFactoryService.entityManager.flush();
        return NotesBulk.jsonNoteWithChildren(note, currentUserFetcher.getUser());
    }

    @GetMapping("/{note}/review-setting")
    public ReviewSetting editReviewSetting(Note note) {
        ReviewSetting reviewSetting = note.getMasterReviewSetting();
        if (reviewSetting == null) {
            reviewSetting = new ReviewSetting();
        }
        return reviewSetting;
    }

    @PostMapping(value = "/{note}/review-setting")
    @Transactional
    public RedirectToNoteResponse updateReviewSetting(@PathVariable("note") Note note, @Valid @RequestBody ReviewSetting reviewSetting) throws NoAccessRightException {
        currentUserFetcher.getUser().getAuthorization().assertAuthorization(note);
        note.mergeMasterReviewSetting(reviewSetting);
        modelFactoryService.noteRepository.save(note);
        return new RedirectToNoteResponse(note.getId());
    }
}
