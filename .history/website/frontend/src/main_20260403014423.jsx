import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import AdminPage from "./AdminPage";
import "./index.css";

const currentPath = window.location.pathname.replace(/\/$/, "") || "/";
const RootView = currentPath === "/admin" ? AdminPage : App;

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <RootView />
  </React.StrictMode>
);
