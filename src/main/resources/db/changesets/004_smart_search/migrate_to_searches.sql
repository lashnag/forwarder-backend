ALTER TABLE subscriptions ADD COLUMN search_id INTEGER;

CREATE TEMP TABLE temp_searches AS
SELECT DISTINCT
    keyword AS raw_keyword,
    json_build_object('words', string_to_array(keyword, ' '))::text AS json_string
FROM subscriptions;

INSERT INTO searches (properties)
SELECT json_string::json
FROM temp_searches
    ON CONFLICT DO NOTHING;

UPDATE subscriptions
SET search_id = k.search_id
    FROM searches k
WHERE json_build_object('keywords', string_to_array(subscriptions.keyword, ' '))::text = k.properties::text;

ALTER TABLE subscriptions DROP COLUMN keyword;

ALTER TABLE subscriptions ADD CONSTRAINT fk_search FOREIGN KEY (search_id) REFERENCES searches(search_id);