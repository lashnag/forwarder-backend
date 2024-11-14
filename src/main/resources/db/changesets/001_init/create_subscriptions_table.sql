create table subscriptions (
    subscription_id serial primary key,
    subscriber varchar not null,
    subscription varchar not null,
    keyword varchar not null,
    unique(subscriber, subscription, keyword)
)

