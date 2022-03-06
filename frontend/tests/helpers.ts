import { merge } from "lodash";
import { Component } from "vue";
import { mount } from "@vue/test-utils";
import { render } from "@testing-library/vue";
import { createTestingPinia } from "@pinia/testing";

type Options = {};

const withMockRoute = (
  comp: Component,
  options: Options = {},
  currentRoute: any,
  func: (comp: Component, options: Options) => any
) => {
  const mockRouter = {
    push: jest.fn(),
  };

  const wrapper = func(
    comp,
    merge(options, {
      global: {
        directives: {
          focus() {}
        },
        mocks: {
          $route: currentRoute,
          $router: mockRouter,
        },
        stubs: {
          "router-view": true,
          "router-link": {
            props: ["to"],
            template: `<a class="router-link" :to='JSON.stringify(to)'><slot/></a>`,
          },
        },
      },
    })
  );

  return { wrapper, mockRouter };
};

const mountWithMockRoute = (
  comp: Component,
  options: Options = {},
  currentRoute: any
) => {
  return withMockRoute(comp, options, currentRoute, mount);
};

const renderWithMockRoute = (
  comp: Component,
  options: Options = {},
  currentRoute: any
) => {
  return withMockRoute(comp, options, currentRoute, render);
};

const withStoreAndMockRoute = (
  store: Function,
  comp: Component,
  options: Options = {},
  currentRoute: any,
  func: (comp: Component, options: Options) => any
) => {
  return withMockRoute(
    comp,
    merge(options, {
      global: {
        plugins: [store],
      },
    }),
    currentRoute,
    func
  );
};

const renderWithStoreAndMockRoute = (
  store: Function,
  comp: Component,
  options: Options = {},
  currentRoute: any,
) => {
  return withStoreAndMockRoute(
    store,
    comp,
    merge(options, {
      global: {
        plugins: [store],
      },
    }),
    currentRoute,
    render
  );
};

const mountWithStoreAndMockRoute = (
  store: Function,
  comp: Component,
  options: Options = {},
  currentRoute: any,
) => {
  return withStoreAndMockRoute(
    store,
    comp,
    merge(options, {
      global: {
        plugins: [createTestingPinia()],
      },
    }),
    currentRoute,
    mount
  );
};

export { mountWithMockRoute, renderWithMockRoute, renderWithStoreAndMockRoute, mountWithStoreAndMockRoute };
