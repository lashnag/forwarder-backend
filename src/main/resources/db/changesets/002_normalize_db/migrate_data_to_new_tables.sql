insert into subscribers (username)
select distinct subscriber from subscriptions_old;

insert into groups (groupname)
select distinct subscription from subscriptions_old;

insert into keywords (subscriber_id, group_id, keyword)
select
    subscriber_id,
    group_id,
    keyword
from subscriptions_old
    join subscribers on subscriber = username
    join groups on subscription = groupname;