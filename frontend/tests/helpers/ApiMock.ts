import fetchMock, { MockParams } from "jest-fetch-mock";

interface ApiMock {
  expecting(url: string) : void
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  expecting(url: string, value: any) : void
  expectingResponse(url: string, response: MockParams) : void
  verifyCall(url: string) : void
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  verifyCall(url: string, matcher: any) : void
}

class ApiMockImpl implements ApiMock {
  fetchMock = fetchMock

  private unexpectedApiCalls: string[] = []

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  private expected: {url: string, value: any, response?: MockParams, called: boolean}[] = []

  init() {
    this.fetchMock.doMock(async (request: Request)=>{
      const match = this.expected.filter(exp=>exp.url === request.url).pop()
      if(match) {
        match.called = true
        return match.response || JSON.stringify(match.value)
      }

      this.unexpectedApiCalls.push(request.url)
      return JSON.stringify({})
    })
    return this
  }

  noUnexpectedCalls() {
    expect(this.unexpectedApiCalls).toHaveLength(0)
    expect(this.mismatchedApiCalls).toHaveLength(0)
  }

  private get mismatchedApiCalls(): string[] {
    return this.expected.filter(exp => !exp.called).map(exp => exp.url);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  expecting(url: string, value: any={}) {
    this.expected.push({ url, value, called: false })
  }

  expectingResponse(url: string, response: MockParams) {
    this.expected.push({ url, value: {}, response, called: false })
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  verifyCall(url: string, matcher: any=expect.anything()) {
    const unexpectedIndex = this.unexpectedApiCalls.indexOf(url)
    if(unexpectedIndex >=0) this.unexpectedApiCalls.splice(unexpectedIndex, 1)

    expect(this.fetchMock).toHaveBeenCalledWith(
      url,
      matcher
    )
  }
}

const  setupApiMock = () => {
  const mockedApi = new ApiMockImpl().init()
  return {mockedApi: mockedApi as ApiMock, teardown: ()=> mockedApi.noUnexpectedCalls()}
}

export { setupApiMock }

export default ApiMock