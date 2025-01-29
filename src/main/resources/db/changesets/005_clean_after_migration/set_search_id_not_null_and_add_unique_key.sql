ALTER TABLE subscriptions
    ALTER COLUMN search_id SET NOT NULL;

CREATE UNIQUE INDEX unique_subscriber_group_search ON subscriptions(subscriber_id, group_id, search_id);