const { useState, useEffect, useRef, useCallback } = React;

const CATEGORIES = ["FOREX", "CRYPTO", "COMMODITY", "INDEX"];

function directionClass(direction) {
  if (direction === "BUY") return "buy";
  if (direction === "SELL") return "sell";
  return "neutral";
}

function formatPrice(value) {
  return typeof value === "number" ? value.toFixed(5) : "-";
}

function formatDate(value) {
  return value ? new Date(value).toLocaleString() : "-";
}

let audioContext = null;

function unlockAudio() {
  const Ctx = window.AudioContext || window.webkitAudioContext;
  if (!Ctx) return;
  if (!audioContext) audioContext = new Ctx();
  if (audioContext.state === "suspended") audioContext.resume();
}

function playAlert() {
  if (!audioContext) return;
  const now = audioContext.currentTime;
  const oscillator = audioContext.createOscillator();
  const gain = audioContext.createGain();
  oscillator.type = "sine";
  oscillator.frequency.setValueAtTime(880, now);
  oscillator.frequency.setValueAtTime(1320, now + 0.15);
  gain.gain.setValueAtTime(0.0001, now);
  gain.gain.exponentialRampToValueAtTime(0.2, now + 0.02);
  gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.32);
  oscillator.connect(gain);
  gain.connect(audioContext.destination);
  oscillator.start(now);
  oscillator.stop(now + 0.34);
}

