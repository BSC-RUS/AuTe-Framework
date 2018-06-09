import { AutotesterUiPage } from './app.po';

describe('autotester-ui App', () => {
  let page: AutotesterUiPage;

  beforeEach(() => {
    page = new AutotesterUiPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!!');
  });
});
