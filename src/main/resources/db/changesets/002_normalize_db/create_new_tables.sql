create table subscribers (
    subscriber_id serial primary key,
    username varchar not null unique
);

create table groups (
    group_id serial primary key,
    groupname varchar not null unique
);

create table keywords (
   keyword_id serial primary key,
   keyword varchar not null,
   subscriber_id integer not null references subscribers(subscriber_id),
   group_id integer not null references groups(group_id),
   unique(subscriber_id, group_id, keyword)
);

