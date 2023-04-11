CREATE TABLE publish
(
    id              uuid    not null,
    type            varchar not null,
    hash            varchar not null,
    first_published timestamp default current_timestamp,
    last_published  timestamp default current_timestamp,
    unique (id, type)
);

create index if not exists publish_id_type on publish (id, type);

