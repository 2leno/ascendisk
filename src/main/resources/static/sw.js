const CACHE = "ascendisk-shell-v1";

const SHELL = [
  "/",
  "/index.html",
  "/css/app.css",
  "/js/api.js",
  "/js/app.jsx",
  "/js/pwa.js",
  "/img/logo.png",
  "/img/icon-192.png",
  "/img/icon-512.png",
  "/manifest.webmanifest",
  "/favicon.ico",
];

const API_PREFIXES = ["/auth", "/signals", "/assets", "/trades", "/account", "/ping", "/health"];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches
      .open(CACHE)
      .then((cache) => cache.addAll(SHELL))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((keys) => Promise.all(keys.filter((key) => key !== CACHE).map((key) => caches.delete(key))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  const request = event.request;
  if (request.method !== "GET") {
    return;
  }

  const url = new URL(request.url);
  if (url.origin !== self.location.origin) {
    return;
  }
  if (API_PREFIXES.some((prefix) => url.pathname.startsWith(prefix))) {
    return;
  }

  event.respondWith(
    fetch(request)
      .then((response) => {
        if (response && response.ok) {
          const copy = response.clone();
          caches.open(CACHE).then((cache) => cache.put(request, copy));
        }
        return response;
      })
      .catch(() =>
        caches.match(request).then((cached) => {
          if (cached) {
            return cached;
          }
          if (request.mode === "navigate") {
            return caches.match("/index.html");
          }
          return Response.error();
        })
      )
  );
});
