PRAGMA foreign_keys = ON;
--<#SPLIT#>--
DROP TABLE IF EXISTS tale_tag_relations;
--<#SPLIT#>--
DROP TABLE IF EXISTS tags;
--<#SPLIT#>--
DROP TABLE IF EXISTS tale_relations;
--<#SPLIT#>--
DROP TABLE IF EXISTS tales;
--<#SPLIT#>--
CREATE TABLE tales(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  value TEXT NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT
);
--<#SPLIT#>--
CREATE TABLE tale_relations(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  parent_tale_id INTEGER NOT NULL,
  child_tale_id INTEGER NOT NULL,
  description TEXT NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT,
  FOREIGN KEY (parent_tale_id) REFERENCES tales (id),
  FOREIGN KEY (child_tale_id) REFERENCES tales (id)
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
CREATE TABLE tale_tag_relations(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  tale_id INTEGER NOT NULL,
  tag_id INTEGER NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT,
  FOREIGN KEY (tale_id) REFERENCES tales (id),
  FOREIGN KEY (tag_id) REFERENCES tags (id)
);