function Login({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  function submit(event) {
    event.preventDefault();
    unlockAudio();
    setBusy(true);
    setError(null);
    api
      .post("/auth/login", { username: username, password: password })
      .then(function (login) {
        onLogin(login.token, login.role);
      })
      .catch(function (e) {
        setError(e.message);
      })
      .then(function () {
        setBusy(false);
      });
  }

  return (
    <div className="login">
      <form className="login-card" onSubmit={submit}>
        <img className="logo" src="/img/logo.png" alt="Ascendisk" />
        <h1>ASCENDISK</h1>
        <p className="muted">Binary options signal indicator</p>
        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoFocus />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        </label>
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={busy}>
          {busy ? "Signing in..." : "Sign in"}
        </button>
      </form>
    </div>
  );
}

function SignalCard({ signal, onPick }) {
  const cls = directionClass(signal.direction);
  const scoreClass = signal.score >= 80 ? "score-high" : "score-low";
  let label = "ATTENTE";
  if (signal.otcClosed) {
    label = "MARCHÉ FERMÉ (OTC)";
  } else if (signal.direction === "BUY") {
    label = "BUY / UP";
  } else if (signal.direction === "SELL") {
    label = "SELL / DOWN";
  }
  return (
    <div className={"card " + cls}>
      <div className="card-head">
        <span className="pair">{signal.displayName}</span>
        {signal.otcClosed && <span className="badge closed">OTC</span>}
        {signal.tradable && <span className="badge tradable">SIGNAL</span>}
      </div>
      <div className="card-body">
        <div className="col">
          <span className="label">Prix actuel</span>
          <span className="value">{formatPrice(signal.currentPrice)}</span>
          <span className="label">Ouverture M1</span>
          <span className="value">{formatPrice(signal.openPrice)}</span>
          <span className="label">Payout</span>
          <span className="value payout">{signal.payout || "-"}</span>
        </div>
        <div className="col">
          <span className="label">Score</span>
          <span className={"value " + scoreClass}>{signal.score}%</span>
          <span className="label">Zone</span>
          <span className="value">
            {formatPrice(signal.zonePrice)} <span className="touches">×{signal.zoneTouches}</span>
          </span>
          <span className="label">Distance</span>
          <span className="value">{formatPrice(signal.distance)}</span>
        </div>
      </div>
      <button
        className={"direction " + cls}
        disabled={!signal.tradable}
        onClick={() => onPick(signal)}
      >
        {label}
      </button>
    </div>
  );
}

function SignalsTab({ signals, error, onTracked }) {
  const [pickError, setPickError] = useState(null);

  function pick(signal) {
    setPickError(null);
    api
      .post("/trades", {
        symbol: signal.symbol,
        direction: signal.direction,
        zonePrice: signal.zonePrice,
      })
      .then(function () {
        if (onTracked) onTracked();
      })
      .catch(function (e) {
        setPickError(e.message);
      });
  }

  return (
    <section className="tab">
      <h2>Signaux</h2>
      <p className="muted">
        Triés par proximité de la zone d'entrée. Un signal n'apparaît qu'à partir de 75%.
      </p>
      {error && <p className="error">{error}</p>}
      {pickError && <p className="error">{pickError}</p>}
      <div className="cards">
        {signals.map((signal) => (
          <SignalCard key={signal.symbol} signal={signal} onPick={pick} />
        ))}
      </div>
    </section>
  );
}

function PriceChart({ symbol }) {
  const containerRef = useRef(null);
  const chartRef = useRef(null);

  useEffect(() => {
    const chart = LightweightCharts.createChart(containerRef.current, {
      height: 170,
      layout: { background: { color: "#181825" }, textColor: "#cdd6f4" },
      grid: { vertLines: { color: "#313244" }, horzLines: { color: "#313244" } },
      timeScale: { timeVisible: true, secondsVisible: false, borderColor: "#313244" },
      rightPriceScale: { borderColor: "#313244" },
    });
    const series = chart.addCandlestickSeries({
      upColor: "#a6e3a1",
      downColor: "#f38ba8",
      borderVisible: false,
      wickUpColor: "#a6e3a1",
      wickDownColor: "#f38ba8",
    });
    chartRef.current = { chart: chart, series: series };
    function onResize() {
      chart.applyOptions({ width: containerRef.current.clientWidth });
    }
    onResize();
    window.addEventListener("resize", onResize);
    return () => {
      window.removeEventListener("resize", onResize);
      chart.remove();
      chartRef.current = null;
    };
  }, []);

  useEffect(() => {
    let active = true;
    function load() {
      api
        .get("/assets/" + encodeURIComponent(symbol) + "/candles?limit=120")
        .then(function (candles) {
          if (!active || !chartRef.current) return;
          const unique = [];
          candles.forEach(function (candle) {
            const time = Math.floor(new Date(candle.openTime).getTime() / 1000);
            if (!unique.length || time > unique[unique.length - 1].time) {
              unique.push({
                time: time,
                open: candle.open,
                high: candle.high,
                low: candle.low,
                close: candle.close,
              });
            }
          });
          chartRef.current.series.setData(unique);
        })
        .catch(function () {});
    }
    load();
    const id = setInterval(load, 10000);
    return () => {
      active = false;
      clearInterval(id);
    };
  }, [symbol]);

  return <div className="chart" ref={containerRef} />;
}

function TradeCard({ trade, onClose }) {
  const cls = directionClass(trade.direction);
  return (
    <div className={"card " + cls}>
      <div className="card-head">
        <span className="pair">{trade.displayName}</span>
        <span className={"badge status-" + trade.status}>{trade.status}</span>
      </div>
      <div className="card-body">
        <div className="col">
          <span className="label">Direction</span>
          <span className="value">{trade.direction}</span>
          <span className="label">Zone</span>
          <span className="value">{formatPrice(trade.zonePrice)}</span>
        </div>
        <div className="col">
          <span className="label">Score zone</span>
          <span className="value">{trade.entryScore}%</span>
          <span className="label">Payout</span>
          <span className="value payout">{trade.payout || "-"}</span>
        </div>
      </div>
      <PriceChart symbol={trade.symbol} />
      {trade.status === "PENDING" && (
        <div className="actions">
          <button className="win" onClick={() => onClose(trade.id, "WON")}>
            Win
          </button>
          <button className="loss" onClick={() => onClose(trade.id, "LOST")}>
            Loss
          </button>
          <button onClick={() => onClose(trade.id, "DRAW")}>Draw</button>
        </div>
      )}
      {trade.status !== "PENDING" && (
        <p className="muted small">
          {trade.status} · fermé le {formatDate(trade.closedAt)}
        </p>
      )}
    </div>
  );
}

function TrackedTab() {
  const [trades, setTrades] = useState([]);
  const [error, setError] = useState(null);

  const load = useCallback(function () {
    api
      .get("/trades")
      .then(function (data) {
        setTrades(data);
        setError(null);
      })
      .catch(function (e) {
        setError(e.message);
      });
  }, []);

  useEffect(() => {
    load();
    const id = setInterval(load, 8000);
    return () => clearInterval(id);
  }, [load]);

  function close(id, status) {
    api
      .put("/trades/" + id, { status: status })
      .then(function () {
        load();
      })
      .catch(function (e) {
        setError(e.message);
      });
  }

  return (
    <section className="tab">
      <h2>Suivi</h2>
      <p className="muted">
        Positions placées manuellement sur Pocket Option, avec graphique M1 en temps réel.
      </p>
      {error && <p className="error">{error}</p>}
      {!trades.length && <p className="muted">Aucune position suivie pour le moment.</p>}
      <div className="cards">
        {trades.map((trade) => (
          <TradeCard key={trade.id} trade={trade} onClose={close} />
        ))}
      </div>
    </section>
  );
}

function AccountTab() {
  const [account, setAccount] = useState(null);
  const [balance, setBalance] = useState("");
  const [error, setError] = useState(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    api
      .get("/account")
      .then(function (data) {
        setAccount(data);
        setBalance(String(data.balance));
      })
      .catch(function (e) {
        setError(e.message);
      });
  }, []);

  function save(event) {
    event.preventDefault();
    setError(null);
    api
      .put("/account", { balance: Number(balance) })
      .then(function (data) {
        setAccount(data);
        setSaved(true);
        setTimeout(() => setSaved(false), 2000);
      })
      .catch(function (e) {
        setError(e.message);
      });
  }

  if (!account) {
    return (
      <section className="tab">
        <h2>Compte</h2>
        {error ? <p className="error">{error}</p> : <p className="muted">Chargement...</p>}
      </section>
    );
  }

  return (
    <section className="tab">
      <h2>Compte</h2>
      <div className="panel">
        <p className="muted">Trader : {account.username}</p>
        <form onSubmit={save}>
          <label>
            Solde ({account.currency})
            <input value={balance} onChange={(e) => setBalance(e.target.value)} type="number" step="0.01" />
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit">Enregistrer</button>
          {saved && <span className="ok"> Enregistré</span>}
        </form>
        <p className="muted small">Dernière mise à jour : {formatDate(account.updatedAt)}</p>
      </div>
    </section>
  );
}

