const express = require("express");
const path = require("node:path");
const multer = require("multer");
const { randomUUID } = require("node:crypto");
const { z } = require("zod");
const { readCollection, writeCollection } = require("../utils/fileDb");

const router = express.Router();
const apkFile = path.join(__dirname, "../../data/apks.json");
const uploadDir = path.join(__dirname, "../../uploads/apks");

const storage = multer.diskStorage({
  destination: (_req, _file, cb) => {
    cb(null, uploadDir);
  },
  filename: (_req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const safeExt = ext === ".apk" ? ext : ".apk";
    cb(null, `${Date.now()}-${randomUUID()}${safeExt}`);
  }
});

const upload = multer({
  storage,
  fileFilter: (_req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    if (ext !== ".apk") {
      return cb(new Error("Only .apk files are allowed"));
    }
    return cb(null, true);
  }
});

const uploadSchema = z.object({
  version: z.string().min(1).max(20),
  changelog: z.string().min(3).max(4000),
  build: z.string().max(30).optional(),
  adminKey: z.string().optional()
});

router.get("/", async (_req, res, next) => {
  try {
    const apks = await readCollection(apkFile, []);
    const sorted = [...apks].sort(
      (a, b) => new Date(b.releasedAt).getTime() - new Date(a.releasedAt).getTime()
    );

    return res.json({
      latest: sorted[0] || null,
      items: sorted
    });
  } catch (error) {
    return next(error);
  }
});

router.post("/upload", upload.single("apk"), async (req, res, next) => {
  try {
    const parsed = uploadSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({
        message: "Invalid upload payload",
        issues: parsed.error.flatten()
      });
    }

    if (!req.file) {
      return res.status(400).json({ message: "APK file is required" });
    }

    const requiredKey = process.env.ADMIN_UPLOAD_KEY;
    if (requiredKey && parsed.data.adminKey !== requiredKey) {
      return res.status(401).json({ message: "Invalid admin key" });
    }

    const apks = await readCollection(apkFile, []);
    const entry = {
      id: randomUUID(),
      version: parsed.data.version,
      build: parsed.data.build || "stable",
      changelog: parsed.data.changelog,
      fileName: req.file.filename,
      fileSize: req.file.size,
      downloadUrl: `/downloads/apks/${req.file.filename}`,
      releasedAt: new Date().toISOString()
    };

    apks.push(entry);
    await writeCollection(apkFile, apks);

    return res.status(201).json(entry);
  } catch (error) {
    return next(error);
  }
});

module.exports = router;
