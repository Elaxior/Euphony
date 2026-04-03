const express = require("express");
const { z } = require("zod");
const { randomUUID } = require("node:crypto");
const path = require("node:path");
const { readCollection, writeCollection } = require("../utils/fileDb");

const router = express.Router();
const messagesFile = path.join(__dirname, "../../data/messages.json");

const messageSchema = z.object({
  name: z.string().min(2).max(60),
  email: z.string().email(),
  message: z.string().min(10).max(1200)
});

router.post("/", async (req, res, next) => {
  try {
    const parsed = messageSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({
        message: "Invalid message payload",
        issues: parsed.error.flatten()
      });
    }

    const messages = await readCollection(messagesFile, []);
    const entry = {
      id: randomUUID(),
      ...parsed.data,
      createdAt: new Date().toISOString()
    };

    messages.push(entry);
    await writeCollection(messagesFile, messages);

    return res.status(201).json({
      message: "Feedback submitted",
      id: entry.id
    });
  } catch (error) {
    return next(error);
  }
});

module.exports = router;
