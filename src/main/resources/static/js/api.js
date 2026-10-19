(function () {
  var TOKEN_KEY = "ascendisk.token";
  var ROLE_KEY = "ascendisk.role";

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function getRole() {
    return localStorage.getItem(ROLE_KEY);
  }

  function setSession(token, role) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(ROLE_KEY, role || "");
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
  }

  function request(method, path, body) {
    var headers = { Accept: "application/json" };
    var token = getToken();
    if (token) {
      headers.Authorization = "Bearer " + token;
    }
    if (body !== undefined && body !== null) {
      headers["Content-Type"] = "application/json";
    }
    return fetch(path, {
      method: method,
      headers: headers,
      body: body === undefined || body === null ? undefined : JSON.stringify(body),
    }).then(function (response) {
      if (response.status === 401) {
        clearSession();
        window.dispatchEvent(new Event("ascendisk:unauthorized"));
      }
      return response.text().then(function (text) {
        var data = null;
        if (text) {
          try {
            data = JSON.parse(text);
          } catch (parseError) {
            data = null;
          }
        }
        if (!response.ok) {
          var message = data && data.message ? data.message : "Request failed (" + response.status + ")";
          throw new Error(message);
        }
        return data;
      });
    });
  }

  function stream(path, handlers) {
    var headers = { Accept: "text/event-stream" };
    var token = getToken();
    if (token) {
      headers.Authorization = "Bearer " + token;
    }
    var controller = new AbortController();
    var stopped = false;

    fetch(path, { headers: headers, signal: controller.signal })
      .then(function (response) {
        if (response.status === 401) {
          clearSession();
          window.dispatchEvent(new Event("ascendisk:unauthorized"));
          throw new Error("unauthorized");
        }
        if (!response.ok || !response.body) {
          throw new Error("stream unavailable (" + response.status + ")");
        }
        var reader = response.body.getReader();
        var decoder = new TextDecoder();
        var buffer = "";
        function pump() {
          return reader.read().then(function (result) {
            if (result.done) {
              if (!stopped && handlers.onClose) handlers.onClose();
              return;
            }
            buffer += decoder.decode(result.value, { stream: true });
            var index;
            while ((index = buffer.indexOf("\n\n")) >= 0) {
              var chunk = buffer.slice(0, index);
              buffer = buffer.slice(index + 2);
              var payload = "";
              chunk.split("\n").forEach(function (line) {
                if (line.indexOf("data:") === 0) {
                  payload += line.slice("data:".length).trim();
                }
              });
              if (payload && handlers.onEvent) {
                try {
                  handlers.onEvent(JSON.parse(payload));
                } catch (parseError) {
                  // ignore malformed events
                }
              }
            }
            return pump();
          });
        }
        return pump();
      })
      .catch(function (error) {
        if (!stopped && handlers.onError) handlers.onError(error);
      });

    return function () {
      stopped = true;
      controller.abort();
    };
  }

  window.api = {
    getToken: getToken,
    getRole: getRole,
    setSession: setSession,
    clearSession: clearSession,
    get: function (path) {
      return request("GET", path, null);
    },
    post: function (path, body) {
      return request("POST", path, body);
    },
    put: function (path, body) {
      return request("PUT", path, body);
    },
    stream: stream,
  };
})();
