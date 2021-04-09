package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.currentUser.CurrentUserFetcher;
import com.odde.doughnut.entities.LinkEntity;
import com.odde.doughnut.entities.Note;
import com.odde.doughnut.exceptions.NoAccessRightException;
import com.odde.doughnut.models.LinkModel;
import com.odde.doughnut.services.ModelFactoryService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/links")
public class LinkController extends ApplicationMvcController  {
    private final ModelFactoryService modelFactoryService;

    public LinkController(CurrentUserFetcher currentUserFetcher, ModelFactoryService modelFactoryService) {
        super(currentUserFetcher);
        this.modelFactoryService = modelFactoryService;
    }

    @GetMapping("/{linkEntity}")
    public String show( @PathVariable("linkEntity") LinkEntity linkEntity, Model model) throws NoAccessRightException {
        currentUserFetcher.getUser().assertAuthorization(linkEntity);
        return "links/show";
    }

    @GetMapping("/{note}/link")
    public String link(@PathVariable("note") Note note, @RequestParam(required = false) String searchTerm, Model model) {
        List<Note> linkableNotes = currentUserFetcher.getUser().filterLinkableNotes(note, searchTerm);
        model.addAttribute("linkableNotes", linkableNotes);
        return "links/new";
    }

    @PostMapping(value = "/{note}/link", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String linkNote(@PathVariable("note") Note note, Integer targetNoteId, Model model) throws NoAccessRightException {
        Note targetNote = modelFactoryService.noteRepository.findById(targetNoteId).get();
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.setSourceNote(note);
        linkEntity.setTargetNote(targetNote);
        linkEntity.setType(LinkEntity.LinkType.RELATED_TO.label);
        model.addAttribute("linkEntity", linkEntity);
        return "links/link_choose_type";
    }

    @PostMapping(value = "/create_link", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String linkNoteFinalize(@Valid LinkEntity linkEntity, BindingResult bindingResult) throws NoAccessRightException {
        if (bindingResult.hasErrors()) {
            return "links/link_choose_type";
        }
        currentUserFetcher.getUser().assertAuthorization(linkEntity.getSourceNote());
        linkEntity.setUserEntity(currentUserFetcher.getUser().getEntity());
        modelFactoryService.linkRepository.save(linkEntity);
        return "redirect:/notes/" + linkEntity.getSourceNote().getId();
    }

    @PostMapping(value = "/{linkEntity}", params="submit", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String updateLink(@Valid LinkEntity linkEntity, BindingResult bindingResult) throws NoAccessRightException {
        if (bindingResult.hasErrors()) {
            return "links/show";
        }
        currentUserFetcher.getUser().assertAuthorization(linkEntity.getSourceNote());
        modelFactoryService.linkRepository.save(linkEntity);
        return "redirect:/notes/" + linkEntity.getSourceNote().getId();
    }

    @PostMapping(value = "/{linkEntity}", params="delete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String deleteLink(@Valid LinkEntity linkEntity) throws NoAccessRightException {
        currentUserFetcher.getUser().assertAuthorization(linkEntity.getSourceNote());
        LinkModel linkModel = modelFactoryService.toLinkModel(linkEntity);
        linkModel.destroy();
        return "redirect:/notes/" + linkEntity.getSourceNote().getId();
    }

}
