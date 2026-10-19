# Ascendisk — demo API requests (you can copy and paste)

Base URL: replace `{BASE_URL}` with the URL of the running application
(local development: `http://localhost:8080`).

> Tested from a terminal where `curl` is available (Windows 10+ ships `curl.exe`).
> Sign in first (section 1) and replace `{TOKEN}` with the value returned by the login request.

---

## 1. Login — POST /auth/login (public)

```bash
export BASE="{BASE_URL}"

curl -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"{USERNAME}","password":"{PASSWORD}"}'
```

Response shape: `{"token":"...","role":"ADMIN"}`. Put the token in a variable once:

```bash
export TOKEN="<paste-token-here>"
```

---

## 2. Role checks (expect allowed / 403)

### 2.1 POST /assets — ADMIN only

```bash
# TRADER (expect 403)
curl -X POST $BASE/assets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"XAUUSD=X","displayName":"Gold","category":"COMMODITY","payout":"79%","enabled":true}'

# ADMIN (expect 201)
curl -X POST $BASE/assets \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"XAUUSD=X","displayName":"Gold","category":"COMMODITY","payout":"79%","enabled":true}'
```

### 2.2 GET /assets — any authenticated user (expect 200)

```bash
curl -X GET $BASE/assets -H "Authorization: Bearer $TOKEN"
curl -X GET "$BASE/assets?enabled=true" -H "Authorization: Bearer $TOKEN"
```

### 2.3 Protected endpoint without a token (expect 401)

```bash
curl -X GET $BASE/trades
```

---

## 3. Signals — GET /signals (authenticated)

Returns the scored list of enabled assets, sorted by distance to the entry zone.

```bash
curl -X GET $BASE/signals -H "Authorization: Bearer $TOKEN"
```

Single asset:

```bash
curl -X GET $BASE/signals/EURUSD=X -H "Authorization: Bearer $TOKEN"
```

Response shape (per asset): `symbol`, `currentPrice`, `openPrice`, `ema9`, `ema21`, `rsi`, `trend`,
`momentum`, `zonePrice`, `zoneTouches`, `zoneStrength`, `score`, `direction`, `tradable`, `distance`,
`payout`, `otcClosed`, `evaluatedAt`.

> On the OTC weekend window (Friday 21:00 UTC → Sunday 21:00 UTC) every entry comes back with
> `otcClosed: true` and `direction: "NEUTRAL"`.

---

## 4. Candles — GET /assets/{symbol}/candles (authenticated)

```bash
curl -X GET "$BASE/assets/EURUSD=X/candles?limit=60" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 5. Trades — manually placed orders (authenticated)

### 5.1 Track a trade — POST /trades

```bash
curl -X POST $BASE/trades \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"EURUSD=X","direction":"BUY","zonePrice":1.0850}'
```

Response shape: `{"id":"...","symbol":"EURUSD=X","direction":"BUY","status":"PENDING",...}`.

### 5.2 List trades — GET /trades

```bash
curl -X GET $BASE/trades -H "Authorization: Bearer $TOKEN"
curl -X GET "$BASE/trades?status=PENDING" -H "Authorization: Bearer $TOKEN"
```

### 5.3 Close a trade — PUT /trades/{tradeId}

```bash
curl -X PUT $BASE/trades/{TRADE_ID} \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"WON","result":"take profit"}'
```

`status` accepts `PENDING`, `WON`, `LOST`, `DRAW` or `CANCELLED`.

---

## 6. Account — GET / PUT /account (authenticated)

```bash
curl -X GET $BASE/account -H "Authorization: Bearer $TOKEN"

curl -X PUT $BASE/account \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"balance":1500}'
```

---

## 7. Health & UI

```bash
curl -X GET $BASE/ping    # -> pong
```

Open the single-page UI in a browser at `{BASE_URL}/`.
