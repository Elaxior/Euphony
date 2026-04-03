const jwt = require("jsonwebtoken");

function extractToken(req) {
  const header = req.headers.authorization || "";
  if (!header.startsWith("Bearer ")) {
    return null;
  }
  return header.slice("Bearer ".length).trim();
}

function requireAdminAuth(req, res, next) {
  const token = extractToken(req);
  if (!token) {
    return res.status(401).json({ message: "Missing admin token" });
  }

  try {
    const secret = process.env.ADMIN_JWT_SECRET || process.env.ADMIN_UPLOAD_KEY;
    if (!secret) {
      return res.status(500).json({ message: "Admin auth is not configured on server" });
    }

    const payload = jwt.verify(token, secret);
    if (payload?.role !== "admin") {
      return res.status(403).json({ message: "Invalid admin session" });
    }

    req.admin = {
      role: payload.role,
      issuedAt: payload.iat,
      expiresAt: payload.exp
    };

    return next();
  } catch {
    return res.status(401).json({ message: "Invalid or expired admin token" });
  }
}

module.exports = {
  requireAdminAuth
};