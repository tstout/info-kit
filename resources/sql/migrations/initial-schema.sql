create table LOGS (
   id     int identity(1, 1) primary key not null
  ,at     datetime not null
  ,level  varchar(32) not null
  ,msg    varchar(4096) not null
  ,logger varchar(200) not null
  ,thread varchar(200) not null
  ,ndc    varchar(1000)
);

create table TAGS (
  id int identity(1,1) primary key not null
  ,name varchar(1000)
  ,count int default 0
);

create table ARTIFACTS (
  id int identity(1, 1) primary key not NULL
  ,created datetime not null default current_timestamp()
  ,name varchar(1000)
  ,body clob
);

create table ARTIFACT_TAGS (
  artifact_id int not null
  ,tag_id int not null
  ,foreign key (artifact_id) references ARTIFACTS(id)
  ,foreign key (tag_id) references TAGS(id)
);