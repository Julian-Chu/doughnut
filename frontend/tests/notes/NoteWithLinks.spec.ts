/**
 * @jest-environment jsdom
 */
import NoteWithLinks from '@/components/notes/NoteWithLinks.vue';
import makeMe from '../fixtures/makeMe';
import helper from '../helpers';

helper.resetWithApiMock(beforeEach, afterEach)

describe('new/updated pink banner', () => {
  beforeAll(() => {
    Date.now = jest.fn(() => new Date(Date.UTC(2017, 1, 14)).valueOf());
  });

  it.each([
    [new Date(Date.UTC(2017, 1, 15)), 'rgb(208,237,23)'],
    [new Date(Date.UTC(2017, 1, 13)), 'rgb(189,209,64)'],
    [new Date(Date.UTC(2017, 1, 12)), 'rgb(181,197,82)'],
    [new Date(Date.UTC(2016, 1, 12)), 'rgb(150,150,150)'],
  ])(
    'should show fresher color if recently updated',
    (updatedAt, expectedColor) => {
      const note = makeMe.aNoteRealm.textContentUpdatedAt(updatedAt).please();
      helper.store.loadNoteRealms([note]);

      const wrapper = helper.component(NoteWithLinks).withProps(note).mount()

      expect(wrapper.find('.note-body').element).toHaveStyle(
        `border-color: ${expectedColor};`
      );
    }
  );
});

describe('in place edit on title', () => {
  it('should display text field when one single click on title', async () => {
    const noteParent = makeMe.aNoteRealm.title('Dummy Title').please();
    helper.store.loadNoteRealms([noteParent]);

    const wrapper = helper.component(NoteWithLinks).withProps(noteParent).mount()

    expect(wrapper.findAll('[role="title"] input')).toHaveLength(0);
    await wrapper.find('[role="title"] h2').trigger('click');

    expect(wrapper.findAll('[role="title"] input')).toHaveLength(1);
    expect(wrapper.findAll('[role="title"] h2')).toHaveLength(0);
  });

  it('should back to label when blur text field title', async () => {
    const noteParentSphere = makeMe.aNoteRealm.title('Dummy Title').please();
    helper.store.loadNoteRealms([noteParentSphere]);

    const wrapper = helper.component(NoteWithLinks).withProps(noteParentSphere).mount()

    await wrapper.find('[role="title"]').trigger('click');
    await wrapper.find('[role="title"] input').setValue('updated');
    await wrapper.find('[role="title"] input').trigger('blur');

    helper.apiMock.verifyCall(`/api/text_content/${noteParentSphere.id}`, expect.objectContaining({ method: 'PATCH' })
    );
  });
});

describe('undo editing', () => {
  it('should call addEditingToUndoHistory on submitChange', async () => {
    const noteRealm = makeMe.aNoteRealm.title('Dummy Title').please();
    helper.store.loadNoteRealms([noteRealm]);

    const updatedTitle = 'updated';
    const wrapper = helper.component(NoteWithLinks).withProps(noteRealm).mount()

    await wrapper.find('[role="title"]').trigger('click');
    await wrapper.find('[role="title"] input').setValue(updatedTitle);
    await wrapper.find('[role="title"] input').trigger('blur');

    expect(helper.store.peekUndo()).toMatchObject({ type: 'editing' });
    helper.apiMock.verifyCall(`/api/text_content/${noteRealm.id}`)
  });
});
