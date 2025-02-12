<template>
  <template v-if="quizQuestion.questionType === 'JUST_REVIEW'">
    <ReviewPointAsync
      v-bind="{
        reviewPointId: quizQuestion.quizQuestion.reviewPoint,
        showSelfEvaluate: false,
      }"
    />
    <SelfEvaluateButtons @selfEvaluate="onSelfEvaluate" />
  </template>
  <template v-else>
    <BasicBreadcrumb :ancestors="quizQuestion.scope" />
    <ShowPicture
      v-if="quizQuestion.pictureWithMask"
      v-bind="quizQuestion.pictureWithMask"
      :opacity="1"
    />
    <NoteFrameOfLinks v-bind="{ links: quizQuestion.hintLinks }">
      <div class="quiz-instruction">
        <pre
          style="white-space: pre-wrap"
          v-if="!pictureQuestion"
          v-html="quizQuestion.description"
        />
        <h2 v-if="!!quizQuestion.mainTopic" class="text-center">
          {{ quizQuestion.mainTopic }}
        </h2>
      </div>
    </NoteFrameOfLinks>

    <div class="row" v-if="quizQuestion.questionType !== 'SPELLING'">
      <div
        class="col-sm-6 mb-3 d-grid"
        v-for="option in quizQuestion.options"
        :key="option.note.id"
      >
        <button
          class="btn btn-secondary btn-lg"
          v-on:click.once="
            answerNoteId = option.note.id;
            processForm();
          "
        >
          <div v-if="!option.picture">{{ option.display }}</div>
          <div v-else>
            <ShowPicture v-bind="option.note.note.pictureWithMask" :opacity="1" />
          </div>
        </button>
      </div>
    </div>

    <div v-else>
      <form @submit.prevent.once="processForm">
        <div class="aaa">
          <TextInput
            scopeName="review_point"
            field="answer"
            v-model="answer"
            placeholder="put your answer here"
            :autofocus="true"
          />
        </div>
        <input type="submit" value="OK" class="btn btn-primary btn-lg btn-block" />
      </form>
    </div>
  </template>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import BasicBreadcrumb from "../commons/BasicBreadcrumb.vue";
import ShowPicture from "../notes/ShowPicture.vue";
import NoteFrameOfLinks from "../links/NoteFrameOfLinks.vue";
import TextInput from "../form/TextInput.vue";
import SelfEvaluateButtons from "./SelfEvaluateButtons.vue";
import SvgNoReview from "../svgs/SvgNoReview.vue";
import ReviewPointAsync from "./ReviewPointAsync.vue";

export default defineComponent({
  props: {
    quizQuestion: {
      type: Object as PropType<Generated.QuizQuestionViewedByUser>,
      required: true,
    },
  },
  components: {
    BasicBreadcrumb,
    ShowPicture,
    NoteFrameOfLinks,
    TextInput,
    SelfEvaluateButtons,
    SvgNoReview,
    ReviewPointAsync,
  },
  emits: ["answer", "removeFromReview"],
  data() {
    return {
      answer: "" as string,
      answerNoteId: null as Doughnut.ID | null,
    };
  },
  computed: {
    answerToQuestion(): Generated.Answer {
      return {
        spellingAnswer: this.answer,
        answerNoteId: this.answerNoteId,
        question: this.quizQuestion.quizQuestion,
      };
    },
    pictureQuestion() {
      return this.quizQuestion.questionType === "PICTURE_TITLE";
    },
  },
  methods: {
    onSelfEvaluate(choice: string) {
      this.answer = choice;
      this.processForm();
    },
    processForm() {
      this.$emit("answer", this.answerToQuestion);
    },
  },
});
</script>
