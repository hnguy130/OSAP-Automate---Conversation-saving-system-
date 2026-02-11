 -- comment
 CREATE TABLE IF NOT EXISTS contexts (
    -- column 1 id
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    --column 2 context
    context TEXT,
    -- column 3 created_at
    created_at TIMESTAMP DEFAULT NOW()
 );

 INSERT INTO contexts (context) VALUES ('test from file') RETURNING id;
 
 SELECT * FROM contexts;