<template>
  <div>
    <LinkTypeSelect
      field="linkType"
      scopeName="link"
      v-model="formData.typeId"
      :errors="formErrors.typeId"
      :inverseIcon="true"
    />
    <div>
      Target: <strong>{{ targetNote.title }}</strong>
    </div>
    <CheckInput
      scopeName="link"
      v-model="formData.moveUnder"
      :errors="formErrors.moveUnder"
      field="alsoMoveToUnderTargetNote"
    />

    <RadioButtons
      v-if="!!formData.moveUnder"
      scopeName="link"
      v-model="formData.asFirstChild"
      :errors="formErrors.asFristChild"
      :options="[
        { value: true, label: 'as its first child' },
        { value: false, label: 'as its last child' },
      ]"
    />

    <button
      class="btn btn-secondary go-back-button"
      v-on:click="$emit('goBack')"
    >
      <SvgGoBack />
    </button>
    <button class="btn btn-primary" @click.once="createLink()">
      Create Link
    </button>
  </div>
</template>

<script>
import LinkTypeSelect from "./LinkTypeSelect.vue";
import CheckInput from "../form/CheckInput.vue";
import RadioButtons from "../form/RadioButtons.vue";
import SvgGoBack from "../svgs/SvgGoBack.vue";
import useStoredLoadingApi from "../../managedApi/useStoredLoadingApi";
import usePopups from "../commons/Popups/usePopup";

export default ({
  setup() {
    return {...useStoredLoadingApi({hasFormError: true}), ...usePopups()};
  },
  name: "LinkNoteFinalize",
  props: { note: Object, targetNote: { type: Object, required: true } },
  components: { LinkTypeSelect, SvgGoBack, CheckInput, RadioButtons },
  emits: ["success", "goBack"],
  data() {
    return {
      formData: { asFirstChild: false },
    };
  },
  methods: {
    async createLink() {
      if (this.formData.moveUnder && this.note.parentId === null) {
        if (
          !(await this.popups.confirm(
            `"${this.note.title}" is a top level notebook. Do you want to move it under other notebook?`
          ))
        ) {
          return;
        }
      }
      this.storedApi.createLink(this.note.id, this.targetNote.id, this.formData)
        .then((r) => this.$emit("success"))
        .catch((res) => {
          this.formErrors = res
        });
    },
  },
});
</script>
