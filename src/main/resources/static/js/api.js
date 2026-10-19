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
  };
})();
