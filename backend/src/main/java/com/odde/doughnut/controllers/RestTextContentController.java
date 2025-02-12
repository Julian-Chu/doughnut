
package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.currentUser.CurrentUserFetcher;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.entities.json.NoteRealm;
import com.odde.doughnut.exceptions.NoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.NoteViewer;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.TestabilitySettings;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.sql.Timestamp;

@RestController
@RequestMapping("/api/text_content")
class RestTextContentController {
    private final ModelFactoryService modelFactoryService;
    private final CurrentUserFetcher currentUserFetcher;
    @Resource(name = "testabilitySettings")
    private final TestabilitySettings testabilitySettings;

    public RestTextContentController(ModelFactoryService modelFactoryService, CurrentUserFetcher currentUserFetcher, TestabilitySettings testabilitySettings) {
        this.modelFactoryService = modelFactoryService;
        this.currentUserFetcher = currentUserFetcher;
        this.testabilitySettings = testabilitySettings;
    }

    @PatchMapping(path = "/{note}")
    @Transactional
    public NoteRealm updateNote(@PathVariable(name = "note") Note note, @Valid @ModelAttribute TextContent textContent) throws NoAccessRightException {
        final UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertAuthorization(note);

        Timestamp currentUTCTimestamp = testabilitySettings.getCurrentUTCTimestamp();
        note.getTextContent().updateTextContent(textContent, currentUTCTimestamp);

        modelFactoryService.noteRepository.save(note);
        return new NoteViewer(user.getEntity(), note).toJsonObject();
    }

}
