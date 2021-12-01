
ALTER TABLE nav_ansatt ADD COLUMN navn VARCHAR NOT NULL DEFAULT ''; -- Default skal ikke brukes når vi merger migreringene sammen i 1 script

ALTER TABLE nav_ansatt DROP COLUMN fornavn;

ALTER TABLE nav_ansatt DROP COLUMN etternavn;
