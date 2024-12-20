insert into subscribers (subscriber_id, username, chatid) values
(1,'lashnag', '12345'),
(2,'otheruser', null);
insert into groups (group_id, groupname, lastmessageid) values
(1,'samokatus', 100),
(2, 'pirates', 200);
insert into keywords (subscriber_id, group_id, keyword) values
(1, 1, 'казань'),
(1, 1, 'новосибирск'),
(1, 2, 'тайланд'),
(2, 2, 'оаэ');