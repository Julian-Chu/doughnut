import { render } from "@testing-library/vue";
import moment from "moment";
import NoteShow from "@/components/notes/NoteShow.vue";
import { createMockNote } from "./NoteShow-fixtures";

describe("new/updated pink banner", () => {
  test("should show pink banner if the note was updated within the last 12 hours", async () => {
    const now = moment().format();
    const note = createMockNote({ note: { noteContent: { updatedAt: now } } });
    const wrapper = render(NoteShow, { props: note });

    expect(
      await wrapper.findByText("This note has been changed recently.")
    ).toBeTruthy();
  });

  test("should show 0 pink banners if the note was updated more than 12 hours ago", async () => {
    const lastUpdated = moment().subtract(12, "hours").format();
    const note = createMockNote({
      note: { noteContent: { updatedAt: lastUpdated } },
    });
    const wrapper = render(NoteShow, { props: note });

    expect(
      await wrapper.queryAllByText("This note has been changed recently.")
    ).toHaveLength(0);
  });
});
