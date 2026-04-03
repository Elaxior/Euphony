const fs = require("node:fs/promises");
const path = require("node:path");

async function ensureFile(filePath, fallback = []) {
  try {
    await fs.access(filePath);
  } catch {
    await fs.mkdir(path.dirname(filePath), { recursive: true });
    await fs.writeFile(filePath, JSON.stringify(fallback, null, 2), "utf8");
  }
}

async function readCollection(filePath, fallback = []) {
  await ensureFile(filePath, fallback);
  const raw = await fs.readFile(filePath, "utf8");
  try {
    return JSON.parse(raw);
  } catch {
    return fallback;
  }
}

async function writeCollection(filePath, data) {
  await fs.writeFile(filePath, JSON.stringify(data, null, 2), "utf8");
}

module.exports = {
  readCollection,
  writeCollection
};
