import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideIcons } from '@ng-icons/core';
import {
  lucideArrowRight,
  lucideCheck,
  lucideChevronDown,
  lucideDatabase,
  lucideInfo,
  lucideLogOut,
  lucideMoon,
  lucideShield,
  lucideSun,
  lucideTrophy,
  lucideUser,
  lucideX,
  lucideXCircle,
  lucideZap,
} from '@ng-icons/lucide';
import { App } from './app';

@Component({ selector: 'router-outlet', template: '', standalone: true })
class MockRouterOutlet {}

@Component({ selector: 'app-header', template: '', standalone: true })
class MockHeader {}

@Component({ selector: 'app-toast', template: '', standalone: true })
class MockToast {}

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideIcons({
          lucideArrowRight,
          lucideCheck,
          lucideChevronDown,
          lucideDatabase,
          lucideInfo,
          lucideLogOut,
          lucideMoon,
          lucideShield,
          lucideSun,
          lucideTrophy,
          lucideUser,
          lucideX,
          lucideXCircle,
          lucideZap,
        }),
      ],
    })
      .overrideComponent(App, {
        set: {
          imports: [MockRouterOutlet, MockHeader, MockToast],
        },
      })
      .compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should have sqlab-client as title signal value', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app['title']()).toBe('sqlab-client');
  });
});
