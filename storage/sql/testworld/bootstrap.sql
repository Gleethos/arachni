PRAGMA foreign_keys = ON;
--<#SPLIT#>--
DROP TABLE IF EXISTS human_relation_attribute_relations;
--<#SPLIT#>--
DROP TABLE IF EXISTS human_attribute_relations;
--<#SPLIT#>--
DROP TABLE IF EXISTS attributes;
--<#SPLIT#>--
DROP TABLE IF EXISTS human_relations;
--<#SPLIT#>--
DROP TABLE IF EXISTS humans;
--<#SPLIT#>--
CREATE TABLE humans(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  value TEXT NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT
);
--<#SPLIT#>--
CREATE TABLE human_relations(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  superior_human_id INTEGER NOT NULL,
  inferior_human_id INTEGER NOT NULL,
  description TEXT NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT,
  FOREIGN KEY (superior_human_id) REFERENCES humans (id),
  FOREIGN KEY (inferior_human_id) REFERENCES humans (id)
);

--<#SPLIT#>--

CREATE TABLE human_relation_attribute_relations(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  relation_id INTEGER NOT NULL,
  attribute_id INTEGER NOT NULL,
  description TEXT NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT,
  FOREIGN KEY (relation_id) REFERENCES human_relations (id),
  FOREIGN KEY (attribute_id) REFERENCES attributes (id)
);

--<#SPLIT#>--

CREATE TABLE attributes(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  description TEXT NULL,
  created TEXT NOT NULL,
  deleted TEXT
);

--<#SPLIT#>--

CREATE TABLE human_attribute_relations(
  id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  human_id INTEGER NOT NULL,
  attribute_id INTEGER NOT NULL,
  description TEXT NOT NULL,
  created TEXT NOT NULL,
  deleted TEXT,
  FOREIGN KEY (human_id) REFERENCES humans (id),
  FOREIGN KEY (attribute_id) REFERENCES attributes (id)
);

