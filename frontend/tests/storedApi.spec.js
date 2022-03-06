/**
 * @jest-environment jsdom
 */
import storedApi from "@/managedApi/storedApi";
import { useStore } from "@/store";
import makeMe from "./fixtures/makeMe";
import { setActivePinia, createPinia } from "pinia";

beforeEach(() => {
  fetch.resetMocks();
});

describe("storedApi", () => {
  setActivePinia(createPinia());
  const store = useStore();
  const note = makeMe.aNote.please()
  const sa = storedApi({$store: store})

  describe("delete note", () => {
    beforeEach(() => {
      fetch.mockResponseOnce(JSON.stringify({}));
      store.loadNotes([note]);
    });

    it("should call the api", async () => {
      await sa.deleteNote(note.id)
      expect(fetch).toHaveBeenCalledTimes(1);
      expect(fetch).toHaveBeenCalledWith(`/api/notes/${note.id}/delete`, expect.anything());
    });

    it("should change the store", async () => {
      await sa.deleteNote(note.id)
      expect(store.getNoteById(note.id)).toBeUndefined()
    });

    it("should remove children notes", async () => {
      const child = makeMe.aNote.under(note).please()
      store.loadNotes([child]);
      await sa.deleteNote(note.id)
      expect(store.getNoteById(child.id)).toBeUndefined()
    });

    it("should remove child from list", async () => {
      const child = makeMe.aNote.under(note).please()
      store.loadNotes([child]);
      const childrenCount = store.getChildrenIdsByParentId(note.id).length
      await sa.deleteNote(child.id)
      expect(store.getChildrenIdsByParentId(note.id)).toHaveLength(childrenCount - 1)
    });

  });
});

