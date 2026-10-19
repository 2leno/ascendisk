# Ascendisk — demo API requests (you can copy and paste)

Base URL (preprod):

```
https://nmrclahvsdiy25powd7xc62gv40vdsfk.lambda-url.eu-west-3.on.aws
```

> Tested from a terminal where `curl` is available (Windows 10+ ships `curl.exe`).
> Replace `{TOKEN}` with the value returned by the login request.

---

## 1. Accounts (roles)

| Role   | Username | Password   |
| ------ | -------- | ---------- |
| ADMIN  | `admin`  | `admin123` |
| TRADER | `trader` | `trader123`|

---

## 2. Login — POST /auth/login (public)

```bash
export BASE="https://nmrclahvsdiy25powd7xc62gv40vdsfk.lambda-url.eu-west-3.on.aws"

curl -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

curl -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"trader","password":"trader123"}'
```

Response shape: `{"token":"...","role":"ADMIN"}`. Put the tokens in variables once:

```bash
export ADMIN_TOKEN="<paste-admin-token-here>"
export TRADER_TOKEN="<paste-trader-token-here>"
```

---

## 3. Role checks (expect allowed / 403)

### 3.1 POST /assets — ADMIN only

```bash
# TRADER (expect 403)
curl -X POST $BASE/assets \
  -H "Authorization: Bearer $TRADER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"XAUUSD=X","displayName":"Gold","category":"COMMODITY","payout":"79%","enabled":true}'

# ADMIN (expect 201)
curl -X POST $BASE/assets \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"XAUUSD=X","displayName":"Gold","category":"COMMODITY","payout":"79%","enabled":true}'
```

### 3.2 GET /assets — any authenticated user (expect 200)

```bash
curl -X GET $BASE/assets -H "Authorization: Bearer $TRADER_TOKEN"
curl -X GET "$BASE/assets?enabled=true" -H "Authorization: Bearer $TRADER_TOKEN"
```

### 3.3 Protected endpoint without a token (expect 401)

```bash
curl -X GET $BASE/trades
```

---

## 4. Signals — GET /signals (authenticated)

Returns the scored list of enabled assets, sorted by distance to the entry zone.

```bash
curl -X GET $BASE/signals -H "Authorization: Bearer $TRADER_TOKEN"
```

Single asset:

```bash
curl -X GET $BASE/signals/EURUSD=X -H "Authorization: Bearer $TRADER_TOKEN"
```

Response shape (per asset): `symbol`, `currentPrice`, `openPrice`, `ema9`, `ema21`, `rsi`, `trend`,
`momentum`, `zonePrice`, `zoneTouches`, `zoneStrength`, `score`, `direction`, `tradable`, `distance`,
`payout`, `otcClosed`, `evaluatedAt`.

> On the OTC weekend window (Friday 21:00 UTC → Sunday 21:00 UTC) every entry comes back with
> `otcClosed: true` and `direction: "NEUTRAL"`.

---

## 5. Candles — GET /assets/{symbol}/candles (authenticated)

```bash
curl -X GET "$BASE/assets/EURUSD=X/candles?limit=60" \
  -H "Authorization: Bearer $TRADER_TOKEN"
```

---

## 6. Trades — manually placed orders (authenticated)

### 6.1 Track a trade — POST /trades

```bash
curl -X POST $BASE/trades \
  -H "Authorization: Bearer $TRADER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"symbol":"EURUSD=X","direction":"BUY","zonePrice":1.0850}'
```

Response shape: `{"id":"...","symbol":"EURUSD=X","direction":"BUY","status":"PENDING",...}`.

### 6.2 List trades — GET /trades

```bash
curl -X GET $BASE/trades -H "Authorization: Bearer $TRADER_TOKEN"
curl -X GET "$BASE/trades?status=PENDING" -H "Authorization: Bearer $TRADER_TOKEN"
```

### 6.3 Close a trade — PUT /trades/{tradeId}

```bash
curl -X PUT $BASE/trades/{TRADE_ID} \
  -H "Authorization: Bearer $TRADER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"WON","result":"take profit"}'
```

`status` accepts `PENDING`, `WON`, `LOST`, `DRAW` or `CANCELLED`.

---

## 7. Account — GET / PUT /account (authenticated)

```bash
curl -X GET $BASE/account -H "Authorization: Bearer $TRADER_TOKEN"

curl -X PUT $BASE/account \
  -H "Authorization: Bearer $TRADER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"balance":1500}'
```

---

## 8. Health & UI

```bash
curl -X GET $BASE/ping    # -> pong
```

Open the single-page UI in a browser:

```
https://nmrclahvsdiy25powd7xc62gv40vdsfk.lambda-url.eu-west-3.on.aws/
```
