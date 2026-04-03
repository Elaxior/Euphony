const express = require("express");
const jwt = require("jsonwebtoken");
const { timingSafeEqual } = require("node:crypto");
const { z } = require("zod");
const { requireAdminAuth } = require("../middleware/adminAuth");

const router = express.Router();

const loginSchema = z.object({
  adminKey: z.string().min(1)
});

function safeEqualText(a, b) {
  const first = Buffer.from(a);
  const second = Buffer.from(b);

  if (first.length !== second.length) {
    return false;
  }

  return timingSafeEqual(first, second);
}

router.post("/login", (req, res) => {
  const parsed = loginSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ message: "adminKey is required" });
  }

  const adminKey = process.env.ADMIN_UPLOAD_KEY;
  const secret = process.env.ADMIN_JWT_SECRET || adminKey;
  if (!adminKey || !secret) {
    return res.status(500).json({ message: "Admin auth is not configured on server" });
  }

  if (!safeEqualText(parsed.data.adminKey, adminKey)) {
    return res.status(401).json({ message: "Invalid admin key" });
  }

  const token = jwt.sign({ role: "admin" }, secret, { expiresIn: "12h" });
  return res.json({ token, expiresIn: 43200 });
});

router.get("/verify", requireAdminAuth, (_req, res) => {
  return res.json({ ok: true });
});

module.exports = router;