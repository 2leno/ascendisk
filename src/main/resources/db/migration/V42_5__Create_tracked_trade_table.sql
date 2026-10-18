create table if not exists tracked_trade
(
    id uuid
        constraint tracked_trade_pk primary key,
    asset_id uuid not null
        constraint tracked_trade_asset_fk references asset,
    direction varchar not null,
    zone_price double precision not null,
    entry_score integer not null,
    payout varchar,
    status varchar not null,
    result varchar,
    opened_at timestamp with time zone not null,
    closed_at timestamp with time zone
);
