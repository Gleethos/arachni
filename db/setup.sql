
DROP TABLE IF EXISTS temperatures;
--<#SPLIT#>--
CREATE TABLE temperatures(
  id INTEGER NOT NULL PRIMARY KEY,
  value DOUBLE NOT NULL,
  created TEXT
);
--<#SPLIT#>--
INSERT INTO temperatures
(id, value, created)
VALUES
(1, 23.43, datetime('now'));
--<#SPLIT#>--
INSERT INTO temperatures
(id, value, created)
VALUES
(2, 23.43, datetime('now'));
--<#SPLIT#>--
INSERT INTO temperatures
(id, value, created)
VALUES
(3, 23.43, datetime('now'));
--<#SPLIT#>--
INSERT INTO temperatures
(id, value, created)
VALUES
(4, 23.43, datetime('now'));
--<#SPLIT#>--
INSERT INTO temperatures
(id, value, created)
VALUES
(5, 23.43, datetime('now'));










