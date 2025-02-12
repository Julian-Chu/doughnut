<template>
  <h5 v-if="titleAsLink" class="header note-card-title">
    <NoteTitleWithLink :note="note" class="card-title" />
  </h5>
  <EditableText v-else role="title" class="note-title"
    :multipleLine="false"
    scopeName="note" v-model="textContent.title"  @blur="onBlurTextField"
  />
  <div class="note-content">
    <EditableText
        :multipleLine="true"
        role="description"
        v-if="size==='large'"
        class="col note-description"
        scopeName="note"
        v-model="textContent.description"
        @blur="onBlurTextField"/>
    <NoteShortDescription class="col" v-if="size==='medium'" :shortDescription="note.shortDescription"/>
    <SvgDescriptionIndicator v-if="size==='small' && !!textContent.description" class="description-indicator"/>
    <template v-if="note.pictureWithMask">
      <ShowPicture
        v-if="size!=='small'"
        class="col text-center"
        v-bind="note.pictureWithMask"
        :opacity="0.2"
      />
      <SvgPictureIndicator v-else class="picture-indicator"/>
    </template>
    <template v-if="!!note.noteAccessories.url">
      <div v-if="size!='small'">
        <label v-if="note.noteAccessories.urlIsVideo">Video Url:</label>
        <label v-else>Url:</label>
        <a :href="note.noteAccessories.url" target="_blank">{{ note.noteAccessories.url }}</a>
      </div>
      <a v-else :href="note.noteAccessories.url" target="_blank">
        <SvgUrlIndicator/>
      </a>
    </template>
  </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from "vue";
import NoteTitleWithLink from "./NoteTitleWithLink.vue";
import NoteShortDescription from "./NoteShortDescription.vue";
import ShowPicture from "./ShowPicture.vue";
import SvgDescriptionIndicator from "../svgs/SvgDescriptionIndicator.vue";
import SvgPictureIndicator from "../svgs/SvgPictureIndicator.vue";
import SvgUrlIndicator from "../svgs/SvgUrlIndicator.vue";
import EditableText from "../form/EditableText.vue";
import useStoredLoadingApi from "../../managedApi/useStoredLoadingApi";

export default defineComponent({
  setup() {
    return useStoredLoadingApi({hasFormError: true});
  },
  props: {
    note: {type: Object as PropType<Generated.Note>, required: true },
    size: { type: String, default: 'large'},
    titleAsLink: Boolean,
  },
  components: {
    NoteShortDescription,
    ShowPicture,
    SvgDescriptionIndicator,
    SvgPictureIndicator,
    SvgUrlIndicator,
    EditableText,
    NoteTitleWithLink,
  },
  computed: {
    textContent(){
      return {...this.note.textContent};
    },
  },
  methods: {
    onBlurTextField() {
      this.storedApi.updateTextContent(this.note.id, this.textContent)
      .then((res) => {
        this.$emit("done");
      })
      .catch((res) => (this.formErrors = res))
    }
  }
});
</script>

<style lang="sass" scoped>
.note-content
  display: flex
  flex-wrap: wrap
  .col
    flex: 1 1 auto
    width: 50%

</style>
