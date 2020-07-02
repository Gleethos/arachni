DROP TABLE IF EXISTS tail_tag_reltions;
--<#SPLIT#>--
DROP TABLE IF EXISTS tags;
--<#SPLIT#>--
DROP TABLE IF EXISTS tail_relations;
--<#SPLIT#>--
DROP TABLE IF EXISTS tails;
--<#SPLIT#>--
CREATE TABLE tails(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  value TEXT NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT
);
--<#SPLIT#>--
CREATE TABLE tail_relations(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  parent_tail_id INTEGER NOT NULL,
  child_tail_id INTEGER NOT NULL,
  description TEXT NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT,
  FOREIGN KEY (parent_tail_id) REFERENCES tails (id),
  FOREIGN KEY (child_tail_id) REFERENCES tails (id)
);

--<#SPLIT#>--
CREATE TABLE tags(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  description TEXT NULL,
  created TEXT NOT NULL,
  deleted TEXT
);
--<#SPLIT#>--
CREATE TABLE tail_tag_relations(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  tail_id INTEGER NOT NULL,
  tag_id INTEGER NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT,
  FOREIGN KEY (tail_id) REFERENCES tails (id),
  FOREIGN KEY (tag_id) REFERENCES tags (id)
);

