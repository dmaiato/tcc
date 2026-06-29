import { TestBed } from '@angular/core/testing';
import { UiThemeService, Theme } from './theme.service';

describe('UiThemeService', () => {
  let matchMediaMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    localStorage.clear();
    delete document.documentElement.dataset['theme'];
    matchMediaMock = window.matchMedia as ReturnType<typeof vi.fn>;
    matchMediaMock.mockClear();
  });

  it('deve usar dark quando localStorage vazio e preferência dark', () => {
    matchMediaMock.mockReturnValue({ matches: false });
    TestBed.configureTestingModule({});
    const service = TestBed.inject(UiThemeService);
    expect(service['theme']()).toBe('dark');
  });

  it('deve usar light quando localStorage = light', () => {
    localStorage.setItem('sqlab-theme', 'light');
    TestBed.configureTestingModule({});
    const service = TestBed.inject(UiThemeService);
    expect(service['theme']()).toBe('light');
  });

  it('deve usar dark quando localStorage = dark', () => {
    localStorage.setItem('sqlab-theme', 'dark');
    TestBed.configureTestingModule({});
    const service = TestBed.inject(UiThemeService);
    expect(service['theme']()).toBe('dark');
  });

  it('toggle deve alternar de dark para light', () => {
    localStorage.setItem('sqlab-theme', 'dark');
    TestBed.configureTestingModule({});
    const service = TestBed.inject(UiThemeService);
    service.toggle();
    expect(service.isDark()).toBe(false);
    expect(service.isLight()).toBe(true);
  });

  it('toggle deve alternar de light para dark', () => {
    localStorage.setItem('sqlab-theme', 'light');
    TestBed.configureTestingModule({});
    const service = TestBed.inject(UiThemeService);
    service.toggle();
    expect(service.isDark()).toBe(true);
    expect(service.isLight()).toBe(false);
  });

  it('apply deve persistir tema em localStorage', () => {
    localStorage.setItem('sqlab-theme', 'dark');
    TestBed.configureTestingModule({});
    const service = TestBed.inject(UiThemeService);
    service.toggle();
    expect(localStorage.getItem('sqlab-theme')).toBe('light');
  });

  it('apply deve setar dataset.theme para light e remover para dark', () => {
    localStorage.setItem('sqlab-theme', 'dark');
    TestBed.configureTestingModule({});
    const service = TestBed.inject(UiThemeService);
    service.toggle();
    expect(document.documentElement.dataset['theme']).toBe('light');
    service.toggle();
    expect(document.documentElement.dataset['theme']).toBeUndefined();
  });

  it('isDark/isLight signals refletem tema atual', () => {
    localStorage.setItem('sqlab-theme', 'dark');
    TestBed.configureTestingModule({});
    const service = TestBed.inject(UiThemeService);
    expect(service.isDark()).toBe(true);
    expect(service.isLight()).toBe(false);

    service.toggle();
    expect(service.isDark()).toBe(false);
    expect(service.isLight()).toBe(true);
  });
});
