export function xpForLevel(level: number): number {
  return (level - 1) * (level - 1) * 100;
}

export function xpProgress(xp: number, level: number): number {
  const current = xpForLevel(level);
  const next = xpForLevel(level + 1);
  if (next === current) return 100;
  return ((xp - current) / (next - current)) * 100;
}
