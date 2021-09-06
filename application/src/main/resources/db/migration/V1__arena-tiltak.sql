
CREATE TYPE arena_tiltakstatuskode AS ENUM (
    'AVBRUTT',
    'AVSLUTT',
    'GJENNOMFOR',
    'AVLYST',
    'PLANLAGT',
);

CREATE TABLE arena_tiltak (
  tiltaksnavn varchar,
  tiltaksgruppekode varchar,
  tiltakskode varchar,
  administrasjonskode varchar,
  reg_dato datetime,
  mod_dato datetime,
  dato_fra datetime,
  dato_til datetime
);

CREATE TABLE arena_tiltaksgjennomforing (
  tiltaksgjennomforing_id SERIAL PRIMARY KEY,
  tiltakskode varchar,
  antall_deltagere datetime,
  dato_fra datetime,
  dato_til datetime,
  tekst_fagbeskrivelse varchar,
  tekst_maalgruppe varchar,
  reg_dato datetime,
  reg_user varchar,
  mod_dato varchar,
  mod_user varchar,
  lokaltnavn varchar,
  tiltakstatuskode arena_tiltakstatuskode,
  prosent_deltid float,
  arbeidsgiv_id_arrangor int,
  klokkeslett_fremmote varchar,
  dato_fremmote varchar,
  avtale_id int
);

CREATE TABLE arena_arbgiverenhet (
  arbgiv_id SERIAL PRIMARY KEY,
  bedrnr int,
  orgnr_morselskap varchar,
  offnavn varchar,
  altnavn varchar,
  postnr varchar,
  postnr_sted varchar,
  reg_dato datetime,
  mod_dato datetime
);

CREATE TABLE arena_tiltakdeltaker (
  tiltakdeltaker_id SERIAL PRIMARY KEY,
  person_id varchar,
  tiltakgjennomforing_id id,
  deltakerstatuskode varchar,
  deltakertypekode varchar,
  aarsakverdikode_status varchar,
  prioritet int,
  begrunnelse_innsokt varchar,
  begrunnelse_prioritering varchar,
  reg_dato datetime,
  mod_dato datetime,
  dato_fra datetime,
  dato_til datetime,
  prosent_deltid float,
  antall_dager_pr_uke varchar
);

CREATE TABLE arena_person (
  person_id varchar,
  personnr varchar,
  fodselsnr varchar
);
