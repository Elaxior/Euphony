const express = require("express");
const { z } = require("zod");
const { randomUUID } = require("node:crypto");
const path = require("node:path");
const { readCollection, writeCollection } = require("../utils/fileDb");

const router = express.Router();
const reviewsFile = path.join(__dirname, "../../data/reviews.json");

const reviewSchema = z.object({
  name: z.string().min(2).max(60),
  title: z.string().min(2).max(80),
  rating: z.number().int().min(1).max(5),
  comment: z.string().min(5).max(600)
});

router.get("/", async (_req, res, next) => {
  try {
    const reviews = await readCollection(reviewsFile, []);
    const sorted = [...reviews].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

    return res.json({
      count: sorted.length,
      items: sorted
    });
  } catch (error) {
    return next(error);
  }
});

router.post("/", async (req, res, next) => {
  try {
    const parsed = reviewSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({
        message: "Invalid review payload",
        issues: parsed.error.flatten()
      });
    }

    const reviews = await readCollection(reviewsFile, []);
    const review = {
      id: randomUUID(),
      ...parsed.data,
      createdAt: new Date().toISOString()
    };

    reviews.push(review);
    await writeCollection(reviewsFile, reviews);

    return res.status(201).json(review);
  } catch (error) {
    return next(error);
  }
});

module.exports = router;
