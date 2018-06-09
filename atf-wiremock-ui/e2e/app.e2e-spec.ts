import { BscWireMockUiPage } from './app.po';

describe('bsc-wire-mock-ui App', () => {
  let page: BscWireMockUiPage;

  beforeEach(() => {
    page = new BscWireMockUiPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!!');
  });
});
