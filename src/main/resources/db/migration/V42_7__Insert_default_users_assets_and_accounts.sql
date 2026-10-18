insert into app_user (id, username, password, role, first_name, last_name)
values ('00000000-0000-0000-0000-000000000001', 'admin',
        '$2a$10$1tG56a0H6GiS.RwsWxMmUelUuFO9sWXV6zRVC62Y5WVQmCU3hvzHO', 'ADMIN', 'Admin', 'Ascendisk'),
       ('00000000-0000-0000-0000-000000000002', 'trader',
        '$2a$10$YKKICkbRfO33h51JRp76BuGcaV2av.d5BmD6wvU3XDwOOQBcubtZm', 'TRADER', 'Trader', 'Ascendisk');

insert into asset (id, symbol, display_name, category, payout, enabled)
values ('00000000-0000-0000-0000-000000000101', 'EURUSD=X', 'EUR/USD', 'FOREX', '-', true),
       ('00000000-0000-0000-0000-000000000102', 'GBPUSD=X', 'GBP/USD', 'FOREX', '-', true),
       ('00000000-0000-0000-0000-000000000103', 'AUDUSD=X', 'AUD/USD', 'FOREX', '-', true),
       ('00000000-0000-0000-0000-000000000104', 'USDJPY=X', 'USD/JPY', 'FOREX', '-', true),
       ('00000000-0000-0000-0000-000000000105', 'USDCAD=X', 'USD/CAD', 'FOREX', '-', true),
       ('00000000-0000-0000-0000-000000000106', 'USDCHF=X', 'USD/CHF', 'FOREX', '-', true),
       ('00000000-0000-0000-0000-000000000107', 'NZDUSD=X', 'NZD/USD', 'FOREX', '-', true),
       ('00000000-0000-0000-0000-000000000108', 'EURJPY=X', 'EUR/JPY', 'FOREX', '-', true);

insert into account (id, username, balance, currency, updated_at)
values ('00000000-0000-0000-0000-000000000201', 'admin', 1000.00, 'USD', now()),
       ('00000000-0000-0000-0000-000000000202', 'trader', 1000.00, 'USD', now());
