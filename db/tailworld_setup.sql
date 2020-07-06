
INSERT INTO tails
(name, value, created)
VALUES
('First Tail', 'bla bla', datetime('now'));

--<#SPLIT#>--

INSERT INTO tails
(name, value, created)
VALUES
('Second Tail', 'blu bli', datetime('now'));

--<#SPLIT#>--

INSERT INTO tail_relations
(parent_tail_id, child_tail_id, description, created)
VALUES
(1, 2, 'Title Relation', datetime('now'));

--<#SPLIT#>--

INSERT INTO tags
(name, description, created)
VALUES
('FirstTag', 'First tail...', datetime('now'));

--<#SPLIT#>--

INSERT INTO tail_tag_relations
(tail_id, tag_id, created)
VALUES
(1, 1, datetime('now'));








