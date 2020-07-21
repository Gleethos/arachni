
-----------------------------------------------------------------------------------
-- First human relation (2 humans) -> with 2 relation attribute relations

    INSERT INTO humans -- id : 1
    (name, value, created)
    VALUES
    ('Mr Bean', 'He is funny!', datetime('now'));

--<#SPLIT#>--

    INSERT INTO humans  -- id : 2
    (name, value, created)
    VALUES
    ('Teddy', 'Beans best friend.', datetime('now'));

--<#SPLIT#>--
-- First relation :

    INSERT INTO human_relations -- id : 1
    (superior_human_id, inferior_human_id, description, created)
    VALUES
    (1, 2, 'They are best friends! ...and kind of weired...', datetime('now'));
    -- Bean <-> Teddy --

--<#SPLIT#>--
-- First relation attribute :

    INSERT INTO attributes -- id : 1
    (name, description, created)
    VALUES
    ('Friendship', 'A kind of relationship...', datetime('now'));

--<#SPLIT#>--
-- First relation relation ^^ :

    INSERT INTO human_relation_attribute_relations -- id : 1
    (relation_id, attribute_id, description, created)
    VALUES
    (1, 1, 'This connects friendship with the relation between teddy and bean...', datetime('now'));


--<#SPLIT#>--
-- Second relation attribute :

    INSERT INTO attributes -- id : 2
    (name, description, created)
    VALUES
    ('Weirdness', 'Also a kind of relationship...', datetime('now'));

--<#SPLIT#>--
-- Second relation relation :P :

    INSERT INTO human_relation_attribute_relations -- id : 2
    (relation_id, attribute_id, description, created)
    VALUES
    (1, 2, 'This connects weirdness with the relation between teddy and bean...', datetime('now'));


-----------------------------------------------------------------------------------
--<#SPLIT#>--
-- (MAX) ID-STATES:
-- humans.id                              == 2 ;  ->( 1:'Bean', 2:'Teddy'            )
-- attributes.id                          == 2 ;  ->( 1:'Friendship', 2:'Weirdness'  )
-- human_relations.id                     == 1 ;  ->( 1:( sup:1, inf:2 )             )
-- human_relation_attribute_relations.id  == 2 ;  ->( 1:( r:1, a:1 ), 2:( r:1, a:2 ) )
-----------------------------------------------------------------------------------
-- Third human relation (3 humans) -> Also with 2 relation attribute relations...

    INSERT INTO humans -- id : 3
    (name, value, created)
    VALUES
    ('Neighbour', 'Very annoying!', datetime('now'));

--<#SPLIT#>--
-- Second Relation:

    INSERT INTO human_relations -- id : 2
    (superior_human_id, inferior_human_id, description, created)
    VALUES
    (1, 3, 'They are know each other! ...weired enemies...', datetime('now'));
    -- Bean <-> Neighbour --

--<#SPLIT#>--
-- Third relation attribute :

    INSERT INTO attributes -- id : 3
    (name, description, created)
    VALUES
    ('Enemies', 'Not so nice...', datetime('now'));

--<#SPLIT#>--
-- Third relation relation  O.o  :

    INSERT INTO human_relation_attribute_relations -- id : 3
    (relation_id, attribute_id, description, created)
    VALUES
    (2, 3, 'This connects "Enemies" with the relation between bean and neighbour...', datetime('now'));
    -- (Bean, Neighbour) knowing  <->  Enemies !

--<#SPLIT#>--
-- Third relation relation  O.o  :

    INSERT INTO human_relation_attribute_relations -- id : 4
    (relation_id, attribute_id, description, created)
    VALUES
    (2, 2, 'This connects weirdness with the relation between bean and neighbour...', datetime('now'));
    -- (Bean, Neighbour) knowing  <->  weirdness !


-----------------------------------------------------------------------------------
--<#SPLIT#>--
-- (MAX) ID-STATES:
-- humans.id                              == 3 ;  ->( 1:'Bean',        2:'Teddy',      3:'Neighbour'     )
-- attributes.id                          == 3 ;  ->( 1:'Friendship',  2:'Weirdness',  3:'Enemies'       )
-- human_relations.id                     == 2 ;  ->( 1:( sup:1, inf:2 ),       2:( sup:1, inf:3 )       )
-- human_relation_attribute_relations.id  == 4 ;  ->( 1:(r:1,a:1), 2:(r:1,a:2), 3:(r:2,a:3), 4:(r:2,a:2) )
-----------------------------------------------------------------------------------

    INSERT INTO humans -- id : 4
    (name, value, created)
    VALUES
    ('John Doe', 'Basic dude!', datetime('now'));

--<#SPLIT#>--
-- Fourth relation attribute :

    INSERT INTO attributes -- id : 4
    (name, description, created)
    VALUES
    ('Mysterious', '...  O.o  ...', datetime('now'));

--<#SPLIT#>--
-- First human - attribute relation :

    INSERT INTO human_attribute_relations -- id : 1
    (human_id, attribute_id, description, created)
    VALUES
    (4, 4, 'Jup! John is mysterious. ', datetime('now'));
    --  John <->  Mysterious !

--<#SPLIT#>--
-- Second human - attribute relation :

    INSERT INTO human_attribute_relations -- id : 1
    (human_id, attribute_id, description, created)
    VALUES
    (4, 2, 'John is a weired person...', datetime('now'));
    --  John <->  Mysterious !

--<#SPLIT#>--





