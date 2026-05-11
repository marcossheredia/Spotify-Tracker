const LOCAL_BACKEND_FALLBACK = "http://127.0.0.1:8080";
const LOCAL_FRONTEND_FALLBACK = "http://127.0.0.1:5173";

function normalizeBrowserHost(hostname) {
  if (!hostname) {
    return "localhost";
  }

  const normalizedHostname = hostname.toLowerCase();
  if (normalizedHostname === "0.0.0.0" || normalizedHostname === "::" || normalizedHostname === "[::]") {
    return "localhost";
  }

  return hostname;
}

function getNormalizedWindowOrigin() {
  if (typeof window === "undefined") {
    return LOCAL_FRONTEND_FALLBACK;
  }

  const currentUrl = new URL(window.location.origin);
  currentUrl.hostname = normalizeBrowserHost(currentUrl.hostname);
  currentUrl.pathname = "";
  currentUrl.search = "";
  currentUrl.hash = "";

  return currentUrl.origin;
}

function getDefaultBackendBaseUrl() {
  const currentUrl = new URL(getNormalizedWindowOrigin());
  currentUrl.port = "8080";
  currentUrl.pathname = "";
  currentUrl.search = "";
  currentUrl.hash = "";

  return currentUrl.origin;
}

export function getBackendBaseUrl() {
  const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
  return configuredBaseUrl || getDefaultBackendBaseUrl();
}

export function getFrontendBaseUrl() {
  return getNormalizedWindowOrigin();
}
