
INSERT INTO tales
(name, value, created)
VALUES
('First Tale', 'bla bla', datetime('now'));

--<#SPLIT#>--

INSERT INTO tales
(name, value, created)
VALUES
('Second Tale', 'blu bli', datetime('now'));

--<#SPLIT#>--

INSERT INTO tales
(name, value, created)
VALUES
('Third Tale', 'A long time ago...', datetime('now'));

--<#SPLIT#>--
INSERT INTO tale_relations
(parent_tale_id, child_tale_id, description, created)
VALUES
(1, 2, 'Title Relation', datetime('now'));

--<#SPLIT#>--

INSERT INTO tags
(name, description, created)
VALUES
('FirstTag', 'First tale...', datetime('now'));

--<#SPLIT#>--

INSERT INTO tale_tag_relations
(tale_id, tag_id, created)
VALUES
(1, 1, datetime('now'));








