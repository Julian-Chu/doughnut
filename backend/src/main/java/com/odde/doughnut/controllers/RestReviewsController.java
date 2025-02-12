
package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.currentUser.CurrentUserFetcher;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.entities.json.*;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.AnswerModel;
import com.odde.doughnut.models.ReviewPointModel;
import com.odde.doughnut.models.Reviewing;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.TestabilitySettings;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
class RestReviewsController {
    private final ModelFactoryService modelFactoryService;
    private final CurrentUserFetcher currentUserFetcher;
    @Resource(name = "testabilitySettings")
    private final TestabilitySettings testabilitySettings;


    public RestReviewsController(ModelFactoryService modelFactoryService, CurrentUserFetcher currentUserFetcher, TestabilitySettings testabilitySettings) {
        this.modelFactoryService = modelFactoryService;
        this.currentUserFetcher = currentUserFetcher;
        this.testabilitySettings = testabilitySettings;
    }

    @GetMapping("/overview")
    public ReviewStatus overview() {
        UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertLoggedIn();
        return user.createReviewing(testabilitySettings.getCurrentUTCTimestamp()).getReviewStatus();
    }

    @GetMapping("/initial")
    public ReviewPointViewedByUser initialReview() {
        UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertLoggedIn();
        Reviewing reviewing = user.createReviewing(testabilitySettings.getCurrentUTCTimestamp());
        ReviewPoint reviewPoint = reviewing.getOneInitialReviewPoint();
        ReviewPointViewedByUser from = ReviewPointViewedByUser.from(reviewPoint, user);
        from.setRemainingInitialReviewCountForToday(reviewing.toInitialReviewCount());
        return from;
    }

    @PostMapping(path = "")
    @Transactional
    public ReviewPointViewedByUser create(@RequestBody InitialInfo initialInfo) {
        UserModel userModel = currentUserFetcher.getUser();
        userModel.getAuthorization().assertLoggedIn();
        if (initialInfo.reviewPoint.getNoteId() != null) {
            initialInfo.reviewPoint.setNote(modelFactoryService.noteRepository.findById(initialInfo.reviewPoint.getNoteId()).orElse(null));
        }
        if (initialInfo.reviewPoint.getLinkId() != null) {
            initialInfo.reviewPoint.setLink(modelFactoryService.linkRepository.findById(initialInfo.reviewPoint.getLinkId()).orElse(null));
        }
        ReviewPointModel reviewPointModel = modelFactoryService.toReviewPointModel(initialInfo.reviewPoint);
        reviewPointModel.initialReview(userModel, initialInfo.reviewSetting, testabilitySettings.getCurrentUTCTimestamp());
        return initialReview();
    }

    @GetMapping("/repeat")
    @Transactional
    public RepetitionForUser repeatReview() {
        UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertLoggedIn();
        Reviewing reviewing = user.createReviewing(testabilitySettings.getCurrentUTCTimestamp());
        RepetitionForUser oneRepetitionForUser = reviewing.getOneRepetitionForUser(testabilitySettings.getRandomizer());
        if(oneRepetitionForUser == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no more repetition for today");
        return oneRepetitionForUser;
    }

    @PostMapping("/answer")
    @Transactional
    public AnswerResult answerQuiz(@Valid @RequestBody Answer answer) {
        UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertLoggedIn();
        AnswerModel answerModel = modelFactoryService.toAnswerModel(answer);
        answerModel.updateReviewPoints(testabilitySettings.getCurrentUTCTimestamp());
        answerModel.save();
        AnswerResult answerResult = answerModel.getAnswerResult();
        Reviewing reviewing = user.createReviewing(testabilitySettings.getCurrentUTCTimestamp());
        answerResult.nextRepetition = Optional.ofNullable(reviewing.getOneRepetitionForUser(testabilitySettings.getRandomizer()));
        return answerResult;
    }

    @GetMapping(path = "/answers/{answer}")
    @Transactional
    public AnswerViewedByUser getAnswer(Answer answer) {
        UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertAuthorization(answer.getQuestion().getReviewPoint());
        AnswerModel answerModel = modelFactoryService.toAnswerModel(answer);
        AnswerViewedByUser answerResult = answerModel.getAnswerViewedByUser();
        answerResult.reviewPoint = ReviewPointViewedByUser.from(answer.getQuestion().getReviewPoint(), user);
        answerResult.quizQuestion = QuizQuestionViewedByUser.from(answer.getQuestion(), modelFactoryService.noteRepository);
        return answerResult;
    }

    @PostMapping(path = "/{reviewPoint}/self-evaluate")
    @Transactional
    public ReviewPoint selfEvaluate(ReviewPoint reviewPoint, @RequestBody SelfEvaluation selfEvaluation) {
        if (reviewPoint == null || reviewPoint.getId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The review point does not exist.");
        }
        UserModel user = currentUserFetcher.getUser();
        user.getAuthorization().assertLoggedIn();
        modelFactoryService.toReviewPointModel(reviewPoint).evaluate(testabilitySettings.getCurrentUTCTimestamp(), selfEvaluation.selfEvaluation);
        return reviewPoint;
    }

}
