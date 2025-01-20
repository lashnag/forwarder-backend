insert into subscribers (subscriber_id, username) values
    (1,'lashnag'),
    (2,'otheruser');
insert into groups (group_id, groupname) values
    (1,'samokatus'),
    (2, 'pirates');
insert into searches (search_id, properties) values
    (1, '{"keywords" : ["казань"]}'),
    (2, '{"keywords" : ["новосибирск"]}'),
    (3, '{"keywords" : ["тайланд"]}'),
    (4, '{"keywords" : ["оаэ"]}');

insert into subscriptions (subscriber_id, group_id, search_id) values
(1, 1, 1),
(1, 1, 2),
(1, 2, 3),
(2, 2, 4);