// Simple i18n key usage checker
// Scans Vue/TS files for $t('key') or i18n.t('key') and compares against en-US/vi-VN
// Usage: node scripts/check-i18n-keys.js
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SRC_DIR = path.join(ROOT, 'src');
const EN_LOCALE = path.join(SRC_DIR, 'locale', 'en-US.ts');
const VI_LOCALE = path.join(SRC_DIR, 'locale', 'vi-VN.ts');

function readFileSafe(p) {
  try { return fs.readFileSync(p, 'utf8'); } catch { return ''; }
}

function extractObjectKeys(tsModuleText) {
  // crude parse for `'key': 'value'` or "key": "value"
  const map = new Set();
  const matches = tsModuleText.matchAll(/['"]([\w.-]+)['"]\s*:\s*['`"]?/g);
  for (const m of matches) {
    if (m && m[1]) map.add(m[1]);
  }
  return map;
}

function walk(dir, files = []) {
  for (const name of fs.readdirSync(dir)) {
    const p = path.join(dir, name);
    const stat = fs.statSync(p);
    if (stat.isDirectory()) {
      if (name === 'node_modules' || name.startsWith('.')) continue;
      walk(p, files);
    } else if (/\.(vue|ts|tsx|js|jsx)$/.test(name)) {
      files.push(p);
    }
  }
  return files;
}

function scanUsage(fileText) {
  const keys = new Set();
  const patterns = [
    /\$t\(\s*['"]([\w.-]+)['"]/g,          // $t('key')
    /i18n\.t\(\s*['"]([\w.-]+)['"]/g,     // i18n.t('key')
  ];
  for (const re of patterns) {
    const iter = fileText.matchAll(re);
    for (const m of iter) {
      if (m && m[1]) keys.add(m[1]);
    }
  }
  return keys;
}

function main() {
  const enText = readFileSafe(EN_LOCALE);
  const viText = readFileSafe(VI_LOCALE);
  if (!enText || !viText) {
    console.error('Could not read locale files.');
    process.exit(1);
  }
  const enKeys = extractObjectKeys(enText);
  const viKeys = extractObjectKeys(viText);

  const usedKeys = new Set();
  const files = walk(SRC_DIR);
  for (const f of files) {
    const txt = readFileSafe(f);
    const found = scanUsage(txt);
    found.forEach(k => usedKeys.add(k));
  }

  const missingInEn = [];
  const missingInVi = [];
  const unusedKeys = [];

  for (const k of usedKeys) {
    if (!enKeys.has(k)) missingInEn.push(k);
    if (!viKeys.has(k)) missingInVi.push(k);
  }
  for (const k of enKeys) {
    if (!usedKeys.has(k)) unusedKeys.push(k);
  }

  const report = {
    summary: {
      filesScanned: files.length,
      usedKeys: usedKeys.size,
      enKeys: enKeys.size,
      viKeys: viKeys.size,
      missingInEn: missingInEn.length,
      missingInVi: missingInVi.length,
      unusedKeys: unusedKeys.length,
    },
    missingInEn: missingInEn.sort(),
    missingInVi: missingInVi.sort(),
    unusedKeys: unusedKeys.sort(),
  };

  console.log(JSON.stringify(report, null, 2));
  if (missingInEn.length || missingInVi.length) process.exitCode = 2;
}

main();


