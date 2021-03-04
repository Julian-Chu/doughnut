package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.currentUser.CurrentUserFetcher;
import com.odde.doughnut.entities.NoteEntity;
import com.odde.doughnut.entities.ReviewPointEntity;
import com.odde.doughnut.services.ModelFactoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ReviewController {
    private final CurrentUserFetcher currentUserFetcher;
    private final ModelFactoryService modelFactoryService;

    public ReviewController(CurrentUserFetcher currentUserFetcher, ModelFactoryService modelFactoryService) {
        this.currentUserFetcher = currentUserFetcher;
        this.modelFactoryService = modelFactoryService;
    }

    @GetMapping("/review")
    public String review(Model model) {
        List<NoteEntity> notes = currentUserFetcher.getUser().getNewNotesToReview();
        if (notes.size() > 0) {
            NoteEntity noteEntity = notes.get(0);
            if (modelFactoryService.reviewPointRepository.findByNoteEntity(noteEntity) != null) {
                return "review_done";
            }
            ReviewPointEntity reviewPointEntity = new ReviewPointEntity();
            reviewPointEntity.setNoteEntity(noteEntity);
            reviewPointEntity.setUserEntity(currentUserFetcher.getUser().getEntity());
            modelFactoryService.reviewPointRepository.save(reviewPointEntity);
        }
        model.addAttribute("notes", notes);
        return "review";
    }
}
