<template>
  <svg
    viewBox="0 0 100 100"
    :x="x"
    :y="y ? y : 25"
    :height="!!oheight ? oheight : 350"
  >
    <path
      d="
  M75,10
   a45,45 0 1,0 0,80
   a15,15 0 0,1 0,-80
  "
      :fill="fill"
    />
  </svg>
</template>

<script setup>
import { computed } from "@vue/runtime-core";
import { bgcolors } from "../../../colors";

const props = defineProps({
  linkRole: String,
  x: Number,
  y: Number,
  oheight: Number,
});
const fill = computed(() => bgcolors(props.linkRole));
</script>
