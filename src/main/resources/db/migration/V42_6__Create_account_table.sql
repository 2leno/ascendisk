create table if not exists account
(
    id uuid
        constraint account_pk primary key,
    username varchar not null
        constraint account_username_key unique,
    balance double precision not null,
    currency varchar not null,
    updated_at timestamp with time zone not null
);
