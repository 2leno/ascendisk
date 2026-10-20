create table if not exists app_member
(
    id uuid
        constraint app_member_pk primary key,
    firebase_uid varchar not null
        constraint app_member_firebase_uid_key unique,
    email varchar not null
        constraint app_member_email_key unique,
    display_name varchar,
    role varchar not null,
    status varchar not null,
    created_at timestamptz not null,
    last_seen_at timestamptz not null
);

create table if not exists license_key
(
    id uuid
        constraint license_key_pk primary key,
    code_hash varchar not null
        constraint license_key_code_hash_key unique,
    code_prefix varchar not null,
    label varchar,
    max_activations integer not null default 1,
    activation_count integer not null default 0,
    expires_at timestamptz,
    active boolean not null default true,
    created_at timestamptz not null,
    created_by uuid
);

create table if not exists license_activation
(
    id uuid
        constraint license_activation_pk primary key,
    license_key_id uuid not null
        constraint license_activation_key_fk references license_key (id),
    member_id uuid not null
        constraint license_activation_member_fk references app_member (id),
    activated_at timestamptz not null,
    revoked_at timestamptz,
    constraint license_activation_key_member_key unique (license_key_id, member_id)
);

create index if not exists license_activation_member_idx on license_activation (member_id);
create index if not exists license_activation_key_idx on license_activation (license_key_id);

delete from app_user where username in ('admin', 'trader');