const EMPTY_ASSET = { symbol: "", displayName: "", category: "FOREX", payout: "-", enabled: true };

function AssetsTab() {
  const [assets, setAssets] = useState([]);
  const [form, setForm] = useState(EMPTY_ASSET);
  const [error, setError] = useState(null);

  const load = useCallback(function () {
    api
      .get("/assets")
      .then(function (data) {
        setAssets(data);
        setError(null);
      })
      .catch(function (e) {
        setError(e.message);
      });
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  function create(event) {
    event.preventDefault();
    api
      .post("/assets", form)
      .then(function () {
        setForm(EMPTY_ASSET);
        load();
      })
      .catch(function (e) {
        setError(e.message);
      });
  }

  function toggle(asset) {
    api
      .put("/assets/" + encodeURIComponent(asset.symbol), {
        symbol: asset.symbol,
        displayName: asset.displayName,
        category: asset.category,
        payout: asset.payout,
        enabled: !asset.enabled,
      })
      .then(function () {
        load();
      })
      .catch(function (e) {
        setError(e.message);
      });
  }

  return (
    <section className="tab">
      <h2>Actifs</h2>
      <p className="muted">Gestion des instruments tradables (réservé ADMIN).</p>
      {error && <p className="error">{error}</p>}
      <form className="asset-form" onSubmit={create}>
        <input
          placeholder="Symbol (EURUSD=X)"
          value={form.symbol}
          onChange={(e) => setForm({ ...form, symbol: e.target.value })}
          required
        />
        <input
          placeholder="Nom affiché"
          value={form.displayName}
          onChange={(e) => setForm({ ...form, displayName: e.target.value })}
          required
        />
        <select
          value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value })}
        >
          {CATEGORIES.map((category) => (
            <option key={category} value={category}>
              {category}
            </option>
          ))}
        </select>
        <input
          placeholder="Payout"
          value={form.payout}
          onChange={(e) => setForm({ ...form, payout: e.target.value })}
        />
        <button type="submit">Ajouter</button>
      </form>
      <table className="assets">
        <thead>
          <tr>
            <th>Symbole</th>
            <th>Nom</th>
            <th>Catégorie</th>
            <th>Payout</th>
            <th>Actif</th>
          </tr>
        </thead>
        <tbody>
          {assets.map((asset) => (
            <tr key={asset.symbol}>
              <td>{asset.symbol}</td>
              <td>{asset.displayName}</td>
              <td>{asset.category}</td>
              <td>{asset.payout || "-"}</td>
              <td>
                <button className={asset.enabled ? "win" : "loss"} onClick={() => toggle(asset)}>
                  {asset.enabled ? "Activé" : "Désactivé"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

function hashTab() {
  const value = window.location.hash.replace(/^#\/?/, "");
  return ["signals", "tracked", "account", "assets"].indexOf(value) >= 0 ? value : "signals";
}

function App() {
  const [token, setToken] = useState(api.getToken());
  const [role, setRole] = useState(api.getRole());
  const [tab, setTab] = useState(hashTab());
  const [signals, setSignals] = useState([]);
  const [signalsError, setSignalsError] = useState(null);
  const [soundEnabled, setSoundEnabled] = useState(true);
  const [installPrompt, setInstallPrompt] = useState(null);
  const tradableRef = useRef(new Set());
  const soundRef = useRef(true);

  useEffect(() => {
    soundRef.current = soundEnabled;
  }, [soundEnabled]);

  useEffect(() => {
    function onPrompt(event) {
      event.preventDefault();
      setInstallPrompt(event);
    }
    function onInstalled() {
      setInstallPrompt(null);
    }
    window.addEventListener("beforeinstallprompt", onPrompt);
    window.addEventListener("appinstalled", onInstalled);
    return () => {
      window.removeEventListener("beforeinstallprompt", onPrompt);
      window.removeEventListener("appinstalled", onInstalled);
    };
  }, []);

  useEffect(() => {
    function onUnauthorized() {
      setToken(null);
      setRole(null);
    }
    function onHash() {
      setTab(hashTab());
    }
    window.addEventListener("ascendisk:unauthorized", onUnauthorized);
    window.addEventListener("hashchange", onHash);
    return () => {
      window.removeEventListener("ascendisk:unauthorized", onUnauthorized);
      window.removeEventListener("hashchange", onHash);
    };
  }, []);

  useEffect(() => {
    if (!token) {
      setSignals([]);
      return undefined;
    }
    let active = true;
    let stopStream = null;
    let pollId = null;
    let watchdog = null;
    let received = false;

    function apply(next) {
      if (!active) return;
      received = true;
      if (watchdog) {
        clearTimeout(watchdog);
        watchdog = null;
      }
      setSignals(next);
      setSignalsError(null);
    }

    function startPolling() {
      if (pollId || !active) return;
      function load() {
        api
          .get("/signals")
          .then(apply)
          .catch(function (e) {
            if (active) setSignalsError(e.message);
          });
      }
      load();
      pollId = setInterval(load, 5000);
    }

    function stopPolling() {
      if (pollId) {
        clearInterval(pollId);
        pollId = null;
      }
    }

    stopStream = api.stream("/signals/stream", {
      onEvent: apply,
      onError: function () {
        stopStream = null;
        startPolling();
      },
      onClose: function () {
        stopStream = null;
        startPolling();
      },
    });

    watchdog = setTimeout(function () {
      if (active && !received) {
        if (stopStream) {
          stopStream();
          stopStream = null;
        }
        startPolling();
      }
    }, 8000);

    return function () {
      active = false;
      if (watchdog) clearTimeout(watchdog);
      if (stopStream) stopStream();
      stopPolling();
    };
  }, [token]);

  useEffect(() => {
    const current = new Set(
      signals.filter((signal) => signal.tradable).map((signal) => signal.symbol)
    );
    const previous = tradableRef.current;
    let appeared = false;
    current.forEach(function (symbol) {
      if (!previous.has(symbol)) appeared = true;
    });
    tradableRef.current = current;
    if (appeared && soundRef.current) playAlert();
  }, [signals]);

  function go(next) {
    window.location.hash = "#/" + next;
    setTab(next);
  }

  function onLogin(newToken, newRole) {
    api.setSession(newToken, newRole);
    setToken(newToken);
    setRole(newRole);
    go("signals");
  }

  function logout() {
    api.clearSession();
    setToken(null);
    setRole(null);
  }

  function toggleSound() {
    unlockAudio();
    setSoundEnabled((value) => !value);
  }

  function install() {
    if (!installPrompt) return;
    installPrompt.prompt();
    installPrompt.userChoice.then(function () {
      setInstallPrompt(null);
    });
  }

  if (!token) {
    return <Login onLogin={onLogin} />;
  }

  const tabs = [
    { id: "signals", label: "Signaux" },
    { id: "tracked", label: "Suivi" },
    { id: "account", label: "Compte" },
  ];
  if (role === "ADMIN") {
    tabs.push({ id: "assets", label: "Actifs" });
  }

  return (
    <div className="app">
      <header className="topbar">
        <span className="brand">
          <img src="/img/logo.png" alt="Ascendisk" />
          ASCENDISK
        </span>
        <nav>
          {tabs.map((item) => (
            <button
              key={item.id}
              className={tab === item.id ? "active" : ""}
              onClick={() => go(item.id)}
            >
              {item.label}
            </button>
          ))}
        </nav>
        {installPrompt && (
          <button className="install" onClick={install} title="Installer l'application">
            Installer
          </button>
        )}
        <button
          className={"sound " + (soundEnabled ? "on" : "off")}
          onClick={toggleSound}
          title="Alerte sonore sur nouveau signal"
        >
          {soundEnabled ? "Son" : "Muet"}
        </button>
        <button className="logout" onClick={logout}>
          Déconnexion
        </button>
      </header>
      <main>
        {tab === "signals" && (
          <SignalsTab signals={signals} error={signalsError} onTracked={() => go("tracked")} />
        )}
        {tab === "tracked" && <TrackedTab />}
        {tab === "account" && <AccountTab />}
        {tab === "assets" && role === "ADMIN" && <AssetsTab />}
      </main>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById("root")).render(<App />);
