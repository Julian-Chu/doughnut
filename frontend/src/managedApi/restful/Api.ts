import RestfullFetch, { JsonData } from "./RestfulFetch";

class Api {
  fetch;

  constructor(base_url: string) {
    this.fetch = new RestfullFetch(base_url)
  }

  restGet(url: string) { return this.fetch.restRequest(url, {}, {method: "GET"}); }

  restPost(url: string, data: JsonData) { return this.fetch.restRequest( url, data, { method: 'POST' }); }

  restPatch(url: string, data: JsonData) { return this.fetch.restRequest( url, data, { method: 'PATCH' }); }

  restPostMultiplePartForm(url: string, data: JsonData) {
     return this.fetch.restRequest( url, data, { method: 'POST', contentType: "MultiplePartForm" });
  }

  restPatchMultiplePartForm(url: string, data: JsonData) {
    return this.fetch.restRequest( url, data, { method: 'PATCH', contentType: "MultiplePartForm" });
  }

  restPostWithHtmlResponse(url: string, data: JsonData) {
    return this.fetch.restRequestWithHtmlResponse(url, data, { method: 'POST'});
  }
}

export default Api;
