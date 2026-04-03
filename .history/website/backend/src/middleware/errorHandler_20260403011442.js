function notFound(_req, res) {
  return res.status(404).json({
    message: "Route not found"
  });
}

function errorHandler(error, _req, res, _next) {
  const status = error.statusCode || 500;
  const message = error.message || "Unexpected server error";

  return res.status(status).json({
    message,
    details: process.env.NODE_ENV === "production" ? undefined : error.stack
  });
}

module.exports = {
  notFound,
  errorHandler
};
