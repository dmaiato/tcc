import { describe, it, expect } from 'vitest';
import { xpForLevel, xpProgress } from './xp.utils';

describe('xpForLevel', () => {
  it('returns 0 for level 1', () => {
    expect(xpForLevel(1)).toBe(0);
  });

  it('returns 100 for level 2', () => {
    expect(xpForLevel(2)).toBe(100);
  });

  it('returns 1600 for level 5', () => {
    expect(xpForLevel(5)).toBe(1600);
  });
});

describe('xpProgress', () => {
  it('returns 0 when xp equals current level threshold', () => {
    expect(xpProgress(0, 1)).toBe(0);
  });

  it('calculates correct percentage between levels', () => {
    // level 2: current=100, next=400, xp=300 → ((300-100)/(400-100))*100 = 66.66...
    const result = xpProgress(300, 2);
    expect(result).toBeCloseTo(66.666, 2);
  });

  it('returns 100 when next equals current (should not happen in practice)', () => {
    // If xpForLevel returns same for level and level+1, return 100
    expect(xpProgress(0, 0)).toBe(100);
  });
});
