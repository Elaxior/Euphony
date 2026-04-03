const path = require("node:path");
const fs = require("node:fs/promises");
const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const morgan = require("morgan");
const dotenv = require("dotenv");
const reviewsRoute = require("./routes/reviews");
const apksRoute = require("./routes/apks");
const messagesRoute = require("./routes/messages");
const authRoute = require("./routes/auth");
const { notFound, errorHandler } = require("./middleware/errorHandler");

dotenv.config();

const app = express();
const PORT = Number(process.env.PORT || 4000);
const uploadsPath = path.join(__dirname, "../uploads/apks");

async function bootstrap() {
  await fs.mkdir(uploadsPath, { recursive: true });

  app.use(helmet({
    crossOriginResourcePolicy: false
  }));

  app.use(
    cors({
      origin: process.env.FRONTEND_ORIGIN || "http://localhost:5173"
    })
  );

  app.use(morgan("dev"));
  app.use(express.json({ limit: "1mb" }));

  app.use("/downloads/apks", express.static(uploadsPath));

  app.get("/api/health", (_req, res) => {
    return res.json({
      status: "ok",
      service: "euphony-promo-api",
      timestamp: new Date().toISOString()
    });
  });

  app.use("/api/reviews", reviewsRoute);
  app.use("/api/apks", apksRoute);
  app.use("/api/messages", messagesRoute);
  app.use("/api/auth", authRoute);

  app.use(notFound);
  app.use(errorHandler);

  app.listen(PORT, () => {
    console.log(`API running at http://localhost:${PORT}`);
  });
}

bootstrap().catch((error) => {
  console.error("Failed to start backend", error);
  process.exit(1);
});
