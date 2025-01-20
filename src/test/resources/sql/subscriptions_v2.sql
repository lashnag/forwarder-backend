insert into subscribers (subscriber_id, username, chatid) values
    (1,'lashnag', '12345'),
    (2,'otheruser', null);
insert into groups (group_id, groupname, lastmessageid) values
    (1,'samokatus', 100),
    (2, 'pirates', 200);
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