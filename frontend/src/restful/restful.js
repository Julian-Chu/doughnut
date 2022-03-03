import HttpResponseError from "./HttpResponseError";
import BadRequestError from "./BadRequestError";

const loginOrRegister = () => {
  window.location = `/users/identify?from=${window.location.href}`;
};

const restRequest = async (url, params) => {
  try {
    const res = await fetch(url, params)
    if (res.status !== 200 && res.status !== 400) {
      throw new HttpResponseError(res.status);
    }
    const resp = await res.json()
    if (res.status === 200) return resp;
    if (res.status === 400) throw new BadRequestError(resp.errors);
  }
  catch(error) {
    if (error.status === 204) {
      return null;
    }
    if (error.status === 401) {
      loginOrRegister();
      return null;
    }
    throw error
  }
}


const restRequest = async (url, params) => {
  try {
    const res = await fetch(url, params)
    if (res.status === 200 || res.status === 400) {
      const jsonResponse = await res.json()
      if (res.status === 400) throw toNested(jsonResponse.errors);
      return jsonResponse;
    }
    throw new HttpResponseError(res.status);
  }
  catch(error) {
    if (error.status === 204) {
      return null;
    }
    if (error.status === 401) {
      loginOrRegister();
      return null;
    }
    throw error
  }
}


const restRequestWithHtmlResponse = async (url, params) => {
  try {
    const res = await fetch(url, params);
    if (res.status !== 200 && res.status !== 400) {
      throw new HttpResponseError(res.status);
    }
    const html = await res.text();
    if (res.status === 200) return html;
    if (res.status === 400) throw Error("BadRequest", html);
  }
  catch(error) {
    if (error.status === 204) {
      return null;
    }
    if (error.status === 401) {
      loginOrRegister();
      return null;
    }
    throw error
  }
}

const restGet = (url) => restRequest(url, {});

const restPost = (url, data) =>
  restRequest(
    url,
    {
      method: 'POST',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    },
  );

const restPatch = (url, data) =>
  restRequest(
    url,
    {
      method: 'PATCH',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    },
  );

function objectToFormData(data) {
  const formData = new FormData();
  Object.keys(data).forEach((key) => {
    if (data[key] === null) {
      formData.append(key, '');
    } else if (data[key] instanceof Object && !(data[key] instanceof File)) {
      Object.keys(data[key]).forEach((subKey) => {
        formData.append(
          `${key}.${subKey}`,
          data[key][subKey] === null ? '' : data[key][subKey]
        );
      });
    } else {
      formData.append(key, data[key]);
    }
  });
  return formData;
}

const restPostMultiplePartForm = (url, data) =>
  restRequest(
    url,
    {
      method: 'POST',
      headers: {
        Accept: 'application/json',
      },
      body: objectToFormData(data),
    },
  );

const restPatchMultiplePartForm = (url, data) =>
  restRequest(
    url,
    {
      method: 'PATCH',
      headers: {
        Accept: 'application/json',
      },
      body: objectToFormData(data),
    },
  );

const restPostWithHtmlResponse = (url, data) =>
  restRequestWithHtmlResponse(url, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

export {
  restGet,
  restPost,
  restPatch,
  restPostMultiplePartForm,
  restPatchMultiplePartForm,
  loginOrRegister,
  restPostWithHtmlResponse,
};
