create table if not exists app_user
(
    id uuid
        constraint app_user_pk primary key,
    username varchar not null
        constraint app_user_username_key unique,
    password varchar not null,
    role varchar not null,
    first_name varchar not null,
    last_name varchar not null
);
