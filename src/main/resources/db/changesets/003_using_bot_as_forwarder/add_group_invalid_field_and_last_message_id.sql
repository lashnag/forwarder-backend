alter table groups
    add column invalid boolean default false,
    add column lastmessageid bigint default 0;