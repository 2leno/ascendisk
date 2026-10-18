create table if not exists asset
(
    id uuid
        constraint asset_pk primary key,
    symbol varchar not null
        constraint asset_symbol_key unique,
    display_name varchar not null,
    category varchar not null,
    payout varchar,
    enabled boolean not null default true
);
